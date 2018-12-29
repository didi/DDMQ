package com.didi.carrera.console.dao.dict;

import java.util.Arrays;
import java.util.List;


public enum ConsumeSubscriptionConsumeType {
    SDK((byte)1, "SDK"),
    HTTP((byte)2, "HTTP"),
    BIG_DATA((byte)3, "Write straightly to other component");

    private byte index;

    private String name;

    ConsumeSubscriptionConsumeType(byte index, String name) {
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

    public static ConsumeSubscriptionConsumeType getByIndex(byte index) {
        List<ConsumeSubscriptionConsumeType> all = Arrays.asList(values());
        for (ConsumeSubscriptionConsumeType level : all) {
            if (level.getIndex() == index) {
                return level;
            }
        }
        return null;
    }
}