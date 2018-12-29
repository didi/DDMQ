package com.xiaojukeji.carrera.chronos.enums;


public enum Actions {

    UNKNOWN(0),
    ADD(1),
    CANCEL(2);

    Actions(int val) {
        this.value = val;
    }

    public int getValue() {
        return this.value;
    }

    private int value;
}