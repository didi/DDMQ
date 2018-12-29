package com.didi.carrera.console.service.vo;


public class TopicStateVo {

    private Long topicId;

    private Long clusterId;

    private String topicName;

    private String qid;

    private Long minOffset;

    private Long maxOffset;

    private String lastUpdateTime;

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

    public String getTopicName() {
        return topicName;
    }

    public void setTopicName(String topicName) {
        this.topicName = topicName;
    }

    public String getQid() {
        return qid;
    }

    public void setQid(String qid) {
        this.qid = qid;
    }

    public Long getMinOffset() {
        return minOffset;
    }

    public void setMinOffset(Long minOffset) {
        this.minOffset = minOffset;
    }

    public Long getMaxOffset() {
        return maxOffset;
    }

    public void setMaxOffset(Long maxOffset) {
        this.maxOffset = maxOffset;
    }

    public String getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(String lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }

    @Override
    public String toString() {
        return "TopicStateVo{" +
                "topicId=" + topicId +
                ", clusterId=" + clusterId +
                ", topicName='" + topicName + '\'' +
                ", qid='" + qid + '\'' +
                ", minOffset=" + minOffset +
                ", maxOffset=" + maxOffset +
                ", lastUpdateTime='" + lastUpdateTime + '\'' +
                '}';
    }
}