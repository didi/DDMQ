package com.xiaojukeji.chronos.metrics;


public enum MetricMsgAction {
    UNKNOWN("unknown"),
    ADD("add"),
    ADD_INNER("add_inner"),
    CANCEL("cancel"),
    CANCEL_INNER("cancel_inner");

    private String value;

    MetricMsgAction(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}