package com.didi.carrera.console.dao.dict;

import java.util.Arrays;
import java.util.List;


public enum NodeType {

    ROCKETMQ_BROKER_MASTER((byte)1, "rocketmq broker master"),

    ROCKETMQ_NAMESERVER((byte)2, "rocketmq nameserver"),

    PRODUCER_PROXY((byte)3, "producer proxy"),

    CONSUMER_PROXY((byte)4, "consumer proxy"),

    ROCKETMQ_BROKER_SLAVE((byte)9, "rocketmq broker slave");

    private byte index;

    private String name;

    NodeType(byte index, String name) {
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

    public static NodeType getByIndex(byte index) {
        List<NodeType> all = Arrays.asList(values());
        for (NodeType item : all) {
            if (item.getIndex() == index) {
                return item;
            }
        }
        return null;
    }

}