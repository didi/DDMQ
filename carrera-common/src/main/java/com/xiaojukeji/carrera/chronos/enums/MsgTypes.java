package com.xiaojukeji.carrera.chronos.enums;


public enum MsgTypes {
    TOMBSTONE(0),
    REAL_TIME(1),
    DELAY(2),
    LOOP_DELAY(3),
    LOOP_EXPONENT_DELAY(4);

    MsgTypes(int val) {
        this.value = val;
    }

    public int getValue() {
        return this.value;
    }

    private int value;
}