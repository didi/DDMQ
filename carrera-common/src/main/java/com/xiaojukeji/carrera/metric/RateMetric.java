package com.xiaojukeji.carrera.metric;

import org.slf4j.Logger;

import java.util.concurrent.TimeUnit;


public class RateMetric extends CounterMetric {
    public RateMetric(String metricName, long step, TimeUnit unit, Logger metricLogger, String... metricTags) {
        super(metricName, step, unit, metricLogger, metricTags);
    }

    protected long calcValue(long value) {
        return (long) Math.ceil((double) value / step);
    }
}