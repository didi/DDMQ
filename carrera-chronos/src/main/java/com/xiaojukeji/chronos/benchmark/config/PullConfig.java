package com.xiaojukeji.chronos.benchmark.config;


import com.xiaojukeji.carrera.config.ConfigurationValidator;

public class PullConfig implements ConfigurationValidator {
    private String group;
    private String cproxyAddrs;
    private int retryIntervalMs;
    private int maxBatchSize;
    private int threadNum;

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
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

    public int getThreadNum() {
        return threadNum;
    }

    public void setThreadNum(int threadNum) {
        this.threadNum = threadNum;
    }

    @Override
    public String toString() {
        return "PullConfig{" +
                "group='" + group + '\'' +
                ", cproxyAddrs='" + cproxyAddrs + '\'' +
                ", retryIntervalMs=" + retryIntervalMs +
                ", maxBatchSize=" + maxBatchSize +
                ", threadNum=" + threadNum +
                '}';
    }

    @Override
    public boolean validate() {
        return true;
    }
}