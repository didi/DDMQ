package com.xiaojukeji.carrera.metric;

import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;


public abstract class MetricReporter {
    private static final Logger LOGGER = LoggerFactory.getLogger(MetricReporter.class);

    private final Logger metricLogger;
    protected final String metricName;
    protected final long step;
    private final TimeUnit unit;
    protected final String[] metricTags;
    protected volatile int metricBufferIdx = 0;
    private ScheduledFuture future;

    public MetricReporter(String metricName, long step, TimeUnit unit, Logger metricLogger, String[] metricTags) {
        if (StringUtils.isEmpty(metricName) || step <= 0) {
            throw new IllegalArgumentException("param illegal, metricName=" + metricName + ", step=" + step);
        }
        this.metricName = metricName;
        this.step = step;
        this.unit = unit;
        this.metricTags = metricTags;

        if (metricLogger == null) {
            this.metricLogger = LOGGER;
        } else {
            this.metricLogger = metricLogger;
        }
    }

    public void shutDown() {
        if (future != null) {
            future.cancel(false);
        }
    }

    public void setFuture(ScheduledFuture future) {
        this.future = future;
    }

    public long getStep() {
        return step;
    }

    public TimeUnit getUnit() {
        return unit;
    }

    public void reportWorker() {
        int lastMetricBufferIdx = metricBufferIdx;
        metricBufferIdx = 1 - metricBufferIdx;

        List<Metric> metrics = buildMetrics(lastMetricBufferIdx);

        if (metrics.size() > 0) {
            reportMetrics(metrics);
        }
    }

    protected abstract List<Metric> buildMetrics(int index);

    protected void reportMetrics(List<Metric> metrics) {
        if (metricLogger != null) {
            for (Metric metric : metrics) {
                metricLogger.info("[CarreraMetric] - metric:{}", metric);
            }
        }
        MetricClient.getInstance().sendMetrics(metrics);
    }

    public static String trimTag(String tag) {
        if (StringUtils.isEmpty(tag)) {
            return "null";
        }
        return StringUtils.replaceEach(tag, TRIM_KEYS, TRIM_VALUES);
    }

    private static final String[] TRIM_KEYS;
    private static final String[] TRIM_VALUES;
    private static final Map<String, String> TRIM_MAP = Maps.newHashMap();

    static {
        TRIM_MAP.put("://", "|");
        TRIM_MAP.put(":", "|");
        TRIM_MAP.put("=", "|");
        TRIM_MAP.put(",", "|");
        TRIM_MAP.put("@", "|");
        TRIM_MAP.put(" ", "");
        TRIM_MAP.put("\r", "");
        TRIM_MAP.put("\n", "");
        TRIM_MAP.put("\t", "");
        TRIM_KEYS = TRIM_MAP.keySet().toArray(new String[TRIM_MAP.size()]);
        TRIM_VALUES = TRIM_MAP.values().toArray(new String[TRIM_MAP.size()]);
    }
}