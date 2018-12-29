package com.didi.carrera.console.dao.dict;

import java.util.Arrays;
import java.util.List;


public enum IsDelete {
    NO((byte)0, "normal"),
    YES((byte)1, "deleted");

    private byte index;

    private String name;

    IsDelete(byte index, String name) {
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

    public static IsDelete getByIndex(byte index) {
        List<IsDelete> all = Arrays.asList(values());
        for (IsDelete level : all) {
            if (level.getIndex() == index) {
                return level;
            }
        }
        return null;
    }

}