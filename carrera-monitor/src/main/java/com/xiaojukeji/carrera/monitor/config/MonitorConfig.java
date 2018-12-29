package com.xiaojukeji.carrera.monitor.config;


import com.xiaojukeji.carrera.config.ConfigurationValidator;

public class MonitorConfig implements ConfigurationValidator {

    private String zookeeperAddr;

    private String broker;

    public String getZookeeperAddr() {
        return zookeeperAddr;
    }

    public void setZookeeperAddr(String zookeeperAddr) {
        this.zookeeperAddr = zookeeperAddr;
    }

    public String getBroker() {
        return broker;
    }

    public void setBroker(String broker) {
        this.broker = broker;
    }

    @Override
    public String toString() {
        return "MonitorConfig{" +
                "zookeeperAddr='" + zookeeperAddr + '\'' +
                ", broker='" + broker + '\'' +
                '}';
    }

    @Override
    public boolean validate() throws ConfigException {
        return true;
    }
}
