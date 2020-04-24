package com.didi.carrera.console;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

import java.net.InetAddress;
import java.net.UnknownHostException;

@Slf4j
@SpringBootApplication
@MapperScan("com.didi.carrera.console.dao.mapper")
public class ConsoleApplication {
    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(ConsoleApplication.class);
        Environment env = app.run(args).getEnvironment();
        log(env);
    }

    /**
     * 格式化运行成功后输出项目地址
     *
     * @param env
     */
    private static void log(Environment env) {
        String name = env.getProperty("spring.application.name");
        String port = env.getProperty("server.port");
        String path = env.getProperty("server.servlet.context-path");
        String ssl = env.getProperty("server.ssl.enabled");
        String http = "true".equals(ssl) ? "https" : "http";
        String address = null;
        try {
            address = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            address = "127.0.0.1";
            e.printStackTrace();
        }
        name = StringUtils.isEmpty(name) ? "" : name;
        port = StringUtils.isEmpty(port) ? "8080" : port;
        path = StringUtils.isEmpty(path) ? "" : path;
        log.info(
                "\n----------------------------------------------------------\n\t"
                        + "Application '{}' is running! Access URLs:\n\t"
                        + "Local: \t\t{}://localhost:{}{}\n\t"
                        + "External: \t{}://{}:{}{}"
                        + "\n----------------------------------------------------------",
                name, http, port, path, http, address, port, path);
    }
}
