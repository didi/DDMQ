package com.xiaojukeji.carrera.pproxy.producer;

import com.google.common.collect.Sets;
import com.xiaojukeji.carrera.config.v4.PProxyConfig;
import com.xiaojukeji.carrera.config.v4.TopicConfig;
import com.xiaojukeji.carrera.config.v4.pproxy.CarreraConfiguration;
import com.xiaojukeji.carrera.config.v4.pproxy.KafkaConfiguration;
import com.xiaojukeji.carrera.config.v4.pproxy.RocketmqConfiguration;
import com.xiaojukeji.carrera.config.v4.pproxy.TopicConfiguration;
import com.xiaojukeji.carrera.dynamic.ParameterDynamicZookeeper;
import com.xiaojukeji.carrera.utils.CommonFastJsonUtils;
import com.xiaojukeji.carrera.utils.CommonUtils;
import com.xiaojukeji.carrera.pproxy.config.PProxyServiceImpl;
import com.xiaojukeji.carrera.pproxy.config.RemoteConfigInfo;
import com.xiaojukeji.carrera.pproxy.ratelimit.IGroupRequestLimiter;
import com.xiaojukeji.carrera.pproxy.utils.ConfigConvertUtils;
import com.xiaojukeji.carrera.pproxy.utils.ConfigUtils;
import com.xiaojukeji.carrera.pproxy.utils.LogUtils;
import com.xiaojukeji.carrera.pproxy.utils.MetricUtils;
import com.xiaojukeji.carrera.pproxy.utils.TimeUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class ConfigManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigManager.class);

    private static final int MESSAGE_MAX_SIZE_DEFAULT = 2 * 1024 * 1024;//2M

    private static final boolean IS_CONFIG_LOCAL_MODE = ConfigUtils.getDefaultConfig("com.xiaojukeji.carrera.config.isConfigLocalMode", false);

    private String path;
    private PProxyServiceImpl proxyConfigService;
    private volatile PProxyConfig proxyConfig;
    private String proxyNodeName;
    private TopicConfigManager topicConfigManager;
    private IGroupRequestLimiter requestLimiter;
    private ProducerManager producerManager;
    private RemoteConfigInfo remoteConfigInfo;

    public ConfigManager(String path) {
        this.path = path;
        topicConfigManager = new TopicConfigManager(this);
    }

    public boolean start() {
        long startTime = TimeUtils.getCurTime();
        if (StringUtils.isEmpty(path)) {
            LOGGER.error("config path is null");
            return false;
        }

        if (IS_CONFIG_LOCAL_MODE) {
            try {
                proxyConfig = CommonFastJsonUtils.toObject(Files.newInputStream(Paths.get(path)), PProxyConfig.class);
                if (!checkProxyConfig(proxyConfig)) {
                    LogUtils.logError("ConfigManager.start", "proxy config is illegal");
                    return false;
                }
                proxyNodeName = getProxyNodeName(null, proxyConfig.getCarreraConfiguration().getThriftServer().getPort());
                topicConfigManager.setDefaultTopicConfig(proxyConfig.getCarreraConfiguration().getDefaultTopicInfoConf());
            } catch (Exception e) {
                LogUtils.logError("ConfigManager.start", "load config error", e);
                return false;
            }
        } else {
            try {
                remoteConfigInfo = RemoteConfigInfo.loadFromFile(path);
                if (remoteConfigInfo == null || remoteConfigInfo.getZookeeperAddr() == null) {
                    LogUtils.logError("ConfigManager.start", "remote mode, but zk path is null");
                    return false;
                }
                if (StringUtils.isEmpty(proxyNodeName)) {
                    proxyNodeName = getProxyNodeName(remoteConfigInfo.getHost(), remoteConfigInfo.getPort());
                }

                proxyConfigService = new PProxyServiceImpl(remoteConfigInfo.getZookeeperAddr(), false);
                proxyConfig = proxyConfigService.getPProxy(proxyNodeName);
                if (!checkProxyConfig(proxyConfig)) {
                    LogUtils.logError("ConfigManager.start", "proxy config is illegal");
                    return false;
                }

                proxyConfig.getCarreraConfiguration().getThriftServer().setPort(remoteConfigInfo.getPort());//update port
                if (remoteConfigInfo.getDefaultTopicInfoConf() != null) {
                    topicConfigManager.setDefaultTopicConfig(remoteConfigInfo.getDefaultTopicInfoConf());
                }
                topicConfigManager.setDefaultTopicConfig(proxyConfig.getCarreraConfiguration().getDefaultTopicInfoConf());
            } catch (Exception e) {
                LogUtils.logError("ConfigManager.start", "load config error", e);
                return false;
            }
        }

        LOGGER.info("get proxy config:{}", proxyConfig);
        LogUtils.getMainLogger().info("get proxy config time cost:{}", TimeUtils.getElapseTime(startTime));
        return true;
    }

    public void setProxyNodeName(String proxyNodeName) {
        this.proxyNodeName = proxyNodeName;
    }

    public void startWatchConfig() throws Exception {
        long startTime = TimeUtils.getCurTime();
        if (IS_CONFIG_LOCAL_MODE) {
            return;
        }

        proxyConfigService.getAndWatchIndex(proxyConfig.getTopics(), new TopicConfigCallback());

        proxyConfigService.getAndWatchProxy(proxyNodeName, new ParameterDynamicZookeeper.DataChangeCallback<PProxyConfig>() {
            @Override
            public void handleDataChange(String dataPath, PProxyConfig data, Stat stat) throws Exception {
                onDataChange(data);
            }

            @Override
            public void handleDataDeleted(String dataPath) throws Exception {
                LogUtils.logError("ConfigManager.startWatchConfig", "node is delete");
            }
        });

        LogUtils.getMainLogger().info("get topic config time cost:{}", TimeUtils.getElapseMills(startTime));
    }

    private synchronized void updateTopicConfig(TopicConfig newTopicConfig) {
        LogUtils.getMainLogger().info("topic config change:{}", newTopicConfig);
        if (newTopicConfig == null || !proxyConfig.getTopics().contains(newTopicConfig.getTopic())
                || newTopicConfig.getTopicUnits().isEmpty()) {
            LOGGER.info("topic:{} not in the with list", newTopicConfig.getTopic());
            return;
        }

        Set<String> newClusters = new HashSet<>();
        Map<String, TopicConfiguration> configUpdate = new HashMap<>();
        Map<String, TopicConfiguration> configDelete = new HashMap<>();
        for (TopicConfiguration topicConfiguration : newTopicConfig.getTopicUnits()) {
            if (isTopicClusterInManagement(topicConfiguration)) {
                configUpdate.put(topicConfiguration.getBrokerCluster(), topicConfiguration);
            } else {
                configDelete.put(topicConfiguration.getBrokerCluster(), topicConfiguration);
            }
            newClusters.add(topicConfiguration.getBrokerCluster());
        }

        configUpdate.forEach((brokerCluster, config) -> configDelete.remove(brokerCluster));

        boolean isUpdated = false;
        for (TopicConfiguration topicConfiguration : configDelete.values()) {
            if (topicConfigManager.containsCluster(newTopicConfig.getTopic(), topicConfiguration.getBrokerCluster())) {
                topicConfigManager.deleteClusterConfig(newTopicConfig.getTopic(), topicConfiguration.getBrokerCluster());
                LogUtils.getMainLogger().info("topic:{} delete broker cluster:{}", newTopicConfig.getTopic(), topicConfiguration.getBrokerCluster());
                isUpdated = true;
            }
        }
        for (TopicConfiguration topicConfiguration : configUpdate.values()) {
            if (!topicConfigManager.containsTopic(newTopicConfig.getTopic())) {
                topicConfigManager.addTopicConfig(newTopicConfig, topicConfiguration);
            } else {
                topicConfigManager.updateClusterConfig(newTopicConfig, topicConfiguration);
            }

            MetricUtils.addTopic(newTopicConfig.getTopic());
            if (newTopicConfig.isDelayTopic()) {
                MetricUtils.addTopic(ConfigConvertUtils.addMarkForDelayTopic(newTopicConfig.getTopic()));
            }
            LogUtils.getMainLogger().info("topic:{} config update:{}", newTopicConfig.getTopic(), topicConfiguration);
            isUpdated = true;
        }

        Set<String> delClusters = new HashSet<>();
        if (!CollectionUtils.isEmpty(topicConfigManager.getBrokerClusters(newTopicConfig.getTopic()))) {
            delClusters.addAll(topicConfigManager.getBrokerClusters(newTopicConfig.getTopic()));
        }
        delClusters.removeAll(newClusters);
        LogUtils.getMainLogger().info("topic:{}, broker clusters:{} delete", newTopicConfig.getTopic(), delClusters);
        for (String brokerCluster : delClusters) {
            topicConfigManager.deleteClusterConfig(newTopicConfig.getTopic(), brokerCluster);
        }


        if (isUpdated && proxyConfig.getCarreraConfiguration().isUseRequestLimiter() && requestLimiter != null) {
            requestLimiter.updateConfig(topicConfigManager);
        }
    }

    private boolean isTopicClusterInManagement(TopicConfiguration topicConfiguration) {
        for (Set<String> proxyNodes : topicConfiguration.getProxies().values()) {
            if (proxyNodes.contains(proxyNodeName)) {
                return true;
            }
        }
        return false;
    }

    public void registerLimiterForConfigUpdate(IGroupRequestLimiter requestLimiter) {
        this.requestLimiter = requestLimiter;
    }


    private synchronized void onDataChange(PProxyConfig newPProxyConfig) throws Exception {
        LogUtils.getMainLogger().info("proxyConfig change, old:{}, new:{}", proxyConfig, newPProxyConfig);
        if (!checkProxyConfig(newPProxyConfig)) {
            return;
        }

        //swap
        PProxyConfig oldProxyConfig = proxyConfig.clone();
        proxyConfig = newPProxyConfig;

        //add new and delete not in management
        if (!CollectionUtils.isEqualCollection(oldProxyConfig.getTopics(), proxyConfig.getTopics())) {
            Set<String> topics = getTopicsInProxyConfigAndLocal(proxyConfig);
            updateTopics(topics);
        }

        //rocketmq client update
        checkAndUpdateRmqConfig(oldProxyConfig.getCarreraConfiguration().getRocketmqConfigurationMap(),
                proxyConfig.getCarreraConfiguration().getRocketmqConfigurationMap());

        //kafka client update
        checkAndUpdateKafkaConfig(oldProxyConfig.getCarreraConfiguration().getKafkaConfigurationMap(),
                proxyConfig.getCarreraConfiguration().getKafkaConfigurationMap());

        //cluster change, update all topics
        if (!CollectionUtils.isEqualCollection(oldProxyConfig.getBrokerClusters(), proxyConfig.getBrokerClusters())) {
            updateAllTopicConfig();
            LogUtils.getMainLogger().info("cluster change, old:{}, new:{}", oldProxyConfig.getBrokerClusters(), proxyConfig.getBrokerClusters());
        }

        //node rate limiter update
        if (oldProxyConfig.getCarreraConfiguration().getMaxTps() != proxyConfig.getCarreraConfiguration().getMaxTps()
                || oldProxyConfig.getCarreraConfiguration().getTpsWarningRatio() != proxyConfig.getCarreraConfiguration().getTpsWarningRatio()) {
            requestLimiter.updateNodeConfig(proxyConfig.getCarreraConfiguration().getTpsWarningRatio(),
                    proxyConfig.getCarreraConfiguration().getMaxTps());
        }

        proxyConfigService.getAndWatchIndex(proxyConfig.getTopics(), new TopicConfigCallback());
        LogUtils.getMainLogger().info("proxyConfig change completely");
    }

    private boolean checkProxyConfig(PProxyConfig pProxyConfig) throws Exception {
        if (pProxyConfig == null) {
            LogUtils.getMainLogger().warn("proxy config is null, do not update config");
            return false;
        } else if (!pProxyConfig.validate()) {
            LogUtils.getMainLogger().warn("proxy config is invalidate");
            return false;
        }

        Set<String> brokerClusterConfig = Sets.newHashSet();
        if (pProxyConfig.getCarreraConfiguration().getKafkaConfigurationMap() != null) {
            brokerClusterConfig.addAll(pProxyConfig.getCarreraConfiguration().getKafkaConfigurationMap().keySet());
        }
        if (pProxyConfig.getCarreraConfiguration().getRocketmqConfigurationMap() != null) {
            brokerClusterConfig.addAll(pProxyConfig.getCarreraConfiguration().getRocketmqConfigurationMap().keySet());
        }

        if (!pProxyConfig.getBrokerClusters().containsAll(brokerClusterConfig)) {
            LogUtils.getMainLogger().warn("broker cluster not match, cluster list:{}, config list:{}", pProxyConfig.getBrokerClusters(), brokerClusterConfig);
            return false;
        }

        return true;

    }


    //topics contain default
    private Set<String> getTopicsInProxyConfigAndLocal(PProxyConfig newPProxyConfig) {
        Set<String> topics = new HashSet<>(newPProxyConfig.getTopics());
        for (TopicConfig config : newPProxyConfig.getCarreraConfiguration().getDefaultTopicInfoConf().getTopics()) {
            topics.add(config.getTopic());
        }

        if (remoteConfigInfo.getDefaultTopicInfoConf() != null && CollectionUtils.isNotEmpty(remoteConfigInfo.getDefaultTopicInfoConf().getTopics())) {
            for (TopicConfig config : remoteConfigInfo.getDefaultTopicInfoConf().getTopics()) {
                topics.add(config.getTopic());
            }
        }
        return topics;
    }

    private void updateAllTopicConfig() {
        List<TopicConfig> topicConfigs = proxyConfigService.getTopicConfig(proxyConfig.getTopics());
        if (CollectionUtils.isEmpty(topicConfigs)) {
            LogUtils.logError("ConfigManager.updateAllTopicConfig", "do not get config for the topics:" + proxyConfig.getTopics());
            return;
        }

        if (topicConfigs.size() < proxyConfig.getTopics().size()) {
            LogUtils.logError("ConfigManager.updateAllTopicConfig", "get topic config count:" + topicConfigs.size() + " < topic count:" + proxyConfig.getTopics().size());
        }

        for (TopicConfig config : topicConfigs) {
            updateTopicConfig(config);
        }
    }

    private void checkAndUpdateRmqConfig(Map<String, RocketmqConfiguration> oldConfig, Map<String, RocketmqConfiguration> newConfig) {
        if (MapUtils.isEmpty(newConfig)) {
            LOGGER.info("rmq config is null or empty, illegal");
            return;
        }

        for (Map.Entry<String, RocketmqConfiguration> config : newConfig.entrySet()) {
            if (oldConfig == null || !config.getValue().equals(oldConfig.get(config.getKey()))) {
                LogUtils.getMainLogger().debug("rmq broker cluster update, cluster:{}", config.getKey());
                if (producerManager != null) {
                    try {
                        producerManager.addAndUpdateRmqProducer(config.getKey(), ProducerType.RMQ);
                        LogUtils.getMainLogger().info("rmq broker cluster update success, cluster:{}", config.getKey());
                    } catch (Exception ex) {
                        LogUtils.logError("ConfigManager.checkAndUpdateRmqConfig", "broker cluster" + config.getKey() + "start failed", ex);
                    }
                } else {
                    LogUtils.getMainLogger().warn("rmq broker cluster update failed, cluster:{}", config.getKey());
                }
            }
        }

        Set<String> brokerClustersDel = new HashSet<>(oldConfig.keySet());
        brokerClustersDel.removeAll(newConfig.keySet());

        LogUtils.getMainLogger().info("rmq broker cluster delete:{}", brokerClustersDel);
        for (String brokerCluster : brokerClustersDel) {
            producerManager.deleteCluster(brokerCluster);
        }
        LogUtils.getMainLogger().info("rmq cluster update completely");
    }

    private void checkAndUpdateKafkaConfig(Map<String, KafkaConfiguration> oldConfig, Map<String, KafkaConfiguration> newConfig) {
        if (MapUtils.isEmpty(newConfig)) {
            LOGGER.info("rmq config is null or empty, illegal");
            return;
        }

        for (Map.Entry<String, KafkaConfiguration> config : newConfig.entrySet()) {
            if (oldConfig == null || !config.getValue().equals(oldConfig.get(config.getKey()))) {
                if (producerManager != null) {
                    try {
                        producerManager.addAndUpdateRmqProducer(config.getKey(), ProducerType.KAFKA);
                        LogUtils.getMainLogger().info("kafka broker cluster update config success, cluster:{}", config.getKey());
                    } catch (Exception ex) {
                        LogUtils.logError("ConfigManager.checkAndUpdateKafkaConfig", "broker cluster" + config.getKey() + "start failed", ex);
                    }
                }
            }
        }

        Set<String> brokerClustersDel = new HashSet<>(oldConfig.keySet());
        brokerClustersDel.removeAll(newConfig.keySet());

        LogUtils.getMainLogger().info("kafka broker cluster delete:{}", brokerClustersDel);
        for (String brokerCluster : brokerClustersDel) {
            producerManager.deleteCluster(brokerCluster);
        }

        LogUtils.getMainLogger().info("kafka cluster update completely");
    }

    public void registerProducerForConfigUpdate(ProducerManager producerManager) {
        this.producerManager = producerManager;
    }

    public synchronized void updateTopics(Set<String> topics) {
        Set<String> topicsDel = new HashSet<>(topicConfigManager.getTopicConfigs().keySet());
        topicsDel.removeAll(topics);

        Set<String> topicsAdd = new HashSet<>(topics);
        topicsAdd.removeAll(topicConfigManager.getTopicConfigs().keySet());

        List<TopicConfig> newTopicConfigs = proxyConfigService.getTopicConfig(topicsAdd);
        if (!CollectionUtils.isEmpty(newTopicConfigs)) {
            for (TopicConfig config : newTopicConfigs) {
                updateTopicConfig(config);
            }
        }

        for (String topic : topicsDel) {
            topicConfigManager.deleteTopic(topic);
        }

        LogUtils.getMainLogger().info("topic update, add:{}, delete:{}", topicsAdd, topicsDel);
    }

    private String getProxyNodeName(String hostAddress, int port) throws Exception {
        if (StringUtils.isBlank(hostAddress)) {
            hostAddress = CommonUtils.getHostAddress();
        }
        if (StringUtils.isEmpty(hostAddress)) {
            LOGGER.error("do not get host address");
            throw new Exception("do not get host address");
        }
        return hostAddress + ":" + port;
    }


    public PProxyConfig getProxyConfig() {
        return proxyConfig;
    }

    public CarreraConfiguration getCarreraConfig() {
        return proxyConfig.getCarreraConfiguration();
    }

    public int getMaxMessageSize(String topic) {
        Set<String> clusters = topicConfigManager.getBrokerClusters(topic);
        if (CollectionUtils.isEmpty(clusters)) {
            return MESSAGE_MAX_SIZE_DEFAULT;
        }

        int minSize = Integer.MAX_VALUE;
        for (String cluster : clusters) {
            if (proxyConfig.getCarreraConfiguration().getRocketmqConfigurationMap().containsKey(cluster)) {
                int size = proxyConfig.getCarreraConfiguration().getRocketmqConfigurationMap().get(cluster).getMaxMessageSize();
                minSize = size < minSize ? size : minSize;
            }
        }

        minSize = minSize == Integer.MAX_VALUE ? MESSAGE_MAX_SIZE_DEFAULT : minSize;
        return minSize;
    }

    public TopicConfigManager getTopicConfigManager() {
        return topicConfigManager;
    }


    class TopicConfigCallback implements ParameterDynamicZookeeper.DataChangeCallback<TopicConfig> {
        @Override
        public void handleDataChange(String dataPath, TopicConfig data, Stat stat) throws Exception {
            updateTopicConfig(data);
        }

        @Override
        public void handleDataDeleted(String dataPath) throws Exception {
            LOGGER.info("path is deleted:{}", dataPath);
        }
    }
}