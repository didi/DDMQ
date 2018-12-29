package com.xiaojukeji.chronos.metrics;


public enum MetricPushMsgResult {
    OK("ok"), FAIL("fail"), BACKUP("backup");

    private String value;

    MetricPushMsgResult(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}