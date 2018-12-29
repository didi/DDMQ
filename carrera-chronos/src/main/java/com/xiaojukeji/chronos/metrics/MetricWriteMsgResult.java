package com.xiaojukeji.chronos.metrics;


public enum MetricWriteMsgResult {
    OK("ok"), FAIL("fail");

    private String value;

    MetricWriteMsgResult(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}