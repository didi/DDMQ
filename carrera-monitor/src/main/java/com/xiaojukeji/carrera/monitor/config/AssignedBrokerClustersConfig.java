package com.xiaojukeji.carrera.monitor.config;


import com.xiaojukeji.carrera.config.ConfigurationValidator;

import java.util.List;


public class AssignedBrokerClustersConfig implements ConfigurationValidator {

    private List<String> brokers;

    public List<String> getBrokers() {
        return brokers;
    }

    public void setBrokers(List<String> brokers) {
        this.brokers = brokers;
    }

    @Override
    public String toString() {
        return "AssignedBrokerClustersConfig{" +
                "brokers=" + brokers +
                '}';
    }

    @Override
    public boolean validate() throws ConfigException {
        return true;
    }
}
