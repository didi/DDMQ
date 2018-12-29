package com.xiaojukeji.carrera.utils;


import org.apache.commons.lang.StringUtils;

import java.util.Properties;


public class VersionUtils {

    private static String version = null;

    public static String getVersion() {
        if (StringUtils.isNotEmpty(version)) {
            return version;
        }
        Properties properties = new Properties();
        try {
            properties.load(VersionUtils.class.getClassLoader().getResourceAsStream("carrera_producer_sdk_version.properties"));
            if (!properties.isEmpty()) {
                version = properties.getProperty("version");
            }
            if (StringUtils.isEmpty(version)) {
                version = "java";
            }
        }catch (Exception e) {
            version = "java";
        }
        return version;
    }
}