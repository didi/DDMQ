package com.xiaojukeji.carrera.pproxy.utils;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.apache.rocketmq.common.stats.TimeStats;
import org.slf4j.Logger;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class StatsUtils {
    public static final Logger LOGGER = LogUtils.getMetricLogger();

    public static final TimeStats.RolloverTimeStats sendSync = new TimeStats.RolloverTimeStats(100000);
    public static ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(
            new ThreadFactoryBuilder().setNameFormat("StatsUtils-%d").setDaemon(true).build());

    static {
        scheduler.scheduleAtFixedRate(StatsUtils::report, 1, 1, TimeUnit.SECONDS);
    }

    public static void report() {
        LOGGER.info("sendSync.stats:{}", sendSync.reportAndReset(1e-3));
    }
}