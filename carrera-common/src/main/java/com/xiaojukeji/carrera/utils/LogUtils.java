package com.xiaojukeji.carrera.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.slf4j.LoggerFactory.getLogger;


public class LogUtils {
    public static final Logger LOGGER = getLogger(LogUtils.class);
    public static final Logger MAIN_LOGGER = getLogger("MainLogger");
    public static final Logger METRIC_LOGGER = getLogger("MetricLogger");

    public static void logMainInfo(String format, Object... objects) {
        MAIN_LOGGER.info(format, objects);
    }

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
        MetricUtils.incError(errTag);
    }

}