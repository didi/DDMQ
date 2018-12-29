package com.xiaojukeji.carrera.pproxy.utils;

import java.util.function.Function;


public class ConfigUtils {

    private static <T> T getDefaultConfig(String configKey, T defaultValue, Function<String, T> converter) {
        String value = System.getProperty(configKey);
        if (value == null) {
            return defaultValue;
        }
        try {
            return converter.apply(value);
        } catch (Throwable t) {
            LogUtils.logError("ConfigUtils.getDefaultConfig", "convert error, value=" + value, t);
        }

        return defaultValue;
    }

    public static boolean getDefaultConfig(String configKey, boolean defaultValue) {
        return getDefaultConfig(configKey, defaultValue, Boolean::valueOf);
    }

    public static int getDefaultConfig(String configKey, int defaultValue) {
        return getDefaultConfig(configKey, defaultValue, Integer::valueOf);
    }
}