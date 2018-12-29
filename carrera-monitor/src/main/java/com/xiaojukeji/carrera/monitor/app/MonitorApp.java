package com.xiaojukeji.carrera.monitor.app;

import com.xiaojukeji.carrera.monitor.broker.BrokerMonitor;
import com.xiaojukeji.carrera.monitor.config.MonitorConfig;
import com.xiaojukeji.carrera.monitor.inspection.InspectionMonitor;
import com.xiaojukeji.carrera.monitor.lag.ConsumerLagMonitor;
import com.xiaojukeji.carrera.monitor.utils.ConfigurationLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;

public class MonitorApp {

    private static final Logger LOGGER = LoggerFactory.getLogger(MonitorApp.class);

    private String configFile;

    private ConsumerLagMonitor consumerLagMonitor;

    private BrokerMonitor brokerMonitor;

    private InspectionMonitor inspectionMonitor;

    private CountDownLatch countDownLatch;

    public MonitorApp(String configFile) {
        this.configFile = configFile;
    }

    public void start() throws Exception {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                LOGGER.info("start shutdown carrera monitor");
                MonitorApp.this.shutdown();
                LOGGER.info("carrera monitor stopped");
            } catch (Exception e) {
                LOGGER.error("shutdown carrera monitor error", e);
            }
        }));

        MonitorConfig config = ConfigurationLoader.newConfig(configFile, MonitorConfig.class);

        LOGGER.info("start monitor, config:{}", config);
        countDownLatch = new CountDownLatch(1);

        brokerMonitor = new BrokerMonitor(config);
        inspectionMonitor = new InspectionMonitor(config);
        consumerLagMonitor = new ConsumerLagMonitor(config);

        try {
            brokerMonitor.start();
            inspectionMonitor.start();
            consumerLagMonitor.start();
        } catch (Exception e) {
            LOGGER.error("Monitor start error", e);
            shutdown();
            return;
        }

        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            LOGGER.error("", e);
        }

        LOGGER.info("monitor stopped");
    }

    public void shutdown() {
        if (brokerMonitor != null) {
            LOGGER.info("start shutdown brokerMonitor");
            brokerMonitor.shutdown();
            brokerMonitor = null;
            LOGGER.info("brokerMonitor stoped");
        }

        if (inspectionMonitor != null) {
            LOGGER.info("start shutdown inspectionMonitor");
            inspectionMonitor.shutdown();
            inspectionMonitor = null;
            LOGGER.info("inspectionMonitor stoped");
        }

        if (consumerLagMonitor != null) {
            LOGGER.info("start shutdown consumerLagMonitor");
            consumerLagMonitor.shutdown();
            consumerLagMonitor = null;
            LOGGER.info("consumerLagMonitor stopped");
        }

        if (countDownLatch != null) {
            countDownLatch.countDown();
            countDownLatch = null;
        }
    }
}
