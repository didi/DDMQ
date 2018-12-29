package com.xiaojukeji.carrera.cproxy.config;

import com.xiaojukeji.carrera.config.ConfigurationValidator;
import com.xiaojukeji.carrera.config.v4.CProxyConfig;
import com.xiaojukeji.carrera.config.v4.GroupConfig;
import com.xiaojukeji.carrera.config.v4.cproxy.KafkaConfiguration;
import com.xiaojukeji.carrera.config.v4.cproxy.RocketmqConfiguration;
import com.xiaojukeji.carrera.config.v4.cproxy.UpstreamTopic;
import com.xiaojukeji.carrera.cproxy.utils.PropertyUtils;
import com.xiaojukeji.carrera.cproxy.utils.MixAll;

import java.util.*;


public class ConsumerGroupConfig implements ConfigurationValidator {

    private String instance; //required
    private String group; //required
    private String brokerCluster = MixAll.BROKER_CLUSTER_GENERAL_NAME; //required
    private GroupConfig groupConfig; //required
    private CProxyConfig cProxyConfig; //required
    private int delayRequestHandlerThreads = -1;

    //need run createIndex() to generate
    private Map<String/*Topic*/, UpstreamTopic> topicMap;
    private Map<String/*Topic*/, Integer> topicCount;
    private Map<String/*Topic*/, Long> maxConsumeLagMap;
    private List<String> topicNames;
    private Integer totalThreads;

    public String getInstance() {
        return instance;
    }

    public void setInstance(String instance) {
        this.instance = instance;
    }

    public String getBrokerCluster() {
        return brokerCluster;
    }

    public void setBrokerCluster(String brokerCluster) {
        this.brokerCluster = brokerCluster;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public GroupConfig getGroupConfig() {
        return groupConfig;
    }

    public void setGroupConfig(GroupConfig groupConfig) {
        this.groupConfig = groupConfig;
    }

    public CProxyConfig getcProxyConfig() {
        return cProxyConfig;
    }

    public void setcProxyConfig(CProxyConfig cProxyConfig) {
        this.cProxyConfig = cProxyConfig;
    }

    public int getDelayRequestHandlerThreads() {
        return delayRequestHandlerThreads;
    }

    public void setDelayRequestHandlerThreads(int delayRequestHandlerThreads) {
        this.delayRequestHandlerThreads = delayRequestHandlerThreads;
    }

    public Map<String, UpstreamTopic> getTopicMap() {
        return topicMap;
    }

    public void setTopicMap(Map<String, UpstreamTopic> topicMap) {
        this.topicMap = topicMap;
    }

    public Map<String, Integer> getTopicCount() {
        return topicCount;
    }

    public void setTopicCount(Map<String, Integer> topicCount) {
        this.topicCount = topicCount;
    }

    public Map<String, Long> getMaxConsumeLagMap() {
        return maxConsumeLagMap;
    }

    public void setMaxConsumeLagMap(Map<String, Long> maxConsumeLagMap) {
        this.maxConsumeLagMap = maxConsumeLagMap;
    }

    public List<String> getTopicNames() {
        return topicNames;
    }

    public void setTopicNames(List<String> topicNames) {
        this.topicNames = topicNames;
    }

    public Integer getTotalThreads() {
        return totalThreads;
    }

    public void setTotalThreads(Integer totalThreads) {
        this.totalThreads = totalThreads;
    }

    synchronized public void createIndex() {

        Map<String, UpstreamTopic> tmpTopicMap = new HashMap<>();
        Map<String/*Topic*/, Integer> tmpTopicCount = new HashMap<>();
        Map<String/*Topic*/, Long> tmpMaxConsumeLagMap = new HashMap<>();
        List<String> tmpTopicNames = new ArrayList<>();
        int tmpTotalThreads = 0;

        for (UpstreamTopic upstreamTopic : groupConfig.getTopics()) {
            tmpTopicMap.put(upstreamTopic.getTopic(), upstreamTopic);
            tmpTopicNames.add(upstreamTopic.getTopic());
            tmpTotalThreads += upstreamTopic.getFetchThreads();
            tmpTopicCount.put(upstreamTopic.getTopic(), upstreamTopic.getFetchThreads());
            if (upstreamTopic.getMaxConsumeLag() > 0) {
                tmpMaxConsumeLagMap.put(upstreamTopic.getTopic(), upstreamTopic.getMaxConsumeLag());
            }
        }

        topicMap = tmpTopicMap;
        topicCount = tmpTopicCount;
        maxConsumeLagMap = tmpMaxConsumeLagMap;
        topicNames = tmpTopicNames;
        totalThreads = tmpTotalThreads;
        if (delayRequestHandlerThreads <= 0) {
            this.delayRequestHandlerThreads = (int) Math.max(0.5 * groupConfig.getAsyncThreads(), 1);
        }
    }

    public String getGroupBrokerCluster() {
        return group + "@" + brokerCluster;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConsumerGroupConfig that = (ConsumerGroupConfig) o;
        return delayRequestHandlerThreads == that.delayRequestHandlerThreads &&
                Objects.equals(instance, that.instance) &&
                Objects.equals(group, that.group) &&
                Objects.equals(brokerCluster, that.brokerCluster) &&
                Objects.equals(groupConfig, that.groupConfig) &&
                Objects.equals(cProxyConfig, that.cProxyConfig) &&
                Objects.equals(topicMap, that.topicMap) &&
                Objects.equals(topicCount, that.topicCount) &&
                Objects.equals(maxConsumeLagMap, that.maxConsumeLagMap) &&
                Objects.equals(topicNames, that.topicNames) &&
                Objects.equals(totalThreads, that.totalThreads);
    }

    public boolean bizEquals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConsumerGroupConfig that = (ConsumerGroupConfig) o;
        boolean ret =  delayRequestHandlerThreads == that.delayRequestHandlerThreads &&
                Objects.equals(instance, that.instance) &&
                Objects.equals(group, that.group) &&
                Objects.equals(brokerCluster, that.brokerCluster) &&
                groupConfig.bizEquals(that.groupConfig) &&
                Objects.equals(topicMap, that.topicMap) &&
                Objects.equals(topicCount, that.topicCount) &&
                Objects.equals(maxConsumeLagMap, that.maxConsumeLagMap) &&
                Objects.equals(topicNames, that.topicNames) &&
                Objects.equals(totalThreads, that.totalThreads);

        if (!ret)
            return false;

        if (MixAll.BROKER_CLUSTER_GENERAL_NAME.equals(brokerCluster)) {
            return Objects.equals(cProxyConfig, that.cProxyConfig);
        }

        if (this.cProxyConfig.getRocketmqConfigs().containsKey(brokerCluster)) {
            RocketmqConfiguration oldRmqConf = this.cProxyConfig.getRocketmqConfigs().get(brokerCluster);
            RocketmqConfiguration newRmqConf = that.cProxyConfig.getRocketmqConfigs().get(brokerCluster);
            return oldRmqConf.equals(newRmqConf);
        } else if (this.cProxyConfig.getKafkaConfigs().containsKey(brokerCluster)) {
            KafkaConfiguration oldKafkaConf = this.cProxyConfig.getKafkaConfigs().get(brokerCluster);
            KafkaConfiguration newKafkaConf = that.cProxyConfig.getKafkaConfigs().get(brokerCluster);
            return oldKafkaConf.equals(newKafkaConf);
        }
        return true;
    }

    @Override
    public int hashCode() {

        return Objects.hash(instance, group, brokerCluster, groupConfig, cProxyConfig, delayRequestHandlerThreads, topicMap, topicCount, maxConsumeLagMap, topicNames, totalThreads);
    }

    @Override
    public boolean validate() {
        try {
            if (instance == null || !instance.contains(":") || group == null || brokerCluster == null) {
                return false;
            }
            if (!cProxyConfig.validate() || !groupConfig.validate()) {
                return false;
            }
            if (!MixAll.BROKER_CLUSTER_GENERAL_NAME.equals(brokerCluster) &&
                    cProxyConfig.getKafkaConfigs().get(brokerCluster) == null &&
                    cProxyConfig.getRocketmqConfigs().get(brokerCluster) == null) {
                return false;
            }
        } catch (Throwable e) {
            return false;
        }

        return true;
    }

    @Override
    public String toString() {
        return "ConsumerGroupConfig{" +
                "instance='" + instance + '\'' +
                ", group='" + group + '\'' +
                ", brokerCluster='" + brokerCluster + '\'' +
                ", groupConfig=" + groupConfig +
                ", cProxyConfig=" + cProxyConfig +
                ", delayRequestHandlerThreads=" + delayRequestHandlerThreads +
                ", topicMap=" + topicMap +
                ", topicCount=" + topicCount +
                ", maxConsumeLagMap=" + maxConsumeLagMap +
                ", topicNames=" + topicNames +
                ", totalThreads=" + totalThreads +
                '}';
    }

    /**
     * deep copy
     * @return
     */
    @Override
    public ConsumerGroupConfig clone() {
        ConsumerGroupConfig newConfig = new ConsumerGroupConfig();
        PropertyUtils.copyNonNullProperties(newConfig, this);

        if (this.groupConfig != null) {
            newConfig.groupConfig = this.groupConfig.clone();
        }
        if (this.cProxyConfig != null) {
            newConfig.cProxyConfig = this.cProxyConfig.clone();
        }

        newConfig.createIndex();
        return newConfig;
    }
}