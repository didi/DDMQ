package com.xiaojukeji.carrera.cproxy.consumer;


import com.xiaojukeji.carrera.cproxy.utils.LogUtils;
import org.apache.rocketmq.common.ThreadFactoryImpl;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


public class SharedThreadPool {

    private static BlockingQueue bq = new LinkedBlockingQueue();

    private static int availableProcessors = Runtime.getRuntime().availableProcessors();

    private static ThreadPoolExecutor executor = new ThreadPoolExecutor(
            availableProcessors,
            availableProcessors,
            1000 * 60,
            TimeUnit.MILLISECONDS,
            bq,
            new ThreadFactoryImpl("CproxySharedExecutorThreadPool_")
    );

    private static ScheduledThreadPoolExecutor scheduler = new ScheduledThreadPoolExecutor(
            availableProcessors,
            new ThreadFactoryImpl("CproxySharedSchedulerThreadPool_")
    );

    static {
        scheduler.scheduleAtFixedRate(()-> LogUtils.METRIC_LOGGER.info("Shared scheduler queue size: {}, Shared executor queue size: {}.",
                getSchedulerQueueSize(), getExecutorQueueSize()), 60, 60, TimeUnit.SECONDS);
    }

    public static ThreadPoolExecutor getExecutor() {
        return executor;
    }

    public static ScheduledExecutorService getScheduler() {
        return scheduler;
    }


    private static int getSchedulerQueueSize() {
        if (scheduler == null || scheduler.getQueue() == null) {
            return 0;
        }
        return scheduler.getQueue().size();
    }

    private static int getExecutorQueueSize() {
        if (bq == null) {
            return 0;
        }
        return bq.size();
    }

    public static void shutdown() {
        scheduler.shutdown();
        executor.shutdown();
    }
}