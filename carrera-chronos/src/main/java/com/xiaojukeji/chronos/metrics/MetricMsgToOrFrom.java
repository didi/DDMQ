package com.xiaojukeji.chronos.metrics;


public enum MetricMsgToOrFrom {
    UNKNOWN("unknown"),
    DB("db"),
    SEND("send");

    private String value;

    MetricMsgToOrFrom(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}