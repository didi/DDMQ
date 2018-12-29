package com.xiaojukeji.chronos.model;

import com.xiaojukeji.chronos.utils.JsonUtils;


public class CancelWrap {
    private String uniqDelayMsgId;
    private String topic;

    public CancelWrap() {
    }

    public CancelWrap(String uniqDelayMsgId, String topic) {
        this.uniqDelayMsgId = uniqDelayMsgId;
        this.topic = topic;
    }

    public String getUniqDelayMsgId() {
        return uniqDelayMsgId;
    }

    public void setUniqDelayMsgId(String uniqDelayMsgId) {
        this.uniqDelayMsgId = uniqDelayMsgId;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String toJsonString() {
        return JsonUtils.toJsonString(this);
    }

    @Override
    public String toString() {
        return "CancelWrap{" +
                "uniqDelayMsgId='" + uniqDelayMsgId + '\'' +
                ", topic='" + topic + '\'' +
                '}';
    }
}