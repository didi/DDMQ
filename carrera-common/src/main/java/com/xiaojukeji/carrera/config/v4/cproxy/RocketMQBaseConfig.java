package com.xiaojukeji.carrera.config.v4.cproxy;

import java.util.Map;

import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.apache.rocketmq.common.protocol.heartbeat.MessageModel;


public abstract class RocketMQBaseConfig extends MQServerConfiguration implements Cloneable {

    protected Integer pollNameServerInterval;

    protected Integer heartbeatBrokerInterval;

    protected Integer persistConsumerOffsetInterval;

    protected Integer consumeConcurrentlyMaxSpan;

    protected Integer consumeMessageBatchMaxSize;

    protected Integer pullBatchSize;

    protected ConsumeFromWhere consumeFromWhere;

    protected String consumeTimestamp;

    protected Boolean orderly;

    protected MessageModel messageModel;

    protected Map<String, String> subscription;

    protected Integer consumeThreadMax;

    protected Integer consumeThreadMin;

    protected String instanceName;

    protected Integer pullThresholdForQueue;

    protected Integer pullThresholdSizeForQueue;

    protected Integer pullThresholdForTopic;

    protected Integer pullThresholdSizeForTopic;

    public Integer getPollNameServerInterval() {
        return pollNameServerInterval;
    }

    public void setPollNameServerInterval(Integer pollNameServerInterval) {
        this.pollNameServerInterval = pollNameServerInterval;
    }

    public Integer getHeartbeatBrokerInterval() {
        return heartbeatBrokerInterval;
    }

    public void setHeartbeatBrokerInterval(Integer heartbeatBrokerInterval) {
        this.heartbeatBrokerInterval = heartbeatBrokerInterval;
    }

    public Integer getPersistConsumerOffsetInterval() {
        return persistConsumerOffsetInterval;
    }

    public void setPersistConsumerOffsetInterval(Integer persistConsumerOffsetInterval) {
        this.persistConsumerOffsetInterval = persistConsumerOffsetInterval;
    }

    public Integer getConsumeConcurrentlyMaxSpan() {
        return consumeConcurrentlyMaxSpan;
    }

    public void setConsumeConcurrentlyMaxSpan(Integer consumeConcurrentlyMaxSpan) {
        this.consumeConcurrentlyMaxSpan = consumeConcurrentlyMaxSpan;
    }

    public Integer getConsumeMessageBatchMaxSize() {
        return consumeMessageBatchMaxSize;
    }

    public void setConsumeMessageBatchMaxSize(Integer consumeMessageBatchMaxSize) {
        this.consumeMessageBatchMaxSize = consumeMessageBatchMaxSize;
    }

    public Integer getPullBatchSize() {
        return pullBatchSize;
    }

    public void setPullBatchSize(Integer pullBatchSize) {
        this.pullBatchSize = pullBatchSize;
    }

    public ConsumeFromWhere getConsumeFromWhere() {
        return consumeFromWhere;
    }

    public void setConsumeFromWhere(ConsumeFromWhere consumeFromWhere) {
        this.consumeFromWhere = consumeFromWhere;
    }

    public String getConsumeTimestamp() {
        return consumeTimestamp;
    }

    public void setConsumeTimestamp(String consumeTimestamp) {
        this.consumeTimestamp = consumeTimestamp;
    }

    public Boolean getOrderly() {
        return orderly;
    }

    public void setOrderly(Boolean orderly) {
        this.orderly = orderly;
    }

    public MessageModel getMessageModel() {
        return messageModel;
    }

    public void setMessageModel(MessageModel messageModel) {
        this.messageModel = messageModel;
    }

    public Map<String, String> getSubscription() {
        return subscription;
    }

    public void setSubscription(Map<String, String> subscription) {
        this.subscription = subscription;
    }

    public Integer getConsumeThreadMax() {
        return consumeThreadMax;
    }

    public void setConsumeThreadMax(Integer consumeThreadMax) {
        this.consumeThreadMax = consumeThreadMax;
    }

    public Integer getConsumeThreadMin() {
        return consumeThreadMin;
    }

    public void setConsumeThreadMin(Integer consumeThreadMin) {
        this.consumeThreadMin = consumeThreadMin;
    }

    public String getInstanceName() {
        return instanceName;
    }

    public void setInstanceName(String instanceName) {
        this.instanceName = instanceName;
    }

    public Integer getPullThresholdForQueue() {
        return pullThresholdForQueue;
    }

    public void setPullThresholdForQueue(Integer pullThresholdForQueue) {
        this.pullThresholdForQueue = pullThresholdForQueue;
    }

    public Integer getPullThresholdSizeForQueue() {
        return pullThresholdSizeForQueue;
    }

    public void setPullThresholdSizeForQueue(Integer pullThresholdSizeForQueue) {
        this.pullThresholdSizeForQueue = pullThresholdSizeForQueue;
    }

    public Integer getPullThresholdForTopic() {
        return pullThresholdForTopic;
    }

    public void setPullThresholdForTopic(Integer pullThresholdForTopic) {
        this.pullThresholdForTopic = pullThresholdForTopic;
    }

    public Integer getPullThresholdSizeForTopic() {
        return pullThresholdSizeForTopic;
    }

    public void setPullThresholdSizeForTopic(Integer pullThresholdSizeForTopic) {
        this.pullThresholdSizeForTopic = pullThresholdSizeForTopic;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        RocketMQBaseConfig that = (RocketMQBaseConfig) o;

        if (pollNameServerInterval != null ? !pollNameServerInterval.equals(that.pollNameServerInterval) : that.pollNameServerInterval != null)
            return false;
        if (heartbeatBrokerInterval != null ? !heartbeatBrokerInterval.equals(that.heartbeatBrokerInterval) : that.heartbeatBrokerInterval != null)
            return false;
        if (persistConsumerOffsetInterval != null ? !persistConsumerOffsetInterval.equals(that.persistConsumerOffsetInterval) : that.persistConsumerOffsetInterval != null)
            return false;
        if (consumeConcurrentlyMaxSpan != null ? !consumeConcurrentlyMaxSpan.equals(that.consumeConcurrentlyMaxSpan) : that.consumeConcurrentlyMaxSpan != null)
            return false;
        if (consumeMessageBatchMaxSize != null ? !consumeMessageBatchMaxSize.equals(that.consumeMessageBatchMaxSize) : that.consumeMessageBatchMaxSize != null)
            return false;
        if (pullBatchSize != null ? !pullBatchSize.equals(that.pullBatchSize) : that.pullBatchSize != null)
            return false;
        if (consumeFromWhere != that.consumeFromWhere) return false;
        if (consumeTimestamp != null ? !consumeTimestamp.equals(that.consumeTimestamp) : that.consumeTimestamp != null)
            return false;
        if (orderly != null ? !orderly.equals(that.orderly) : that.orderly != null) return false;
        if (messageModel != that.messageModel) return false;
        if (subscription != null ? !subscription.equals(that.subscription) : that.subscription != null) return false;
        if (consumeThreadMax != null ? !consumeThreadMax.equals(that.consumeThreadMax) : that.consumeThreadMax != null)
            return false;
        if (consumeThreadMin != null ? !consumeThreadMin.equals(that.consumeThreadMin) : that.consumeThreadMin != null)
            return false;
        if (instanceName != null ? !instanceName.equals(that.instanceName) : that.instanceName != null) return false;
        if (pullThresholdForQueue != null ? !pullThresholdForQueue.equals(that.pullThresholdForQueue) : that.pullThresholdForQueue != null)
            return false;
        if (pullThresholdSizeForQueue != null ? !pullThresholdSizeForQueue.equals(that.pullThresholdSizeForQueue) : that.pullThresholdSizeForQueue != null)
            return false;
        if (pullThresholdForTopic != null ? !pullThresholdForTopic.equals(that.pullThresholdForTopic) : that.pullThresholdForTopic != null)
            return false;
        return pullThresholdSizeForTopic != null ? pullThresholdSizeForTopic.equals(that.pullThresholdSizeForTopic) : that.pullThresholdSizeForTopic == null;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (pollNameServerInterval != null ? pollNameServerInterval.hashCode() : 0);
        result = 31 * result + (heartbeatBrokerInterval != null ? heartbeatBrokerInterval.hashCode() : 0);
        result = 31 * result + (persistConsumerOffsetInterval != null ? persistConsumerOffsetInterval.hashCode() : 0);
        result = 31 * result + (consumeConcurrentlyMaxSpan != null ? consumeConcurrentlyMaxSpan.hashCode() : 0);
        result = 31 * result + (consumeMessageBatchMaxSize != null ? consumeMessageBatchMaxSize.hashCode() : 0);
        result = 31 * result + (pullBatchSize != null ? pullBatchSize.hashCode() : 0);
        result = 31 * result + (consumeFromWhere != null ? consumeFromWhere.hashCode() : 0);
        result = 31 * result + (consumeTimestamp != null ? consumeTimestamp.hashCode() : 0);
        result = 31 * result + (orderly != null ? orderly.hashCode() : 0);
        result = 31 * result + (messageModel != null ? messageModel.hashCode() : 0);
        result = 31 * result + (subscription != null ? subscription.hashCode() : 0);
        result = 31 * result + (consumeThreadMax != null ? consumeThreadMax.hashCode() : 0);
        result = 31 * result + (consumeThreadMin != null ? consumeThreadMin.hashCode() : 0);
        result = 31 * result + (instanceName != null ? instanceName.hashCode() : 0);
        result = 31 * result + (pullThresholdForQueue != null ? pullThresholdForQueue.hashCode() : 0);
        result = 31 * result + (pullThresholdSizeForQueue != null ? pullThresholdSizeForQueue.hashCode() : 0);
        result = 31 * result + (pullThresholdForTopic != null ? pullThresholdForTopic.hashCode() : 0);
        result = 31 * result + (pullThresholdSizeForTopic != null ? pullThresholdSizeForTopic.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "RocketMQBaseConfig{" +
                "pollNameServerInterval=" + pollNameServerInterval +
                ", heartbeatBrokerInterval=" + heartbeatBrokerInterval +
                ", persistConsumerOffsetInterval=" + persistConsumerOffsetInterval +
                ", consumeConcurrentlyMaxSpan=" + consumeConcurrentlyMaxSpan +
                ", consumeMessageBatchMaxSize=" + consumeMessageBatchMaxSize +
                ", pullBatchSize=" + pullBatchSize +
                ", consumeFromWhere=" + consumeFromWhere +
                ", consumeTimestamp='" + consumeTimestamp + '\'' +
                ", orderly=" + orderly +
                ", messageModel=" + messageModel +
                ", subscription=" + subscription +
                ", consumeThreadMax=" + consumeThreadMax +
                ", consumeThreadMin=" + consumeThreadMin +
                ", instanceName='" + instanceName + '\'' +
                ", pullThresholdForQueue=" + pullThresholdForQueue +
                ", pullThresholdSizeForQueue=" + pullThresholdSizeForQueue +
                ", pullThresholdForTopic=" + pullThresholdForTopic +
                ", pullThresholdSizeForTopic=" + pullThresholdSizeForTopic +
                '}';
    }
}