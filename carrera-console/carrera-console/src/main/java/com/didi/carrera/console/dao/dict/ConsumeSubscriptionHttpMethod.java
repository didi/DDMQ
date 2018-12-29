package com.didi.carrera.console.dao.dict;

import java.util.Arrays;
import java.util.List;


public enum ConsumeSubscriptionHttpMethod {
    POST((byte)0, "POST"),
    GET((byte)1, "GET");

    private byte index;

    private String name;

    ConsumeSubscriptionHttpMethod(byte index, String name) {
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

    public static ConsumeSubscriptionHttpMethod getByIndex(byte index) {
        List<ConsumeSubscriptionHttpMethod> all = Arrays.asList(values());
        for (ConsumeSubscriptionHttpMethod level : all) {
            if (level.getIndex() == index) {
                return level;
            }
        }
        return null;
    }

}