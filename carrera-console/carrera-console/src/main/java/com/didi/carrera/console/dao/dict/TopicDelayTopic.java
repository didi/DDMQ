package com.didi.carrera.console.dao.dict;

import java.util.Arrays;
import java.util.List;


public enum TopicDelayTopic {
    DELAY_TOPIC((byte)0, "delay topic"),
    REAL_TIME_TOPIC((byte)1, "real-time topic");

    private byte index;

    private String name;

    TopicDelayTopic(byte index, String name) {
        this.index = index;
        this.name = name;
    }

    public byte getIndex() {
        return this.index;
    }

    public void setIndex(byte index) {
        this.index = index;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public static TopicDelayTopic getByIndex(byte index) {
        List<TopicDelayTopic> all = Arrays.asList(values());
        for (TopicDelayTopic level : all) {
            if (level.getIndex() == index) {
                return level;
            }
        }
        return null;
    }

}