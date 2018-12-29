package com.xiaojukeji.carrera.monitor.app;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CarreraMonitor {

    private static final Logger LOGGER = LoggerFactory.getLogger(CarreraMonitor.class);

    public static void main(String[] args) throws InterruptedException {
        LOGGER.info("Monitor start");

        if (args.length < 1) {
            LOGGER.error("param error");
            return;
        }

        MonitorApp app = new MonitorApp(args[0]);
        try {
            app.start();
        } catch (Exception e) {
            LOGGER.info("Monitor start error", e);
        }

        LOGGER.info("Monitor shutdown");
    }
}
