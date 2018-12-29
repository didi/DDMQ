package com.didi.carrera.console.service.vo;


public class TopicMessageVo {

    private Long topicId;

    private String topicName;

    private Long clusterId;

    private String clusterName;

    private String qid;

    private Long offset;

    private String msg;

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

    public Long getOffset() {
        return offset;
    }

    public void setOffset(Long offset) {
        this.offset = offset;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    @Override
    public String toString() {
        return "TopicMessageVo{" +
                "topicId=" + topicId +
                ", topicName='" + topicName + '\'' +
                ", clusterId=" + clusterId +
                ", clusterName='" + clusterName + '\'' +
                ", qid='" + qid + '\'' +
                ", offset=" + offset +
                ", msg='" + msg + '\'' +
                '}';
    }
}