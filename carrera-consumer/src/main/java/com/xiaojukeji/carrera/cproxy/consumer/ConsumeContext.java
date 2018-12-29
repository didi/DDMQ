package com.xiaojukeji.carrera.cproxy.consumer;

import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.common.message.MessageQueue;

import java.util.Map;


public class ConsumeContext {

    public enum MessageSource {KAFKA, RMQ}

    private String groupId;
    private long startTime;
    private MessageSource source; // RMQ or KAFKA
    private long offset;
    private int partitionId; //for kafka
    private MessageQueue messageQueue; // for RMQ
    private MessageExt originMessage; //for RMQ
    private String qid;
    private Map<String, String> properties; //for RMQ

    public ConsumeContext(MessageSource source, String groupId) {
        this.source = source;
        this.groupId = groupId;
    }

    public MessageExt getOriginMessage() {
        return originMessage;
    }

    public void setOriginMessage(MessageExt originMessage) {
        this.originMessage = originMessage;
    }

    public int getPartitionId() {
        return partitionId;
    }

    public void setPartitionId(int partitionId) {
        this.partitionId = partitionId;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public long getOffset() {
        return offset;
    }

    public void setOffset(long offset) {
        this.offset = offset;
    }

    public MessageQueue getMessageQueue() {
        return messageQueue;
    }

    public void setMessageQueue(MessageQueue messageQueue) {
        this.messageQueue = messageQueue;
    }

    public MessageSource getSource() {
        return source;
    }

    public void setSource(MessageSource source) {
        this.source = source;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public String getQid() {
        return qid;
    }

    public void setQid(String qid) {
        this.qid = qid;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ConsumeContext that = (ConsumeContext) o;

        if (startTime != that.startTime) return false;
        if (offset != that.offset) return false;
        if (partitionId != that.partitionId) return false;
        if (groupId != null ? !groupId.equals(that.groupId) : that.groupId != null) return false;
        if (source != that.source) return false;
        if (messageQueue != null ? !messageQueue.equals(that.messageQueue) : that.messageQueue != null) return false;
        if (originMessage != null ? !originMessage.equals(that.originMessage) : that.originMessage != null)
            return false;
        if (qid != null ? !qid.equals(that.qid) : that.qid != null) return false;
        return properties != null ? properties.equals(that.properties) : that.properties == null;
    }

    @Override
    public int hashCode() {
        int result = groupId != null ? groupId.hashCode() : 0;
        result = 31 * result + (int) (startTime ^ (startTime >>> 32));
        result = 31 * result + (source != null ? source.hashCode() : 0);
        result = 31 * result + (int) (offset ^ (offset >>> 32));
        result = 31 * result + partitionId;
        result = 31 * result + (messageQueue != null ? messageQueue.hashCode() : 0);
        result = 31 * result + (originMessage != null ? originMessage.hashCode() : 0);
        result = 31 * result + (qid != null ? qid.hashCode() : 0);
        result = 31 * result + (properties != null ? properties.hashCode() : 0);
        return result;
    }

    public String info() {
        return "{" +
                "groupId='" + groupId + '\'' +
                ", source=" + source +
                ", qid=" + getQid() +
                ", offset=" + offset +
                ", properties=" + properties +
                '}';
    }

    @Override
    public String toString() {
        return "ConsumeContext{" +
                "groupId='" + groupId + '\'' +
                ", startTime=" + startTime +
                ", source=" + source +
                ", offset=" + offset +
                ", partitionId=" + partitionId +
                ", messageQueue=" + messageQueue +
                ", originMessage=" + originMessage +
                ", qid='" + qid + '\'' +
                ", properties=" + properties +
                '}';
    }
}