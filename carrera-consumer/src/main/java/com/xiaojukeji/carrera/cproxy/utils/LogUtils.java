package com.xiaojukeji.carrera.cproxy.utils;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.slf4j.LoggerFactory.getLogger;


public class LogUtils {
    public static final Logger LOGGER = getLogger(LogUtils.class);
    public static final Logger MAIN_LOGGER = getLogger("MainLogger");
    public static final Logger METRIC_LOGGER = getLogger("MetricLogger");
    public static final Logger DROP_LOGGER = LoggerFactory.getLogger("DropLogger");
    public static final Logger OFFSET_LOGGER = getLogger("OffsetLogger");

    public static void logMainInfo(String format, Object... objects) {
        MAIN_LOGGER.info(format, objects);
    }

    public static void logMainInfo(String format, Object arg1) {
        MAIN_LOGGER.info(format, arg1);
    }

    public static void logMainInfo(String format, Object arg1, Object arg2) {
        MAIN_LOGGER.info(format, arg1, arg2);
    }

    public static void logErrorInfo(String tag, String format, Object ... args) {
        LOGGER.error(format, args);
        MetricUtils.incError(tag);
    }
}