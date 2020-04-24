package com.didi.carrera.console.dao.dict;

import java.util.Arrays;
import java.util.List;


public enum IsEnable {
    ENABLE((byte)0, "enable"),
    DISABLE((byte)1, "disable");

    private byte index;

    private String name;

    IsEnable(byte index, String name) {
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

    public static IsEnable getByIndex(byte index) {
        List<IsEnable> all = Arrays.asList(values());
        for (IsEnable level : all) {
            if (level.getIndex() == index) {
                return level;
            }
        }
        return null;
    }

    public static boolean isEnable(Byte index) {
        if(index == null) {
            return false;
        }

        return index == ENABLE.index;
    }

}