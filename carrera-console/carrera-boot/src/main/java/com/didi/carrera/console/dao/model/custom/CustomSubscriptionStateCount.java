package com.didi.carrera.console.dao.model.custom;

import java.io.Serializable;


public class CustomSubscriptionStateCount implements Serializable {

    private Long groupId;
    private Integer state;
    private Integer count;

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    @Override
    public String toString() {
        return "CustomSubscriptionStateCount{" +
                "groupId=" + groupId +
                ", state=" + state +
                ", count=" + count +
                '}';
    }
}