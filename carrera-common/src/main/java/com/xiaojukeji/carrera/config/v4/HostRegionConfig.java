package com.xiaojukeji.carrera.config.v4;


public class HostRegionConfig {
    private String brokerCluster;
    private String idc;

    public String getBrokerCluster() {
        return brokerCluster;
    }

    public void setBrokerCluster(String brokerCluster) {
        this.brokerCluster = brokerCluster;
    }

    public String getIdc() {
        return idc;
    }

    public void setIdc(String idc) {
        this.idc = idc;
    }

    @Override
    public String toString() {
        return "HostRegionConfig{" +
                "brokerCluster='" + brokerCluster + '\'' +
                ", idc='" + idc + '\'' +
                '}';
    }
}