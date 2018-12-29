package com.didi.carrera.console.data;


public class Message {

    private String tags;
    private String keys;
    private Integer storeSize;
    private Long bornTimestamp;

    private String qid;
    private long offset;
    private String body;

    public Message(String qid, long offset, String body, String tags, String keys, Integer storeSize, Long bornTimestamp) {
        this.tags = tags;
        this.keys = keys;
        this.storeSize = storeSize;
        this.bornTimestamp = bornTimestamp;
        this.qid = qid;
        this.offset = offset;
        this.body = body;
    }

    public String getQid() {
        return qid;
    }

    public void setQid(String qid) {
        this.qid = qid;
    }

    public long getOffset() {
        return offset;
    }

    public void setOffset(long offset) {
        this.offset = offset;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public String getKeys() {
        return keys;
    }

    public void setKeys(String keys) {
        this.keys = keys;
    }

    public Integer getStoreSize() {
        return storeSize;
    }

    public void setStoreSize(Integer storeSize) {
        this.storeSize = storeSize;
    }

    public Long getBornTimestamp() {
        return bornTimestamp;
    }

    public void setBornTimestamp(Long bornTimestamp) {
        this.bornTimestamp = bornTimestamp;
    }

    @Override
    public String toString() {
        return super.toString();
    }
}