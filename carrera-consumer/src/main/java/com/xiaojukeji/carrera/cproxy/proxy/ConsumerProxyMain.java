package com.xiaojukeji.carrera.cproxy.proxy;

import com.xiaojukeji.carrera.cproxy.utils.LogUtils;
import org.slf4j.Logger;


public class ConsumerProxyMain {

    private static Logger LOGGER = LogUtils.MAIN_LOGGER;

    public static volatile ProxyApp proxyApp;

    public static void main(String[] args) throws InterruptedException {
        Thread.setDefaultUncaughtExceptionHandler((thread, exception) ->
                LOGGER.error("UncaughtException in Thread " + thread.toString(), exception));
        LOGGER.info("carrera consumer start!");
        if (args.length < 1) {
            LOGGER.error("param error !");
            return;
        }

        proxyApp = new ProxyApp(args[0]);
        try {
            proxyApp.start();
        } catch (Exception e) {
            LOGGER.error("start proxyApp failed.", e);
            proxyApp.stop();
        }

        LOGGER.info("carrera consumer end !");
    }

}