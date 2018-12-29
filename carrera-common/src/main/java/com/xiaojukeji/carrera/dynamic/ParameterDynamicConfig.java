package com.xiaojukeji.carrera.dynamic;

import java.io.File;


public class ParameterDynamicConfig {

    public static final int DEFAULT_SESSION_TIMEOUT = 30000;
    public static final int GET_DATA_FROM_LOCAL_FILE_INTERVAL = 1000 * 3;
    public static final int GET_DATA_RETRY_WATCH_INTERVAL = 1000 * 5;
    public static final String LOCAL_CONFIG_FILE_DIR = System.getProperty("user.home") + File.separator + "carrera.conf";
    public static final String CHARSET = "UTF-8";

    private String zooKeeperHost;
    private boolean isConfigCentre;

    public ParameterDynamicConfig(String zooKeeperHost) {
        this.zooKeeperHost = zooKeeperHost;
    }

    public String getZooKeeperHost() {
        return zooKeeperHost;
    }

    public void setZooKeeperHost(String zooKeeperHost) {
        this.zooKeeperHost = zooKeeperHost;
    }

    public boolean isConfigCentre() {
        return isConfigCentre;
    }

    public void setConfigCentre(boolean configCentre) {
        isConfigCentre = configCentre;
    }

    @Override
    public String toString() {
        return "ParameterDynamicConfig{" +
                "DEFAULT_SESSION_TIMEOUT=" + DEFAULT_SESSION_TIMEOUT +
                ", GET_DATA_FROM_LOCAL_FILE_INTERVAL=" + GET_DATA_FROM_LOCAL_FILE_INTERVAL +
                ", GET_DATA_RETRY_WATCH_INTERVAL=" + GET_DATA_RETRY_WATCH_INTERVAL +
                ", LOCAL_CONFIG_FILE_DIR='" + LOCAL_CONFIG_FILE_DIR + '\'' +
                ", CHARSET='" + CHARSET + '\'' +
                ", zooKeeperHost='" + zooKeeperHost + '\'' +
                ", isConfigCentre=" + isConfigCentre +
                '}';
    }
}