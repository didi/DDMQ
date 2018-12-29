package com.xiaojukeji.carrera.cproxy.concurrent;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


public class CarreraExecutors {

    public static ScheduledExecutorService newSingleThreadScheduledExecutor(String threadName) {
        return new CarreraScheduledExecutorService(new ScheduledThreadPoolExecutor(1, r -> {
            Thread t = new Thread(r, threadName);
            t.setDaemon(true);
            return t;
        }));
    }

    public static ScheduledExecutorService newScheduledThreadPool(int nThread, String threadName) {
        return new CarreraScheduledExecutorService(new ScheduledThreadPoolExecutor(nThread, r -> {
            Thread t = new Thread(r, threadName);
            t.setDaemon(true);
            return t;
        }));
    }

    public static ExecutorService newFixedThreadPool(int nThread, String threadName, int capacity) {
        return new ThreadPoolExecutor(nThread, nThread,
                0L, TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<>(capacity),
                r -> {
                    Thread t = new Thread(r, threadName);
                    t.setDaemon(true);
                    return t;
                },
                new ThreadPoolExecutor.AbortPolicy()
        );
    }
}