package com.xiaojukeji.chronos.workers;

import com.google.common.base.Splitter;
import com.xiaojukeji.chronos.config.ConfigManager;
import com.xiaojukeji.chronos.services.MqPullService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;


public class PullWorker {
    private static final Logger LOGGER = LoggerFactory.getLogger(PullWorker.class);

    private static final String CPROXY_ADDRS = ConfigManager.getConfig().getPullConfig().getCproxyAddrs();
    private static final List<String> cproxyServers = Splitter.on(';').splitToList(CPROXY_ADDRS);
    private static final List<MqPullService> pullServices = new ArrayList<>();

    private static int threadNum = ConfigManager.getConfig().getPullConfig().getThreadNum();
    private static volatile PullWorker instance = null;

    private PullWorker() {
    }

    public void start() {
        LOGGER.info("PullWorker will start ...");
        final long start = System.currentTimeMillis();

        if (cproxyServers.size() > threadNum) {
            threadNum = cproxyServers.size();
            LOGGER.error("cproxyServersNum > threadNum in conf, assign cproxyServersNum to threadNum", threadNum);
        }

        for (int i = 0; i < threadNum; i++) {
            MqPullService mqPullService = new MqPullService(cproxyServers.get(i % cproxyServers.size()), i);
            pullServices.add(mqPullService);
            mqPullService.start();
        }

        LOGGER.info("PullWorker has started, cost:{}ms", System.currentTimeMillis() - start);
    }

    public void stop() {
        LOGGER.info("PullWorker will stop ...");
        final long start = System.currentTimeMillis();

        pullServices.forEach(MqPullService::stop);

        LOGGER.info("PullWorker has stopped, cost:{}ms", System.currentTimeMillis() - start);
    }

    public static PullWorker getInstance() {
        if (instance == null) {
            synchronized (PullWorker.class) {
                if (instance == null) {
                    instance = new PullWorker();
                }
            }
        }
        return instance;
    }
}