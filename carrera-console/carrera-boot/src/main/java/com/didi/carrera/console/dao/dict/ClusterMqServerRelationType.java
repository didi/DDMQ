package com.didi.carrera.console.dao.dict;


public enum ClusterMqServerRelationType {
    P_PROXY((byte)0, "pproxy"),
    C_PROXY((byte)1, "cproxy");

    private byte index;

    private String name;

    ClusterMqServerRelationType(byte index, String name) {
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

    public static ClusterMqServerRelationType getByIndex(byte index) {
        ClusterMqServerRelationType[] all = values();
        for (ClusterMqServerRelationType type : all) {
            if (type.getIndex() == index) {
                return type;
            }
        }
        return null;
    }
}