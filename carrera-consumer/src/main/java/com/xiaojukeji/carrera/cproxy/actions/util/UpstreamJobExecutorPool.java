package com.xiaojukeji.carrera.cproxy.actions.util;

import com.xiaojukeji.carrera.cproxy.config.ConsumerGroupConfig;
import com.xiaojukeji.carrera.cproxy.consumer.SharedThreadPool;
import com.xiaojukeji.carrera.cproxy.consumer.UpstreamJob;
import com.xiaojukeji.carrera.cproxy.utils.LogUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;


public class UpstreamJobExecutorPool {

    private static final Logger LOGGER = LoggerFactory.getLogger(UpstreamJobExecutorPool.class);
    private UpstreamJobBlockingQueue queue;

    private List<WorkerThread> threads = null;
    private boolean useBackgroundThread = false;
    private AtomicInteger activeThreadNumber = new AtomicInteger(0);
    private String group;
    private int asyncThreads;

    public UpstreamJobExecutorPool(ConsumerGroupConfig config) {
        this.group = config.getGroup();
        this.asyncThreads = config.getGroupConfig().getAsyncThreads();
        this.queue = new UpstreamJobBlockingQueue(config);
    }

    public void startBackgroundThreads() {
        useBackgroundThread = true;
        threads = new ArrayList<>();
        for (int i = 0; i < asyncThreads; i++) {
            WorkerThread workerThread = new WorkerThread(group + "-" + i, i);
            workerThread.start();
            threads.add(workerThread);
        }
    }

    public void submit(UpstreamJob job) throws InterruptedException {
        job.registerJobFinishedCallback(this::onJobFinished);
        queue.submit(job);
        if (!useBackgroundThread) {
            queue.processNextMessage();
        }
    }

    public void onJobFinished(UpstreamJob job, boolean success) {
        queue.onJobFinished(job);
        if (!useBackgroundThread) {
            SharedThreadPool.getExecutor().execute(() -> queue.processNextMessage());
        }
    }

    public int getActiveThreadNumber() {
        return activeThreadNumber.get();
    }

    public UpstreamJobBlockingQueue getQueue() {
        return queue;
    }

    public void shutdown() {
        if(threads != null) {
            threads.forEach(WorkerThread::shutdown);
        }
    }

    private class WorkerThread extends Thread {
        private volatile boolean running = true;
        private final int workerId;

        public WorkerThread(String name, int workerId) {
            super(name);
            this.workerId = workerId;
        }

        @Override
        public void run() {
            LOGGER.info("Thread {} started.", getName());
            while (running) {
                UpstreamJob job;
                try {
                    job = queue.poll();
                } catch (InterruptedException e) {
                    LOGGER.info("worker thread {} is interrupted", getName());
                    break;
                }
                assert job != null;
                activeThreadNumber.incrementAndGet();
                job.setWorkerId(workerId);
                try {
                    job.execute();
                } catch (Exception e) {
                    LogUtils.logErrorInfo("worker_running_error", "worker running error", e);
                }
                activeThreadNumber.decrementAndGet();
            }
            LOGGER.info("Thread {} finished. job after shutdown, group={}, queue.info={}",
                    getName(), group, queue.info());
        }

        public void shutdown() {
            running = false;
            interrupt();
        }
    }
}