package com.xiaojukeji.carrera.utils;

import com.xiaojukeji.carrera.config.ConfigurationValidator;
import org.slf4j.Logger;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.function.Function;

import static org.slf4j.LoggerFactory.getLogger;


public class ConfigUtils {
    public static final Logger LOGGER = getLogger(ConfigUtils.class);

    public static <T> T getDefaultConfig(String configKey, T defaultValue, Function<String, T> converter) {
        String value = System.getProperty(configKey);
        if (value == null) {
            return defaultValue;
        }
        try {
            return converter.apply(value);
        } catch (Throwable t) {
            LOGGER.error("ConfigUtils.getDefaultConfig", "convert error, value=" + value, t);
        }
        return defaultValue;
    }

    public static boolean getDefaultConfig(String configKey, boolean defaultValue) {
        return getDefaultConfig(configKey, defaultValue, Boolean::valueOf);
    }

    public static int getDefaultConfig(String configKey, int defaultValue) {
        return getDefaultConfig(configKey, defaultValue, Integer::valueOf);
    }

    public static <T extends ConfigurationValidator> T newConfig(String configFile, Class<T> clz) throws ConfigurationValidator.ConfigException {
        T config;
        Yaml yaml = new Yaml();
        try (InputStream in = Files.newInputStream(Paths.get(configFile))) {
            config = yaml.loadAs(in, clz);
        } catch (IOException e) {
            throw new ConfigurationValidator.ConfigException("IO exception: " + e.getMessage());
        }
        if (!config.validate()) {
            throw new ConfigurationValidator.ConfigException("invalid config.");
        }
        return config;
    }
}