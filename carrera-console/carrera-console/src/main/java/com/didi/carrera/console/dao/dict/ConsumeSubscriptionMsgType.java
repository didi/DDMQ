package com.didi.carrera.console.dao.dict;

import java.util.Arrays;
import java.util.List;


public enum ConsumeSubscriptionMsgType {
    JSON((byte)1, "JSON"),
    TEXT((byte)2, "Text"),
    BINARY((byte)3, "Binary");

    private byte index;

    private String name;

    ConsumeSubscriptionMsgType(byte index, String name) {
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

    public static ConsumeSubscriptionMsgType getByIndex(byte index) {
        List<ConsumeSubscriptionMsgType> all = Arrays.asList(values());
        for (ConsumeSubscriptionMsgType level : all) {
            if (level.getIndex() == index) {
                return level;
            }
        }
        return null;
    }

}