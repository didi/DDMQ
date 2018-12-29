package com.xiaojukeji.carrera.pproxy.producer;

import com.xiaojukeji.carrera.config.v4.TopicConfig;
import com.xiaojukeji.carrera.config.v4.pproxy.RocketmqConfiguration;
import com.xiaojukeji.carrera.config.v4.pproxy.TopicConfiguration;
import com.xiaojukeji.carrera.config.v4.pproxy.TopicInfoConfiguration;
import com.xiaojukeji.carrera.thrift.Message;
import com.xiaojukeji.carrera.pproxy.config.TopicInMgmtConfig;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


public class TopicConfigManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(TopicConfigManager.class);
    private Random clusterRandom = new Random();
    private ConcurrentHashMap<String, TopicInMgmtConfig> topicConfigs = new ConcurrentHashMap<>(256);
    private ConfigManager configManager;

    public TopicConfigManager(ConfigManager configManager) {
        this.configManager = configManager;
    }

    public void setDefaultTopicConfig(TopicInfoConfiguration topicInfoConfiguration) {
        List<TopicConfig> topicConfigList = topicInfoConfiguration.getTopics();
        if (CollectionUtils.isEmpty(topicConfigList)) {
            return;
        }

        for (TopicConfig topicConfig : topicConfigList) {
            TopicInMgmtConfig topicInMgmtConfig = new TopicInMgmtConfig(topicConfig);
            topicConfigs.put(topicConfig.getTopic(), topicInMgmtConfig);
        }
    }

    public void addTopicConfig(TopicConfig topicConfig, TopicConfiguration topicConfiguration) {
        if (!topicConfigs.containsKey(topicConfig.getTopic())) {
            TopicInMgmtConfig topicInMgmtConfig = new TopicInMgmtConfig(topicConfig.getTopic(),
                topicConfig.isDelayTopic(), topicConfig.isAutoBatch(),
                topicConfig.isStrongOrder(), topicConfig.getLimiterFailureRetryInterval());
            topicConfigs.put(topicConfig.getTopic(), topicInMgmtConfig);
        }

        topicConfigs.get(topicConfig.getTopic()).updateTopicConfig(topicConfiguration);
    }

    public boolean updateClusterConfig(TopicConfig topicConfig, TopicConfiguration topicConfiguration) {
        if (!topicConfigs.containsKey(topicConfig.getTopic())) {
            LOGGER.error("topic:{}, not exist", topicConfig.getTopic());
            return false;
        }

        topicConfigs.get(topicConfig.getTopic()).updateCommonConfig(topicConfig);
        topicConfigs.get(topicConfig.getTopic()).updateTopicConfig(topicConfiguration);
        return true;
    }

    public ConcurrentHashMap<String, TopicInMgmtConfig> getTopicConfigs() {
        return topicConfigs;
    }

    public void deleteTopic(String topic) {
        topicConfigs.remove(topic);
    }

    public void deleteClusterConfig(String topic, String brokerCluster) {
        if (!topicConfigs.containsKey(topic)) {
            return;
        }
        topicConfigs.get(topic).deleteCluster(brokerCluster);
        if (!topicConfigs.get(topic).hasCluster()) {
            topicConfigs.remove(topic);
        }
    }

    public boolean containsTopic(String topic) {
        return topicConfigs.containsKey(topic);
    }

    public boolean containsCluster(String topic, String brokerCluster) {
        if (!topicConfigs.containsKey(topic)) {
            return false;
        }

        return topicConfigs.get(topic).containsCluster(brokerCluster);
    }

    public boolean isStrongOrder(String topic) {
        return topicConfigs.get(topic) != null && topicConfigs.get(topic).isStrongOrder();
    }

    public boolean isAutoBatch(String topic) {
        return topicConfigs.get(topic) != null && topicConfigs.get(topic).isAutoBatch();
    }

    public boolean isDelayTopic(String topic) {
        return topicConfigs.get(topic) != null && topicConfigs.get(topic).isDelayTopic();
    }

    public String getBrokerCluster(final Message msg) {
        TopicInMgmtConfig config = topicConfigs.get(msg.getTopic());
        if (config == null) {
            return null;
        }

        Set<String> brokerClusters = config.getClusterConfig().keySet();
        if (brokerClusters.isEmpty()) {
            return null;
        }

        List<String> brokerClusterList = new ArrayList<>(brokerClusters);
        Collections.sort(brokerClusterList);

        if (brokerClusters.size() == 1) {
            return brokerClusterList.get(0);
        }

        long index = clusterRandom.nextLong();
        if (msg.getPartitionId() >= 0) {
            index = msg.partitionId;
        } else if (msg.getPartitionId() == -1) {
            index = msg.hashId;
        } else if (msg.key != null) {
            index = msg.key.hashCode();
        }

        return brokerClusterList.get((int) Math.abs(index) % brokerClusterList.size());
    }

    public String getDefaultRmqBrokerCluster(final Message msg) {
        TopicInMgmtConfig config = topicConfigs.get(msg.getTopic());
        if (config == null) {
            return null;
        }

        Set<String> brokerClusters = config.getClusterConfig().keySet();
        if (brokerClusters.isEmpty()) {
            return null;
        }

        List<String> brokerClusterList = new ArrayList<>(brokerClusters);
        Collections.sort(brokerClusterList);

        for (String aBrokerClusterList : brokerClusterList) {
            Map<String, RocketmqConfiguration> rmqCluster = configManager.getProxyConfig().getCarreraConfiguration().getRocketmqConfigurationMap();
            if (rmqCluster != null && rmqCluster.containsKey(aBrokerClusterList)) {
                return aBrokerClusterList;
            }
        }

        return null;
    }

    public Set<String> getBrokerClusters(String topic) {
        TopicInMgmtConfig config = topicConfigs.get(topic);
        if (config == null) {
            return null;
        }

        return config.getBrokerClusters();
    }
}