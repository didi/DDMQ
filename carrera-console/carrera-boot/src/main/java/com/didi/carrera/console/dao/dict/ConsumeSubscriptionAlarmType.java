package com.didi.carrera.console.dao.dict;

import java.util.Arrays;
import java.util.List;


public enum ConsumeSubscriptionAlarmType {
    EXTEND_GROUP_CONFIG((byte)0, "Same as group alarm configuration"),
    SEPARATE_CONFIG((byte)1, "own alarm configuration");

    private byte index;

    private String name;

    ConsumeSubscriptionAlarmType(byte index, String name) {
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

    public static ConsumeSubscriptionAlarmType getByIndex(byte index) {
        List<ConsumeSubscriptionAlarmType> all = Arrays.asList(values());
        for (ConsumeSubscriptionAlarmType level : all) {
            if (level.getIndex() == index) {
                return level;
            }
        }
        return null;
    }

}