package com.didi.carrera.console.service.vo;

import java.util.Date;


public class TopicListGroupVo {

    private Long groupId;

    private String groupName;

    private String contacters;

    private Date createTime;

    private Byte state;

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

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Byte getState() {
        return state;
    }

    public void setState(Byte state) {
        this.state = state;
    }

    @Override
    public String toString() {
        return "TopicListGroupVo{" +
                "groupId=" + groupId +
                ", groupName='" + groupName + '\'' +
                ", contacters='" + contacters + '\'' +
                ", createTime=" + createTime +
                ", state=" + state +
                '}';
    }
}