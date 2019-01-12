package com.xiaojukeji.carrera.pproxy.proxy;

import com.xiaojukeji.carrera.pproxy.producer.ConfigManager;
import com.xiaojukeji.carrera.pproxy.producer.ProducerPool;
import com.xiaojukeji.carrera.pproxy.server.Server;
import com.xiaojukeji.carrera.pproxy.server.ThreadedSelectorServer;
import com.xiaojukeji.carrera.pproxy.utils.LogUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ProxyApp {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProxyApp.class);

    private Server server;

    private ProducerPool producerPool;

    private ConfigManager configManager;

    public ProxyApp(String configFilePath) {
        this.configManager = new ConfigManager(configFilePath);
    }

    public void start() {
        if (!configManager.start()) {
            LogUtils.logError("ProxyApp.start", "config manager start failed");
            return;
        }

        ProducerPool.initInstance(configManager);
        producerPool = ProducerPool.getInstance();
        try {
            producerPool.start();
        } catch (Exception e) {
            LogUtils.logError("ProxyApp.start", "start producerPool error", e);
            producerPool.close();
            return;
        }

        producerPool.warmUp();

        server = new ThreadedSelectorServer(configManager.getProxyConfig().getCarreraConfiguration(), producerPool);

        LOGGER.info("Thrift server starts serving...");
        server.startServer();
    }

    public void stop() {
        if (server != null) {
            server.stopServer();
        }

        if (producerPool != null) {
            producerPool.close();
        }
    }
}