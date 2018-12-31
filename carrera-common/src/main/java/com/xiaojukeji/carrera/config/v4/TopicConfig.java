package com.xiaojukeji.carrera.config.v4;

import com.alibaba.fastjson.TypeReference;
import com.xiaojukeji.carrera.config.CompressType;
import com.xiaojukeji.carrera.config.ConfigConstants;
import com.xiaojukeji.carrera.config.ConfigurationValidator;
import com.xiaojukeji.carrera.config.v4.pproxy.TopicConfiguration;
import com.xiaojukeji.carrera.utils.CommonFastJsonUtils;
import com.xiaojukeji.carrera.utils.ConfigUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.List;


public class TopicConfig implements ConfigurationValidator, Cloneable {

    private static final boolean DEFAULT_DELAY_TOPIC = ConfigUtils.getDefaultConfig(
            "com.xiaojukeji.carrera.config.v4.TopicConfig.delayTopic", false);

    private static final boolean DEFAULT_AUTO_BATCH = ConfigUtils.getDefaultConfig(
            "com.xiaojukeji.carrera.config.v4.TopicConfig.autoBatch", false);

    private static final boolean DEFAULT_STRONG_ORDER = ConfigUtils.getDefaultConfig(
            "com.xiaojukeji.carrera.config.v4.TopicConfig.strongOrder", false);

    private String topic;

    private List<String> alarmGroup;

    private List<TopicConfiguration> topicUnits;

    private boolean delayTopic = DEFAULT_DELAY_TOPIC;

    private boolean autoBatch = DEFAULT_AUTO_BATCH;

    private boolean strongOrder = DEFAULT_STRONG_ORDER;

    private CompressType compressType = CompressType.PRIMORDIAL;

    private long limiterFailureRetryInterval = ConfigConstants.LIMITER_FAILURE_RETRY_INTERVAL_FROM_CLUSTER;

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public List<String> getAlarmGroup() {
        return alarmGroup;
    }

    public void setAlarmGroup(List<String> alarmGroup) {
        this.alarmGroup = alarmGroup;
    }

    public List<TopicConfiguration> getTopicUnits() {
        return topicUnits;
    }

    public void setTopicUnits(List<TopicConfiguration> topicUnits) {
        this.topicUnits = topicUnits;
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

    public CompressType getCompressType() {
        return compressType;
    }

    public void setCompressType(CompressType compressType) {
        this.compressType = compressType;
    }

    public long getLimiterFailureRetryInterval() {
        return limiterFailureRetryInterval;
    }

    public void setLimiterFailureRetryInterval(long limiterFailureRetryInterval) {
        this.limiterFailureRetryInterval = limiterFailureRetryInterval;
    }

    public enum ProduceMode {
        SAME_IDC, SAME_REGION, SAME_IDC_OR_REGION, OTHER
    }

    @Override
    public TopicConfig clone() {
        return CommonFastJsonUtils.toObject(CommonFastJsonUtils.toJsonString(this), new TypeReference<TopicConfig>() {
        });
    }

    @Override
    public String toString() {
        return "TopicConfig{" +
                "topic='" + topic + '\'' +
                ", alarmGroup=" + alarmGroup +
                ", topicUnits=" + topicUnits +
                ", delayTopic=" + delayTopic +
                ", autoBatch=" + autoBatch +
                ", strongOrder=" + strongOrder +
                ", compressType=" + compressType +
                ", limiterFailureRetryInterval=" + limiterFailureRetryInterval +
                '}';
    }

    @Override
    public boolean validate() throws ConfigException {
        if (StringUtils.isEmpty(this.topic)) {
            throw new ConfigException("[TopicConfig] topic empty, topic=" + topic);
        } else if (CollectionUtils.isEmpty(this.topicUnits)) {
            throw new ConfigException("[TopicConfig] topicUnits empty, topic=" + topic);
        } else if (!topicUnits.stream().allMatch(TopicConfiguration::validate)) {
            throw new ConfigException("[TopicConfig] topicUnits error, topic=" + topic);
        }

        return true;
    }

}