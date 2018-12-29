package com.xiaojukeji.chronos.model;


import com.xiaojukeji.carrera.chronos.model.InternalKey;


public class InternalPair {
    private InternalKey internalKey;
    private InternalValue internalValue;

    public InternalPair(InternalKey internalKey, InternalValue internalValue) {
        this.internalKey = internalKey;
        this.internalValue = internalValue;
    }

    public InternalKey getInternalKey() {
        return internalKey;
    }

    public void setInternalKey(InternalKey internalKey) {
        this.internalKey = internalKey;
    }

    public InternalValue getInternalValue() {
        return internalValue;
    }

    public void setInternalValue(InternalValue internalValue) {
        this.internalValue = internalValue;
    }

    @Override
    public String toString() {
        return "InternalPair{" +
                "internalKey=" + internalKey +
                ", internalValue=" + internalValue +
                '}';
    }
}