package com.didi.carrera.console.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ConsoleConfig {
    @Value("${console.carrera.zookeeper}")
    private String zookeeper;
    @Value("${console.env}")
    private String env;
    @Value("#{'${console.admin.user}'.split(',')}")
    private List<String> carreraAdminUser;
    @Value("#{'${console.admin.password}'.split(',')}")
    private List<String> carreraAdminPassword;

    public String getZookeeper() {
        return zookeeper;
    }

    public void setZookeeper(String zookeeper) {
        this.zookeeper = zookeeper;
    }

    public String getEnv() {
        return env;
    }

    public void setEnv(String env) {
        this.env = env;
    }

    public List<String> getCarreraAdminUser() {
        return carreraAdminUser;
    }

    public void setCarreraAdminUser(List<String> carreraAdminUser) {
        this.carreraAdminUser = carreraAdminUser;
    }

    public List<String> getCarreraAdminPassword() {
        return carreraAdminPassword;
    }

    public void setCarreraAdminPassword(List<String> carreraAdminPassword) {
        this.carreraAdminPassword = carreraAdminPassword;
    }

    @Override
    public String toString() {
        return "ConsoleConfig{" +
                "zookeeper='" + zookeeper + '\'' +
                ", env='" + env + '\'' +
                ", carreraAdminUser=" + carreraAdminUser +
                ", carreraAdminPassword=" + carreraAdminPassword +
                '}';
    }
}