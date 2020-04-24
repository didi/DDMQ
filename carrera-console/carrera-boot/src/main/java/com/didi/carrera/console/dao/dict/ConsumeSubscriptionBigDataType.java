package com.didi.carrera.console.dao.dict;

import java.util.Arrays;
import java.util.List;


public enum ConsumeSubscriptionBigDataType {
    HDFS((byte)0, "hdfs"),
    HBASE((byte)1, "hbase"),
    REDIS((byte)2, "redis");

    private byte index;

    private String name;

    ConsumeSubscriptionBigDataType(byte index, String name) {
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

    public static ConsumeSubscriptionBigDataType getByIndex(byte index) {
        List<ConsumeSubscriptionBigDataType> all = Arrays.asList(values());
        for (ConsumeSubscriptionBigDataType level : all) {
            if (level.getIndex() == index) {
                return level;
            }
        }
        return null;
    }
}