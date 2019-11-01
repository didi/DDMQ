package com.xiaojukeji.carrera.metric;


import com.alibaba.fastjson.annotation.JSONField;
import com.xiaojukeji.carrera.utils.CommonUtils;

import java.util.HashMap;
import java.util.Map;


public class Metric {

    @JSONField(name = "metric")
    private String name;

    private String endpoint;

    private long timestamp;

    private long value;

    @JSONField(name = "tagsMap")
    private Map<String, String> tags = new HashMap<>();

    private long step;

    private String counterType = "GAUGE";

    public Metric(String name, String host, long value, long step) {
        long current = System.currentTimeMillis() / 1000;
        this.timestamp = current - current % step;
        this.name = name;
        this.value = value;
        this.step = step;
        this.endpoint = CommonUtils.getHostAddress();
        setTag("host", host);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public long getValue() {
        return value;
    }

    public void setValue(long value) {
        this.value = value;
    }

    public Map<String, String> getTags() {
        return tags;
    }

    public void setTags(Map<String, String> tags) {
        this.tags = tags;
    }

    public long getStep() {
        return step;
    }

    public void setStep(long step) {
        this.step = step;
    }

    public void setTag(String tag, String value) {
        tags.put(tag, value);
    }

    public String getCounterType() {
        return counterType;
    }

    public void setCounterType(String counterType) {
        this.counterType = counterType;
    }

    @Override
    public String toString() {
        return "Metric{" +
                "name='" + name + '\'' +
                ", endpoint='" + endpoint + '\'' +
                ", timestamp=" + timestamp +
                ", value=" + value +
                ", tags=" + tags +
                ", step=" + step +
                ", counterType='" + counterType + '\'' +
                '}';
    }
}