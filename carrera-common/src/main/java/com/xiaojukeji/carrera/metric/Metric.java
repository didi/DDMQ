package com.xiaojukeji.carrera.metric;


import java.util.HashMap;
import java.util.Map;


public class Metric {
    private String name;
    private long timestamp;
    private long value;
    private Map<String, String> tags = new HashMap<>();
    private long step;

    public Metric(String name, String host, long value, long step) {
        long current = System.currentTimeMillis() / 1000;
        this.timestamp = current - current % step;
        this.name = name;
        this.value = value;
        this.step = step;
        setTag("host", host);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    @Override
    public String toString() {
        return "Metric{" +
                "name='" + name + '\'' +
                ", timestamp=" + timestamp +
                ", value=" + value +
                ", tags=" + tags +
                ", step=" + step +
                '}';
    }
}