package com.xiaojukeji.carrera.cproxy.consumer.offset;


public class OffsetTrackSnapshot {
    private String group;
    private String mqServer;
    private String topic;
    private String qid;
    private long maxOffset;
    private long startConsumeOffset;
    private long finishConsumeOffset;
    private long maxCommittableOffset;
    private long committedOffset;

    public OffsetTrackSnapshot(String group, String mqServer, String topic, String qid, long maxOffset, long startConsumeOffset, long finishConsumeOffset, long maxCommittableOffset, long committedOffset) {
        this.group = group;
        this.mqServer = mqServer;
        this.topic = topic;
        this.qid = qid;
        this.maxOffset = maxOffset;
        this.startConsumeOffset = startConsumeOffset;
        this.finishConsumeOffset = finishConsumeOffset;
        this.maxCommittableOffset = maxCommittableOffset;
        this.committedOffset = committedOffset;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getMqServer() {
        return mqServer;
    }

    public void setMqServer(String mqServer) {
        this.mqServer = mqServer;
    }

    public long getMaxCommittableOffset() {
        return maxCommittableOffset;
    }

    public void setMaxCommittableOffset(long maxCommittableOffset) {
        this.maxCommittableOffset = maxCommittableOffset;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getQid() {
        return qid;
    }

    public void setQid(String qid) {
        this.qid = qid;
    }

    public long getMaxOffset() {
        return maxOffset;
    }

    public void setMaxOffset(long maxOffset) {
        this.maxOffset = maxOffset;
    }

    public long getStartConsumeOffset() {
        return startConsumeOffset;
    }

    public void setStartConsumeOffset(long startConsumeOffset) {
        this.startConsumeOffset = startConsumeOffset;
    }

    public long getFinishConsumeOffset() {
        return finishConsumeOffset;
    }

    public void setFinishConsumeOffset(long finishConsumeOffset) {
        this.finishConsumeOffset = finishConsumeOffset;
    }

    public long getCommittedOffset() {
        return committedOffset;
    }

    public void setCommittedOffset(long committedOffset) {
        this.committedOffset = committedOffset;
    }

    @Override
    public String toString() {
        return "OffsetTrackSnapshot{" +
                "group='" + group + '\'' +
                ", mqServer='" + mqServer + '\'' +
                ", topic='" + topic + '\'' +
                ", qid='" + qid + '\'' +
                ", maxOffset=" + maxOffset +
                ", startConsumeOffset=" + startConsumeOffset +
                ", finishConsumeOffset=" + finishConsumeOffset +
                ", maxCommittableOffset=" + maxCommittableOffset +
                ", committedOffset=" + committedOffset +
                '}';
    }
}