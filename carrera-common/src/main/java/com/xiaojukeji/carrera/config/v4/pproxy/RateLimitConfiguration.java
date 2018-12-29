package com.xiaojukeji.carrera.config.v4.pproxy;

import com.xiaojukeji.carrera.config.ConfigurationValidator;


public class RateLimitConfiguration implements ConfigurationValidator {
    private boolean staticMode = false;
    private String zkPath;

    public boolean isStaticMode() {
        return staticMode;
    }

    public void setStaticMode(boolean staticMode) {
        this.staticMode = staticMode;
    }

    public String getZkPath() {
        return zkPath;
    }

    public void setZkPath(String zkPath) {
        this.zkPath = zkPath;
    }

    @Override
    public String toString() {
        return "RateLimitConfiguration{" +
                "staticMode=" + staticMode +
                ", zkPath='" + zkPath + '\'' +
                '}';
    }

    @Override
    public boolean validate() {
        return true;
    }
}