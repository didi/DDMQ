package com.xiaojukeji.carrera.cproxy.proxy;

import com.xiaojukeji.carrera.cproxy.consumer.offset.CarreraOffsetManager;
import com.xiaojukeji.carrera.cproxy.consumer.ConfigManager;
import com.xiaojukeji.carrera.cproxy.consumer.ConsumerManager;
import com.xiaojukeji.carrera.cproxy.consumer.SharedThreadPool;
import com.xiaojukeji.carrera.cproxy.server.ConsumeServer;
import com.xiaojukeji.carrera.cproxy.utils.LogUtils;
import com.xiaojukeji.carrera.cproxy.utils.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.slf4j.Logger;

import java.util.concurrent.CountDownLatch;


public class ProxyApp {

    public static final String CONFIG_TYPE_REMOTE = "remote";
    public static final String CONFIG_TYPE_LOCAL = "local";
    private static final Logger LOGGER = LogUtils.MAIN_LOGGER;

    private CountDownLatch waitForShutdown;

    private String configFilePath = "conf/consumer.yaml";
    private String configType = System.getProperty("carrera.consumer.configType", CONFIG_TYPE_REMOTE);
    private ConfigManager configManager;
    private ConsumerManager consumerManager;
    private ConsumeServer server;

    public ProxyApp(String configFilePath) {
        if (StringUtils.isNoneBlank(configFilePath)) {
            this.configFilePath = configFilePath;
        }
    }

    public void start() throws Exception {
        // register shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                LOGGER.info("Start to stop carrera consumer!");
                ProxyApp.this.stop();
                LOGGER.info("carrera consumer stopped!");
            } catch (Exception e) {
                LOGGER.error("exception when shutdown...", e);
            } finally {
                LogManager.shutdown(); //shutdown log4j2.
            }
        }));

        waitForShutdown = new CountDownLatch(1);

        consumerManager = ConsumerManager.getInstance();
        consumerManager.start();

        configManager = ConfigManager.getInstance();
        configManager.start(configFilePath, configType);

        startPullServer(configManager);

        waitForShutdown.await();

    }

    private void startPullServer(ConfigManager configManager) throws Exception {
        server = new ConsumeServer(configManager.getConsumeServerConfiguration());
        server.init();
        new Thread(server::start, "server thread").start();
    }

    public void stop() {
        if (server != null) {
            server.stop();
            server = null;
        }

        CarreraOffsetManager.getInstance().shutdown();

        if (configManager != null) {
            configManager.shutdown();
            configManager = null;
        }
        if (consumerManager != null) {
            consumerManager.shutdown();
            consumerManager = null;
        }

        SharedThreadPool.shutdown();

        if (waitForShutdown != null) {
            waitForShutdown.countDown();
            waitForShutdown = null;
        }
    }
}