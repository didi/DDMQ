package com.didi.carrera.console.dao.dict;

import java.util.Arrays;
import java.util.List;


public enum MqServerType {
    ROCKETMQ((byte)0, "rocketmq"),
    KAFKA((byte)1, "kafka");

    private byte index;

    private String name;

    MqServerType(byte index, String name) {
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

    public static MqServerType getByIndex(byte index) {
        List<MqServerType> all = Arrays.asList(values());
        for (MqServerType type : all) {
            if (type.getIndex() == index) {
                return type;
            }
        }
        return null;
    }
}