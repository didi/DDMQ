package com.didi.carrera.console.service.vo;


public class GroupConsumeStateVo {

    private Long groupId;

    private Long topicId;

    private String topicName;

    private Long clusterId;

    private String clusterName;

    private String qid;

    private Long minOffset;

    private Long maxOffset;

    private Long consumeOffset;

    private Long lag;

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

    public String getTopicName() {
        return topicName;
    }

    public void setTopicName(String topicName) {
        this.topicName = topicName;
    }

    public Long getClusterId() {
        return clusterId;
    }

    public void setClusterId(Long clusterId) {
        this.clusterId = clusterId;
    }

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
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

    public Long getConsumeOffset() {
        return consumeOffset;
    }

    public void setConsumeOffset(Long consumeOffset) {
        this.consumeOffset = consumeOffset;
    }

    public Long getLag() {
        return lag;
    }

    public void setLag(Long lag) {
        this.lag = lag;
    }

    @Override
    public String toString() {
        return "GroupConsumeStateVo{" +
                "groupId=" + groupId +
                ", topicId=" + topicId +
                ", topicName='" + topicName + '\'' +
                ", clusterId=" + clusterId +
                ", clusterName='" + clusterName + '\'' +
                ", qid='" + qid + '\'' +
                ", minOffset=" + minOffset +
                ", maxOffset=" + maxOffset +
                ", consumeOffset=" + consumeOffset +
                ", lag=" + lag +
                '}';
    }
}