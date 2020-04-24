package com.didi.carrera.console.dao.dict;

import java.util.Arrays;
import java.util.List;


public enum  TopicCompressionType {

    RMQ_COMPRESSION((byte)0, "RMQ"),
    SNAPPY_COMPRESSION((byte)1, "Snappy");

    private byte index;

    private String name;

    TopicCompressionType(byte index, String name) {
        this.index = index;
        this.name = name;
    }

    public byte getIndex() {
        return index;
    }

    public void setIndex(byte index) {
        this.index = index;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public static TopicCompressionType getByIndex(byte index) {
        List<TopicCompressionType> all = Arrays.asList(values());
        for (TopicCompressionType level : all) {
            if (level.getIndex() == index) {
                return level;
            }
        }
        return null;
    }
}