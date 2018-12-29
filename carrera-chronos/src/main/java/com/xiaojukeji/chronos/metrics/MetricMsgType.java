package com.xiaojukeji.chronos.metrics;


public enum MetricMsgType {
    UNKNOWN("unknown"),
    DELAY("delay"),
    LOOP_DELAY("loop_delay"),
    LOOP_EXPONENT_DELAY("loop_exponent_delay");

    private String value;

    MetricMsgType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}