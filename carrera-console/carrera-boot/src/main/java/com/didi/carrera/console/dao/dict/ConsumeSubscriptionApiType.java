package com.didi.carrera.console.dao.dict;

import java.util.Arrays;
import java.util.List;


public enum ConsumeSubscriptionApiType {
    HIGH_LEVEL((byte)1, "highlevel"),
    LOW_LEVEL((byte)2, "lowlevel");

    private byte index;

    private String name;

    ConsumeSubscriptionApiType(byte index, String name) {
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

    public static ConsumeSubscriptionApiType getByIndex(byte index) {
        List<ConsumeSubscriptionApiType> all = Arrays.asList(values());
        for (ConsumeSubscriptionApiType level : all) {
            if (level.getIndex() == index) {
                return level;
            }
        }
        return null;
    }

}