package com.didi.carrera.console.config;

import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.List;

public class ConsoleConfig {
    private String zookeeper;
    private static ConsoleConfig consoleConfig;
    private String env;

    private List<String> carreraAdminUser;

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

    public static ConsoleConfig instance() {
        return consoleConfig;
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

    static {
        Yaml yaml = new Yaml();
        try (InputStream inputStream = ConsoleConfig.class.getResourceAsStream("/console.yaml")) {
            consoleConfig = yaml.loadAs(inputStream, ConsoleConfig.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
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