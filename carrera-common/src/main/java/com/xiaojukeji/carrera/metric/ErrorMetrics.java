package com.xiaojukeji.carrera.metric;

import org.slf4j.Logger;

import java.util.concurrent.TimeUnit;

import static org.slf4j.LoggerFactory.getLogger;


public class ErrorMetrics {
    public static final Logger METRIC_LOGGER = getLogger("MetricLogger");
    private static final int REPORT_INTERVAL_S = 10;

    private CounterMetric errorCounter;

    public ErrorMetrics() {
        errorCounter = MetricFactory.getCounterMetric("errorCounter", REPORT_INTERVAL_S, TimeUnit.SECONDS, METRIC_LOGGER, "error_type");
    }

    public void incErrorCount(String type) {
        errorCounter.inc(type);
    }

    public void shutDown() {
        errorCounter.shutDown();
    }
}