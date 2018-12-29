package com.xiaojukeji.carrera.metric;

import java.util.concurrent.TimeUnit;

import com.xiaojukeji.carrera.utils.LogUtils;


public class ErrorMetrics {
    private static final int REPORT_INTERVAL_S = 10;

    private CounterMetric errorCounter;

    public ErrorMetrics() {
        errorCounter = MetricFactory.getCounterMetric("errorCounter", REPORT_INTERVAL_S, TimeUnit.SECONDS, LogUtils.METRIC_LOGGER, "error_type");
    }

    public void incErrorCount(String type) {
        errorCounter.inc(type);
    }

    public void shutDown() {
        errorCounter.shutDown();
    }
}