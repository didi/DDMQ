package com.xiaojukeji.carrera.config.v4;

import java.util.Set;


public class MonitorAssignedConfig {
    private Set<String> brokers;

    public Set<String> getBrokers() {
        return brokers;
    }

    public void setBrokers(Set<String> brokers) {
        this.brokers = brokers;
    }

    @Override
    public String toString() {
        return "MonitorAssignedConfig{" +
                "brokers=" + brokers +
                '}';
    }
}