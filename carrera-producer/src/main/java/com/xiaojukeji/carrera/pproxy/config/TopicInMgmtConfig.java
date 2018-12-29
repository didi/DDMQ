package com.xiaojukeji.carrera.pproxy.config;

import com.xiaojukeji.carrera.config.v4.TopicConfig;
import com.xiaojukeji.carrera.config.v4.pproxy.TopicConfiguration;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;


public class TopicInMgmtConfig {

    private String topic;

    private volatile boolean delayTopic = false;

    private volatile boolean autoBatch = false;

    private volatile boolean strongOrder = false;
    private ConcurrentHashMap<String, TopicConfiguration> clusterConfig = new ConcurrentHashMap<>();

    private long limiterFailureRetryInterval;

    public TopicInMgmtConfig(String topic, boolean delayTopic, boolean autoBatch, boolean strongOrder, long limiterFailureRetryInterval) {
        this.topic = topic;
        this.delayTopic = delayTopic;
        this.autoBatch = autoBatch;
        this.strongOrder = strongOrder;
        this.limiterFailureRetryInterval = limiterFailureRetryInterval;
    }

    public TopicInMgmtConfig(TopicConfig topicConfig) {
        if (topicConfig == null) {
            return;
        }

        updateCommonConfig(topicConfig);

        for (TopicConfiguration configuration : topicConfig.getTopicUnits()) {
            clusterConfig.put(configuration.getBrokerCluster(), configuration);
        }
    }

    public void updateCommonConfig(TopicConfig topicConfig){
        delayTopic = topicConfig.isDelayTopic();
        autoBatch = topicConfig.isAutoBatch();
        strongOrder = topicConfig.isStrongOrder();
        limiterFailureRetryInterval = topicConfig.getLimiterFailureRetryInterval();
    }

    public boolean isDelayTopic() {
        return delayTopic;
    }

    public void setDelayTopic(boolean delayTopic) {
        this.delayTopic = delayTopic;
    }

    public boolean isAutoBatch() {
        return autoBatch;
    }

    public void setAutoBatch(boolean autoBatch) {
        this.autoBatch = autoBatch;
    }

    public boolean isStrongOrder() {
        return strongOrder;
    }

    public void setStrongOrder(boolean strongOrder) {
        this.strongOrder = strongOrder;
    }

    public void updateTopicConfig(TopicConfiguration topicConfiguration) {
        clusterConfig.put(topicConfiguration.getBrokerCluster(), topicConfiguration);
    }

    public boolean containsCluster(String brokerCluster) {
        return clusterConfig.containsKey(brokerCluster);
    }

    public void deleteCluster(String brokerCluster) {
        clusterConfig.remove(brokerCluster);
    }

    public Set<String> getBrokerClusters(){
        return clusterConfig.keySet();
    }

    public boolean hasCluster(){
        return !clusterConfig.isEmpty();
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public ConcurrentHashMap<String, TopicConfiguration> getClusterConfig() {
        return clusterConfig;
    }

    public void setClusterConfig(ConcurrentHashMap<String, TopicConfiguration> clusterConfig) {
        this.clusterConfig = clusterConfig;
    }

    public long getLimiterFailureRetryInterval() {
        return limiterFailureRetryInterval;
    }

    public void setLimiterFailureRetryInterval(long limiterFailureRetryInterval) {
        this.limiterFailureRetryInterval = limiterFailureRetryInterval;
    }
}