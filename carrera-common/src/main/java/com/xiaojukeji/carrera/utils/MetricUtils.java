package com.xiaojukeji.carrera.utils;

import com.xiaojukeji.carrera.metric.ErrorMetrics;
import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;


public class MetricUtils {
    public static final Logger LOGGER = getLogger(MetricUtils.class);

    private static final String METRIC_REPORT_OPEN = "metric.report.open";

    private static boolean started = ConfigUtils.getDefaultConfig(METRIC_REPORT_OPEN, true);
    private static ErrorMetrics errorMetrics = new ErrorMetrics();

    public static void incError(String tag) {
        if (!started) return;
        errorMetrics.incErrorCount(tag);
    }

    public static void shutdown() {
        errorMetrics.shutDown();
    }
}