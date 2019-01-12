package com.xiaojukeji.carrera.cproxy.consumer;

import com.google.common.collect.Maps;
import com.xiaojukeji.carrera.config.v4.CProxyConfig;
import com.xiaojukeji.carrera.config.v4.GroupConfig;
import com.xiaojukeji.carrera.config.v4.cproxy.ConsumeServerConfiguration;
import com.xiaojukeji.carrera.cproxy.concurrent.CarreraExecutors;
import com.xiaojukeji.carrera.cproxy.config.ConsumeProxyConfiguration;
import com.xiaojukeji.carrera.cproxy.config.ConsumerGroupConfig;
import com.xiaojukeji.carrera.cproxy.config.LocalModeConfig;
import com.xiaojukeji.carrera.cproxy.consumer.offset.CarreraOffsetManager;
import com.xiaojukeji.carrera.cproxy.proxy.ConsumerProxyMain;
import com.xiaojukeji.carrera.cproxy.proxy.ProxyApp;
import com.xiaojukeji.carrera.cproxy.utils.ConfigUtils;
import com.xiaojukeji.carrera.cproxy.utils.LogUtils;
import com.xiaojukeji.carrera.cproxy.utils.MixAll;
import com.xiaojukeji.carrera.cproxy.utils.StringUtils;
import com.xiaojukeji.carrera.dynamic.ParameterDynamicZookeeper;
import com.xiaojukeji.carrera.metric.MetricFactory;
import com.xiaojukeji.carrera.utils.CommonUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;


public class ConfigManager {
    private static final Logger LOGGER = LogUtils.MAIN_LOGGER;

    private ScheduledExecutorService configScheduler =
            CarreraExecutors.newSingleThreadScheduledExecutor("carrera_config_manager_thread_scheduler");

    private String configFilePath;

    private String configType; //local or remote

    private String instance; //ip:port

    ReentrantLock configLock = new ReentrantLock();

    private volatile CProxyConfig curCproxyConfig = null;

    private Map<String/*group*/, GroupConfig> curGroupConfigMap = Maps.newConcurrentMap();

    private CProxyServiceImpl cProxyService; // zk service

    protected CProxyConfigCallback cProxyConfigCallback = new CProxyConfigCallback();

    protected GroupConfigCallback groupConfigCallback = new GroupConfigCallback();

    private ConfigManager() {

    }

    private static class Singleton {
        private static ConfigManager INSTANCE = new ConfigManager();
    }

    public static ConfigManager getInstance() {
        return Singleton.INSTANCE;
    }

    public void start(String configFilePath, String configType) throws Exception {

        LOGGER.info("Carrera ConfigManager starting");

        init(configFilePath, configType);
        if (StringUtils.equals(configType, ProxyApp.CONFIG_TYPE_LOCAL)) {
            loadLocalConfig();
        } else if (StringUtils.equals(configType, ProxyApp.CONFIG_TYPE_REMOTE)) {
            startWatchConfig();
        } else {
            throw new Exception("unknown configType, type:" + configType);
        }

        LOGGER.info("Carrera ConfigManager started");
    }

    private void init(String configFilePath, String configType) throws Exception {
        this.configFilePath = configFilePath;
        this.configType = configType;
    }

    private void loadLocalConfig() throws Exception {
        try {
            configLock.lock();
            LocalModeConfig localConfig = com.xiaojukeji.carrera.utils.ConfigUtils.newConfig(configFilePath, LocalModeConfig.class);

            if (!localConfig.validate()) {
                throw new Exception("local config is invalid.");
            }

            //更新 instance
            String ip = CommonUtils.getHostAddress();
            int port = localConfig.getPort();
            this.instance = ip + ":" + port;

            curCproxyConfig = localConfig.getcProxyConfig();
            curGroupConfigMap = localConfig.getGroupConfigs()
                    .stream().collect(Collectors.toMap(GroupConfig::getGroup, groupConfig -> groupConfig));
            handleCproxyConfigChanged(null, curCproxyConfig);

            for (GroupConfig groupConfig : curGroupConfigMap.values()) {
                handleGroupConfigChanged(groupConfig);
            }

            LOGGER.info("load local config succ! instance:{},config_path:{},content:{}.", instance, configFilePath, localConfig);
        } catch (Throwable e) {
            LOGGER.error("load local config error! err:{}.", e.getMessage(), e);
            throw e;
        } finally {
            configLock.unlock();
        }
    }

    private void startWatchConfig() throws Exception {
        try {
            //载入本地proxy配置(zk_addr + port)
            ConsumeProxyConfiguration proxyConfig = ConsumeProxyConfiguration.loadFromFile(configFilePath);
            if (proxyConfig == null) {
                LOGGER.error("load zk config error");
                throw new Exception("load zk config failed from " + configFilePath + "failed");
            }

            //更新 instance
            String host = proxyConfig.getHost();
            if (StringUtils.isBlank(host)) {
                host = CommonUtils.getHostAddress();
            }
            int port = proxyConfig.getPort();
            this.instance = host + ":" + port;

            //监听配置
            cProxyService = new CProxyServiceImpl(proxyConfig.getZookeeperAddr(), false);
            cProxyService.getAndWatchProxy(this.instance, cProxyConfigCallback);

            LOGGER.info("start watch config from zk succ! instance:{},zk_addr:{}. ", instance, proxyConfig.getZookeeperAddr());
        } catch (Throwable e) {
            LogUtils.logErrorInfo("start_watch_config_error","start watch config error! err:{}.", e.getMessage(), e);
            throw e;
        }
    }


    public ConsumeServerConfiguration getConsumeServerConfiguration() {
        return Optional.ofNullable(curCproxyConfig)
                .map(CProxyConfig::getThriftServer)
                .orElse(null);
    }

    public Map<String, GroupConfig> getCurGroupConfigMap() {
        return curGroupConfigMap;
    }

    public CProxyConfig getCurCproxyConfig() {
        return curCproxyConfig;
    }

    public void shutdown() {
        if (StringUtils.equals(configType, ProxyApp.CONFIG_TYPE_REMOTE)) {
            if (cProxyService != null) {
                cProxyService.shutdown();
            }
        }
        configScheduler.shutdown();
        MetricFactory.destroy();
        LOGGER.info("Carrera ConfigManager shutdown.");
    }


    /**
     * CProxyConfig callback
     */
    protected class CProxyConfigCallback implements ParameterDynamicZookeeper.DataChangeCallback<CProxyConfig> {
        @Override
        public void handleDataChange(String dataPath, CProxyConfig data, Stat stat) throws Exception {
            try {
                configLock.lock();
                LogUtils.logMainInfo("Invoke CProxyConfigCallback.handleDataChange dataPath:{}.", dataPath);

                CProxyConfig oldcProxyConfig = curCproxyConfig;
                curCproxyConfig = data;

                handleCproxyConfigChanged(oldcProxyConfig, curCproxyConfig);

                //group was changed
                if (oldcProxyConfig == null || !oldcProxyConfig.getGroups().equals(curCproxyConfig.getGroups())) {
                    cProxyService.getAndWatchIndex(curCproxyConfig.getGroups(), groupConfigCallback);
                }

            } catch (Throwable e) {
                LogUtils.logErrorInfo("CproxyGroup_change_error",
                        "Got a throwable when handle cproxy change., msg:{}.", e.getMessage(), e);
            } finally {
                configLock.unlock();
            }
        }

        @Override
        public void handleDataDeleted(String dataPath) throws Exception {
            LogUtils.logMainInfo("Invoke CProxyConfigCallback.handleDataDeleted dataPath:{}.", dataPath);

            //shutdown cproxy service.
            ConsumerProxyMain.proxyApp.stop();
        }
    }

    /**
     * GroupConfig callback
     */
    protected class GroupConfigCallback implements ParameterDynamicZookeeper.DataChangeCallback<GroupConfig> {
        @Override
        public void handleDataChange(String dataPath, GroupConfig data, Stat stat) throws Exception {
            LogUtils.logMainInfo("Invoke GroupConfigCallback.handleDataChange dataPath:{}.", dataPath);
            try {
                configLock.lock();
                curGroupConfigMap.put(data.getGroup(), data);
                handleGroupConfigChanged(data);
            } catch (Throwable e) {
                LogUtils.logErrorInfo("GroupConfig_change_error",
                        "Got a throwable when handle group change., msg:{}.", e.getMessage(), e);
            } finally {
                configLock.unlock();
            }
        }

        @Override
        public void handleDataDeleted(String dataPath) throws Exception {
            try {
                configLock.lock();

                LogUtils.logMainInfo("Invoke GroupConfigCallback.handleDataDeleted dataPath:{}.", dataPath);
                String group = dataPath.substring(dataPath.lastIndexOf('/') + 1);

                if (group.length() == 0) {
                    return;
                }
                curGroupConfigMap.remove(group);

                handleGroupConfigDeleted(group);
            } catch (Throwable e) {
                LogUtils.logErrorInfo("GroupConfig_delete_error",
                        "Got a throwable when handle group delete., msg:{}.", e.getMessage(), e);
            } finally {
                configLock.unlock();
            }
        }
    }

    private void handleCproxyConfigChanged(CProxyConfig oldConf, CProxyConfig newConf) {

        if (newConf == null) {
            return;
        }

        if (newConf.getKafkaConfigs() == null) {
            newConf.setKafkaConfigs(Maps.newHashMap());
        }
        if (newConf.getRocketmqConfigs() == null) {
            newConf.setRocketmqConfigs(Maps.newHashMap());
        }

        //brokercluster was changed
        if (ConfigUtils.brokerClusterIsUpdated(oldConf, newConf)) {
            LogUtils.logMainInfo("BrokerCluster was channed, old :{}, new:{}.", oldConf, newConf);
            ConsumerManager.getInstance().updateCproxyConfig(newConf);
            CarreraOffsetManager.getInstance().update(newConf);
        }
    }

    private void handleGroupConfigChanged(GroupConfig newConf) {
        //过滤掉禁用的订阅
        newConf.getTopics().removeIf(upstreamTopic -> !upstreamTopic.isEnabled());
        if (CollectionUtils.size(newConf.getTopics()) == 0) {
            return;
        }

        //构造配置
        ConsumerGroupConfig config = new ConsumerGroupConfig();
        config.setInstance(instance);
        config.setGroup(newConf.getGroup());
        config.setBrokerCluster(MixAll.BROKER_CLUSTER_GENERAL_NAME);
        config.setGroupConfig(newConf);
        config.setcProxyConfig(curCproxyConfig);
        config.setDelayRequestHandlerThreads(newConf.getDelayRequestHandlerThreads());

        config.createIndex();

        if (!config.validate()) {
            LogUtils.logErrorInfo("config_error", "config error, group:{}, config:{}", config.getGroup(), config.toString());
            return;
        }
        ConsumerManager.getInstance().addOrUpdateConsumer(config);
    }

    private void handleGroupConfigDeleted(String group) {
        ConsumerManager.getInstance().stopAndRemoveConsumer(group);
    }

}