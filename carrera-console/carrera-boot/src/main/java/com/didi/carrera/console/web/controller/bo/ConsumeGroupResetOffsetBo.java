package com.didi.carrera.console.web.controller.bo;

import javax.validation.constraints.NotNull;
import java.util.Date;

import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.Range;


public class ConsumeGroupResetOffsetBo {
    @NotBlank(message = "当前登录用户不能为空")
    private String user;

    @NotNull(message = "消费组Id不能为空")
    private Long groupId;

    @NotNull(message = "topicId不能为空")
    private Long topicId;

    @NotNull(message = "集群Id不能为空")
    private Long clusterId;

    @NotNull(message = "重置类型不能为空")
    @Range(min = 1, max = 3, message = "重置类型只能为1-3")
    private Integer resetType;

    private Date resetTime;

    private String qid;

    private Long offset;

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
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

    public Integer getResetType() {
        return resetType;
    }

    public void setResetType(Integer resetType) {
        this.resetType = resetType;
    }

    public Date getResetTime() {
        return resetTime;
    }

    public void setResetTime(Date resetTime) {
        this.resetTime = resetTime;
    }

    public String getQid() {
        return qid;
    }

    public void setQid(String qid) {
        this.qid = qid;
    }

    public Long getOffset() {
        return offset;
    }

    public void setOffset(Long offset) {
        this.offset = offset;
    }

    @Override
    public String toString() {
        return "ConsumeGroupResetOffsetBo{" +
                "user='" + user + '\'' +
                ", groupId=" + groupId +
                ", topicId=" + topicId +
                ", clusterId=" + clusterId +
                ", resetType=" + resetType +
                ", resetTime=" + resetTime +
                ", qid='" + qid + '\'' +
                ", offset=" + offset +
                '}';
    }
}