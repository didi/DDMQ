package com.xiaojukeji.carrera.chronos.model;

import java.util.Map;


public class BodyExt {
    private String topic;
    private String uniqDelayMsgId;
    private int action;
    private String body;
    private String tags;
    private Map<String, String> properties;

    public BodyExt() {
    }

    public String getTopic() {
        return topic;
    }

    public BodyExt setTopic(String topic) {
        this.topic = topic;
        return this;
    }

    public String getUniqDelayMsgId() {
        return uniqDelayMsgId;
    }

    public BodyExt setUniqDelayMsgId(String uniqDelayMsgId) {
        this.uniqDelayMsgId = uniqDelayMsgId;
        return this;
    }

    public int getAction() {
        return action;
    }

    public BodyExt setAction(int action) {
        this.action = action;
        return this;
    }

    public String getBody() {
        return body;
    }

    public BodyExt setBody(String body) {
        this.body = body;
        return this;
    }

    public String getTags() {
        return tags;
    }

    public BodyExt setTags(String tags) {
        this.tags = tags;
        return this;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public BodyExt setProperties(Map<String, String> properties) {
        this.properties = properties;
        return this;
    }

    @Override
    public String toString() {
        return "BodyExt{" +
                "topic='" + topic + '\'' +
                ", uniqDelayMsgId='" + uniqDelayMsgId + '\'' +
                ", action=" + action +
                ", body='" + body + '\'' +
                ", tags='" + tags + '\'' +
                ", properties=" + properties +
                '}';
    }
}