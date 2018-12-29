package com.xiaojukeji.chronos.metrics;


public enum MetricPullMsgResult {
    VALID("valid"), INVALID("invalid");

    private String value;

    MetricPullMsgResult(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}