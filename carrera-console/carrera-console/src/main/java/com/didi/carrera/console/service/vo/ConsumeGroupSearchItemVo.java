package com.didi.carrera.console.service.vo;

import java.util.List;


public class ConsumeGroupSearchItemVo {

    private List<SearchItemVo> cluster;
    private List<SearchItemVo> topic;

    public List<SearchItemVo> getCluster() {
        return cluster;
    }

    public void setCluster(List<SearchItemVo> cluster) {
        this.cluster = cluster;
    }

    public List<SearchItemVo> getTopic() {
        return topic;
    }

    public void setTopic(List<SearchItemVo> topic) {
        this.topic = topic;
    }

    @Override
    public String toString() {
        return "ConsumeGroupSearchItemVo{" +
                "cluster=" + cluster +
                ", topic=" + topic +
                '}';
    }
}