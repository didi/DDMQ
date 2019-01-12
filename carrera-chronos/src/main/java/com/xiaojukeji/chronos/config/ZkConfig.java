package com.xiaojukeji.chronos.config;


import com.xiaojukeji.carrera.config.ConfigurationValidator;

public class ZkConfig implements ConfigurationValidator {
    private String zkAddrs;
    private int zkSessionTimeoutMs;
    private String masterPathPrefix;
    private String metaPathPrefix;
    private String offsetsProp;
    private String seekTimestampProp;
    private int baseSleepTimeMs;
    private int maxSleepMs;
    private int maxRetries;

    public String getZkAddrs() {
        return zkAddrs;
    }

    public void setZkAddrs(String zkAddrs) {
        this.zkAddrs = zkAddrs;
    }

    public int getZkSessionTimeoutMs() {
        return zkSessionTimeoutMs;
    }

    public void setZkSessionTimeoutMs(int zkSessionTimeoutMs) {
        this.zkSessionTimeoutMs = zkSessionTimeoutMs;
    }

    public String getMasterPathPrefix() {
        return masterPathPrefix;
    }

    public void setMasterPathPrefix(String masterPathPrefix) {
        this.masterPathPrefix = masterPathPrefix;
    }

    public String getMetaPathPrefix() {
        return metaPathPrefix;
    }

    public void setMetaPathPrefix(String metaPathPrefix) {
        this.metaPathPrefix = metaPathPrefix;
    }

    public String getOffsetsProp() {
        return offsetsProp;
    }

    public void setOffsetsProp(String offsetsProp) {
        this.offsetsProp = offsetsProp;
    }

    public String getSeekTimestampProp() {
        return seekTimestampProp;
    }

    public void setSeekTimestampProp(String seekTimestampProp) {
        this.seekTimestampProp = seekTimestampProp;
    }

    public int getBaseSleepTimeMs() {
        return baseSleepTimeMs;
    }

    public void setBaseSleepTimeMs(int baseSleepTimeMs) {
        this.baseSleepTimeMs = baseSleepTimeMs;
    }

    public int getMaxSleepMs() {
        return maxSleepMs;
    }

    public void setMaxSleepMs(int maxSleepMs) {
        this.maxSleepMs = maxSleepMs;
    }

    public int getMaxRetries() {
        return maxRetries;
    }

    public void setMaxRetries(int maxRetries) {
        this.maxRetries = maxRetries;
    }

    @Override
    public boolean validate() {
        return true;
    }

    @Override
    public String toString() {
        return "ZkConfig{" +
                "zkAddrs='" + zkAddrs + '\'' +
                ", zkSessionTimeoutMs=" + zkSessionTimeoutMs +
                ", masterPathPrefix='" + masterPathPrefix + '\'' +
                ", metaPathPrefix='" + metaPathPrefix + '\'' +
                ", offsetsProp='" + offsetsProp + '\'' +
                ", seekTimestampProp='" + seekTimestampProp + '\'' +
                ", baseSleepTimeMs=" + baseSleepTimeMs +
                ", maxSleepMs=" + maxSleepMs +
                ", maxRetries=" + maxRetries +
                '}';
    }
}