package com.xiaojukeji.chronos.config;

import com.xiaojukeji.carrera.config.ConfigurationValidator;

import java.util.List;


public class PushConfig implements ConfigurationValidator {
    private List<String> pproxyAddrs;
    private int proxyTimeoutMs;
    private int clientRetry;
    private int clientTimeoutMs;
    private int PoolSize;
    private int pushIntervalMs;
    private int batchSendThreadNum;

    public List<String> getPproxyAddrs() {
        return pproxyAddrs;
    }

    public void setPproxyAddrs(List<String> pproxyAddrs) {
        this.pproxyAddrs = pproxyAddrs;
    }

    public int getProxyTimeoutMs() {
        return proxyTimeoutMs;
    }

    public void setProxyTimeoutMs(int proxyTimeoutMs) {
        this.proxyTimeoutMs = proxyTimeoutMs;
    }

    public int getClientRetry() {
        return clientRetry;
    }

    public void setClientRetry(int clientRetry) {
        this.clientRetry = clientRetry;
    }

    public int getClientTimeoutMs() {
        return clientTimeoutMs;
    }

    public void setClientTimeoutMs(int clientTimeoutMs) {
        this.clientTimeoutMs = clientTimeoutMs;
    }

    public int getPoolSize() {
        return PoolSize;
    }

    public void setPoolSize(int poolSize) {
        PoolSize = poolSize;
    }

    public int getPushIntervalMs() {
        return pushIntervalMs;
    }

    public void setPushIntervalMs(int pushIntervalMs) {
        this.pushIntervalMs = pushIntervalMs;
    }

    public int getBatchSendThreadNum() {
        return batchSendThreadNum;
    }

    public void setBatchSendThreadNum(int batchSendThreadNum) {
        this.batchSendThreadNum = batchSendThreadNum;
    }

    @Override
    public boolean validate() {
        return true;
    }

    @Override
    public String toString() {
        return "PushConfig{" +
                "pproxyAddrs=" + pproxyAddrs +
                ", proxyTimeoutMs=" + proxyTimeoutMs +
                ", clientRetry=" + clientRetry +
                ", clientTimeoutMs=" + clientTimeoutMs +
                ", PoolSize=" + PoolSize +
                ", pushIntervalMs=" + pushIntervalMs +
                ", batchSendThreadNum=" + batchSendThreadNum +
                '}';
    }
}