package com.xiaojukeji.carrera.pproxy.proxy;


import com.xiaojukeji.carrera.pproxy.utils.LogUtils;
import org.apache.logging.log4j.LogManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ProducerProxyMain {

    private static Logger LOGGER = LoggerFactory.getLogger(ProducerProxyMain.class);

    private static volatile ProxyApp proxyApp;

    public static void main(String[] args) {
        Thread.setDefaultUncaughtExceptionHandler((thread, exception) ->
                LOGGER.error("UncaughtException in Thread " + thread.toString(), exception));
        registerShutdownHook();

        LOGGER.info("carrera start !");
        if (args.length < 1) {
            LogUtils.logError("ProducerProxyMain.main", "param error !");
            return;
        }
        LOGGER.info("log file path : {}", args[0]);

        proxyApp = new ProxyApp(args[0]);
        proxyApp.start();

        LOGGER.info("carrera end !");
        System.exit(-1);
    }

    private static void registerShutdownHook() {
        // register shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                System.out.println("Start to stop carrera producer!");
                LOGGER.info("Start to stop carrera producer!");
                if (proxyApp != null) {
                    proxyApp.stop();
                }
                System.out.println("carrera producer stopped!");
                LOGGER.info("carrera consumer stopped!");
            } catch (Exception e) {
                LogUtils.logError("ProxyApp.start", "register shutdownHook error", e);
            } finally {
                LogManager.shutdown(); //shutdown log4j2.
            }
        }));
    }
}