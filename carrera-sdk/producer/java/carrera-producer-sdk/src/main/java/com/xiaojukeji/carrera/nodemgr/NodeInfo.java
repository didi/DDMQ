package com.xiaojukeji.carrera.nodemgr;


public class NodeInfo {

    private String host;
    private boolean healthy;
    private long startCooldownTime;

    public NodeInfo(String host) {
        this.host = host;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public boolean isHealthy() {
        return healthy;
    }

    public void setHealthy(boolean healthy) {
        this.healthy = healthy;
    }

    public long getStartCooldownTime() {
        return startCooldownTime;
    }

    public void setStartCooldownTime(long startCooldownTime) {
        this.startCooldownTime = startCooldownTime;
    }

    @Override
    public String toString() {
        return "NodeInfo{" +
                "host='" + host + '\'' +
                ", healthy=" + healthy +
                ", startCooldownTime=" + startCooldownTime +
                '}';
    }

}