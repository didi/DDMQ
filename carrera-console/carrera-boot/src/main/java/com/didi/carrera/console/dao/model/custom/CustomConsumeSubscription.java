package com.didi.carrera.console.dao.model.custom;

import java.io.Serializable;
import java.util.Date;


public class CustomConsumeSubscription implements Serializable {

    private Long groupId;
    private String groupName;
    private String contacters;
    private Long topicId;
    private Long clusterId;
    private Byte state;
    private Date createTime;

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getContacters() {
        return contacters;
    }

    public void setContacters(String contacters) {
        this.contacters = contacters;
    }

    public Long getTopicId() {
        return topicId;
    }

    public void setTopicId(Long topicId) {
        this.topicId = topicId;
    }

    public Long getClusterId() {
        return clusterId;
    }

    public void setClusterId(Long clusterId) {
        this.clusterId = clusterId;
    }

    public Byte getState() {
        return state;
    }

    public void setState(Byte state) {
        this.state = state;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    @Override
    public String toString() {
        return "CustomConsumeSubscription{" +
                "groupId=" + groupId +
                ", groupName='" + groupName + '\'' +
                ", contacters='" + contacters + '\'' +
                ", topicId=" + topicId +
                ", clusterId=" + clusterId +
                ", state=" + state +
                ", createTime=" + createTime +
                '}';
    }
}