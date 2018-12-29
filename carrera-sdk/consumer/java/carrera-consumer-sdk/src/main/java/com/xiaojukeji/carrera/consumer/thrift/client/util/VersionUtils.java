package com.xiaojukeji.carrera.consumer.thrift.client.util;

import org.slf4j.Logger;

import java.util.Properties;

import static org.slf4j.LoggerFactory.getLogger;


public class VersionUtils {
    public static final Logger LOGGER = getLogger(VersionUtils.class);

    private static String version = "java";

    static {
        Properties properties = new Properties();
        try {
            properties.load(VersionUtils.class.getClassLoader().getResourceAsStream("carrera_consumer_sdk_version.properties"));
            if (!properties.isEmpty()) {
                version = properties.getProperty("version");
            }
        } catch (Exception e) {
            LOGGER.warn("get carrera_consumer_sdk_version failed", e);
        }
    }

    public static String getVersion() {
        return version;
    }
}