package com.xiaojukeji.chronos.config;


import com.xiaojukeji.carrera.config.ConfigurationValidator;

public class PullConfig implements ConfigurationValidator {

    private String innerGroup;
    private String innerTopic;
    private String cproxyAddrs;
    private int retryIntervalMs;
    private int maxBatchSize;
    private int timeoutMs;
    private int pullBatchItemNum;
    private int threadNum;
    private int msgByteBaseLen;

    public String getInnerGroup() {
        return innerGroup;
    }

    public void setInnerGroup(String innerGroup) {
        this.innerGroup = innerGroup;
    }

    public String getInnerTopic() {
        return innerTopic;
    }

    public void setInnerTopic(String innerTopic) {
        this.innerTopic = innerTopic;
    }

    public String getCproxyAddrs() {
        return cproxyAddrs;
    }

    public void setCproxyAddrs(String cproxyAddrs) {
        this.cproxyAddrs = cproxyAddrs;
    }

    public int getRetryIntervalMs() {
        return retryIntervalMs;
    }

    public void setRetryIntervalMs(int retryIntervalMs) {
        this.retryIntervalMs = retryIntervalMs;
    }

    public int getMaxBatchSize() {
        return maxBatchSize;
    }

    public void setMaxBatchSize(int maxBatchSize) {
        this.maxBatchSize = maxBatchSize;
    }

    public int getTimeoutMs() {
        return timeoutMs;
    }

    public void setTimeoutMs(int timeoutMs) {
        this.timeoutMs = timeoutMs;
    }

    public int getPullBatchItemNum() {
        return pullBatchItemNum;
    }

    public void setPullBatchItemNum(int pullBatchItemNum) {
        this.pullBatchItemNum = pullBatchItemNum;
    }

    public int getThreadNum() {
        return threadNum;
    }

    public void setThreadNum(int threadNum) {
        this.threadNum = threadNum;
    }

    public int getMsgByteBaseLen() {
        return msgByteBaseLen;
    }

    public void setMsgByteBaseLen(int msgByteBaseLen) {
        this.msgByteBaseLen = msgByteBaseLen;
    }

    @Override
    public boolean validate() {
        if (msgByteBaseLen < 100) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "PullConfig{" +
                "innerGroup='" + innerGroup + '\'' +
                ", innerTopic='" + innerTopic + '\'' +
                ", cproxyAddrs='" + cproxyAddrs + '\'' +
                ", retryIntervalMs=" + retryIntervalMs +
                ", maxBatchSize=" + maxBatchSize +
                ", timeoutMs=" + timeoutMs +
                ", pullBatchItemNum=" + pullBatchItemNum +
                ", threadNum=" + threadNum +
                ", msgByteBaseLen=" + msgByteBaseLen +
                '}';
    }
}