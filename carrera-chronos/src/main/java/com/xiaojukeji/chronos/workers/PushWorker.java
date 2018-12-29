package com.xiaojukeji.chronos.workers;

import com.xiaojukeji.chronos.services.MqPushService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;


public class PushWorker implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(PushWorker.class);

    private static final MqPushService pushService = MqPushService.getInstance();

    private static volatile PushWorker instance = null;
    private static volatile boolean shouldStop = false;
    private static CountDownLatch cdl;

    private PushWorker() {
    }

    @Override
    public void run() {
        while (!shouldStop) {
            pushService.pullFromDefaultCFAndPush();
        }

        LOGGER.info("PushWorker will stop ...");
        final long start = System.currentTimeMillis();
        pushService.stop();
        LOGGER.info("PushWorker has stopped, cost:{}ms", System.currentTimeMillis() - start);
        cdl.countDown();
    }

    public void start() {
        LOGGER.info("PushWorker will start ...");
        final long start = System.currentTimeMillis();
        new Thread(this).start();
        LOGGER.info("PushWorker has started, cost:{}ms", System.currentTimeMillis() - start);
    }

    public void stop() {
        cdl = new CountDownLatch(1);
        shouldStop = true;
        try {
            cdl.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static PushWorker getInstance() {
        if (instance == null) {
            synchronized (PushWorker.class) {
                if (instance == null) {
                    instance = new PushWorker();
                }
            }
        }
        return instance;
    }
}