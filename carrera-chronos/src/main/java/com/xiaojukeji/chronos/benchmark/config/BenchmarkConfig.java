package com.xiaojukeji.chronos.benchmark.config;


import com.xiaojukeji.carrera.config.ConfigurationValidator;

public class BenchmarkConfig implements ConfigurationValidator {

    private PushConfig pushConfig;
    private PullConfig pullConfig;

    public PushConfig getPushConfig() {
        return pushConfig;
    }

    public void setPushConfig(PushConfig pushConfig) {
        this.pushConfig = pushConfig;
    }

    public PullConfig getPullConfig() {
        return pullConfig;
    }

    public void setPullConfig(PullConfig pullConfig) {
        this.pullConfig = pullConfig;
    }

    @Override
    public String toString() {
        return "BenchmarkConfig{" +
                "pushConfig=" + pushConfig +
                ", pullConfig=" + pullConfig +
                '}';
    }

    @Override
    public boolean validate() {
        return true;
    }
}