package com.xiaojukeji.chronos;

import com.xiaojukeji.chronos.config.ConfigManager;
import com.xiaojukeji.chronos.db.RDB;
import com.xiaojukeji.chronos.ha.MasterElection;
import com.xiaojukeji.chronos.http.NettyHttpServer;
import com.xiaojukeji.chronos.metrics.MetricService;
import com.xiaojukeji.chronos.services.MetaService;
import com.xiaojukeji.chronos.services.MqConsumeStatService;
import com.xiaojukeji.chronos.utils.ZkUtils;
import com.xiaojukeji.chronos.workers.DeleteBgWorker;
import com.xiaojukeji.chronos.workers.PullWorker;
import com.xiaojukeji.chronos.workers.PushWorker;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;


public class ChronosStartup {
    private static final Logger LOGGER = LoggerFactory.getLogger(ChronosStartup.class);

    private CountDownLatch waitForShutdown;
    private String configFilePath = "chronos.yaml";
    private PullWorker pullWorker;
    private PushWorker pushWorker;
    private DeleteBgWorker deleteBgWorker;
    private NettyHttpServer nettyHttpServer;

    ChronosStartup(final String configFilePath) {
        if (StringUtils.isNotBlank(configFilePath)) {
            this.configFilePath = configFilePath;
        }
    }

    public void start() throws Exception {
        LOGGER.info("start to launch chronos...");
        final long start = System.currentTimeMillis();

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                try {
                    LOGGER.info("start to stop chronos...");
                    final long start = System.currentTimeMillis();
                    ChronosStartup.this.stop();
                    final long cost = System.currentTimeMillis() - start;
                    LOGGER.info("succ stop chronos, cost:{}ms", cost);
                } catch (Exception e) {
                    LOGGER.error("error while shutdown chronos, err:{}", e.getMessage(), e);
                } finally {
                    /* shutdown log4j2 */
                    LogManager.shutdown();
                }
            }
        });

        /* 注意: 以下初始化顺序有先后次序 */

        /* init config */
        ConfigManager.initConfig(configFilePath);

        /* init metrics */
        if (!MetricService.init()) {
            System.exit(-1);
        }

        /* init rocksdb */
        RDB.init(ConfigManager.getConfig().getDbConfig().getDbPath());

        /* init zk */
        ZkUtils.init();

        /* init seektimestamp */
        MetaService.load();

        waitForShutdown = new CountDownLatch(1);

        if (ConfigManager.getConfig().isStandAlone()) {
            /* standalone */
            MasterElection.standAlone();
        } else {
            /* 集群模式 master election */
            MasterElection.election(waitForShutdown);
        }

        /* init pull worker */
        if (ConfigManager.getConfig().isPullOn()) {
            pullWorker = PullWorker.getInstance();
            pullWorker.start();
        }

        /* init push worker */
        if (ConfigManager.getConfig().isPushOn()) {
            pushWorker = PushWorker.getInstance();
            pushWorker.start();
        }

        /* init delete worker */
        if (ConfigManager.getConfig().isDeleteOn()) {
            deleteBgWorker = DeleteBgWorker.getInstance();
            deleteBgWorker.start();
        }

        final long cost = System.currentTimeMillis() - start;
        LOGGER.info("succ start chronos, cost:{}ms", cost);

        /* init http server */
        nettyHttpServer = NettyHttpServer.getInstance();
        nettyHttpServer.start();

        waitForShutdown.await();
    }

    void stop() {
        /* shutdown netty http server */
        if (nettyHttpServer != null) {
            nettyHttpServer.shutdown();
        }

        /* stop pull from MQ */
        if (pullWorker != null) {
            pullWorker.stop();
        }

        /* stop push to MQ */
        if (pushWorker != null) {
            pushWorker.stop();
        }

        /* stop delete */
        if (deleteBgWorker != null) {
            deleteBgWorker.stop();
        }

        MqConsumeStatService.getInstance().stop();

        /* close zk client */
        ZkUtils.close();

        /* close rocksdb */
        RDB.close();

        if (waitForShutdown != null) {
            waitForShutdown.countDown();
            waitForShutdown = null;
        }
    }
}