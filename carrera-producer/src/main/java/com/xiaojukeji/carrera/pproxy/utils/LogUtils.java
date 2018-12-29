package com.xiaojukeji.carrera.pproxy.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class LogUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(LogUtils.class);

    public static void logError(String errTag, Throwable e) {
        logError(errTag, e.getMessage(), e, true);
    }

    public static void logError(String errTag, String errDetail) {
        logError(errTag, errDetail, null, false);
    }

    public static void logError(String errTag, String errDetail, Throwable e) {
        logError(errTag, errDetail, e, true);
    }

    public static void logError(String errTag, String errDetail, Throwable e, boolean printStackTrace) {
        if (e == null) {
            LOGGER.error("ERROR_TAG:{}, EXCEPTION:null, {}", errTag, errDetail);
        } else if (printStackTrace) {
            LOGGER.error(String.format("ERROR_TAG:%s, EXCEPTION:%s, %s", errTag, e.getClass().getName(), errDetail), e);
        } else {
            LOGGER.error("ERROR_TAG:{}, EXCEPTION:{}, {}", errTag, e.getClass().getName(), errDetail);
        }
    }

    public static void logWarn(String tag, String detail) {
        LOGGER.warn("WARN_TAG:{}, DETAIL:{}", tag, detail);
    }

    public static Logger getDropLogger() {
        return LoggerFactory.getLogger("DropLogger");
    }

    public static Logger getMetricLogger() {
        return LoggerFactory.getLogger("MetricLogger");
    }

    public static Logger getMainLogger() {
        return LoggerFactory.getLogger("MainLogger");
    }
}