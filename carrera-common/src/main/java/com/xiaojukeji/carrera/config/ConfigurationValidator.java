package com.xiaojukeji.carrera.config;


public interface ConfigurationValidator {

    boolean validate() throws ConfigException;

    class ConfigException extends Exception {
        public ConfigException(String message) {
            super(message);
        }
    }
}