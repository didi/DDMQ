package com.xiaojukeji.chronos;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ChronosMain {
    private static Logger LOGGER = LoggerFactory.getLogger(ChronosMain.class);

    public static void main(String[] args) {
        Thread.setDefaultUncaughtExceptionHandler((thread, exception) ->
                LOGGER.error("UncaughtException in Thread " + thread.toString(), exception));

        if (args.length < 1) {
            LOGGER.error("params error!");
            return;
        }

        ChronosStartup startup = new ChronosStartup(args[0]);
        try {
            startup.start();
        } catch (Exception e) {
            LOGGER.error("error while start chronos, err:{}", e.getMessage(), e);
            startup.stop();
        }
    }
}