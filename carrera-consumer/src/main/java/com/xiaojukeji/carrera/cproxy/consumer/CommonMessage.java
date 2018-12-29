package com.xiaojukeji.carrera.cproxy.consumer;

import org.apache.commons.lang3.ArrayUtils;

import java.util.Arrays;


public class CommonMessage {
    private String topic;
    private String key;
    private byte[] value;

    public CommonMessage(String topic, String key, byte[] value) {
        this.topic = topic;
        this.key = key;
        this.value = value;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public byte[] getValue() {
        return value;
    }

    public void setValue(byte[] value) {
        this.value = value;
    }

    public String info() {
        return "{" +
                "topic='" + topic + '\'' +
                ", key='" + key + '\'' +
                ", value.length=" + ArrayUtils.getLength(value) +
                '}';
    }

    @Override
    public String toString() {
        return "CommonMessage{" +
                "topic='" + topic + '\'' +
                ", key='" + key + '\'' +
                ", value=" + Arrays.toString(value) +
                '}';
    }
}