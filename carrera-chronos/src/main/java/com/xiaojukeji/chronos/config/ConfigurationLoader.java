package com.xiaojukeji.chronos.config;

import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;


public class ConfigurationLoader {
    public static <T extends ConfigValidator> T newConfig(String configFile, Class<T> clz) throws Exception {
        T config;
        Yaml yaml = new Yaml();
        try (InputStream in = Files.newInputStream(Paths.get(configFile))) {
            config = yaml.loadAs(in, clz);
        }
        if (!config.validate()) {
            throw new Exception("invalid config.");
        }
        return config;
    }
}