package com.didi.carrera.console.dao.dict;

import java.util.Arrays;
import java.util.List;


public enum ConsumeSubscriptionPressureTraffic {
    DISABLE((byte)0, "not consume pressure traffic"),
    ENABLE((byte)1, "consume pressure traffic");

    private byte index;

    private String name;

    ConsumeSubscriptionPressureTraffic(byte index, String name) {
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

    public static ConsumeSubscriptionPressureTraffic getByIndex(byte index) {
        List<ConsumeSubscriptionPressureTraffic> all = Arrays.asList(values());
        for (ConsumeSubscriptionPressureTraffic level : all) {
            if (level.getIndex() == index) {
                return level;
            }
        }
        return null;
    }

}