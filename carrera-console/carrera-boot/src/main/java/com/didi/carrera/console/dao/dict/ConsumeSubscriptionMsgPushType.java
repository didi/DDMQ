package com.didi.carrera.console.dao.dict;

import java.util.Arrays;
import java.util.List;


public enum ConsumeSubscriptionMsgPushType {

    FORM_PARAMS((byte)1, "Post Body:params=<msg>"),
    FORM_PARAMS2((byte)2, "Post Body:flatten message's first layer to key=value");

    private byte index;

    private String name;

    ConsumeSubscriptionMsgPushType(byte index, String name) {
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

    public static ConsumeSubscriptionMsgPushType getByIndex(byte index) {
        List<ConsumeSubscriptionMsgPushType> all = Arrays.asList(values());
        for (ConsumeSubscriptionMsgPushType level : all) {
            if (level.getIndex() == index) {
                return level;
            }
        }
        return null;
    }

}