package com.xiaojukeji.carrera.pproxy.producer;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.xiaojukeji.carrera.pproxy.utils.RoundRobinPickerList;
import io.netty.util.HashedWheelTimer;
import org.apache.rocketmq.common.ThreadFactoryImpl;

import java.util.concurrent.*;


public class TimeOutHandlerMgr {

    private static boolean isInited = false;
    private static volatile RoundRobinPickerList<HashedWheelTimer> timeoutCheckers;

    private static int availableProcessors = Runtime.getRuntime().availableProcessors();
    private static BlockingQueue bq = new LinkedBlockingQueue();
    private static final ExecutorService timeOutExecutor = new ThreadPoolExecutor(
            availableProcessors,
            availableProcessors,
            1000 * 60,
            TimeUnit.MILLISECONDS,
            bq,
            new ThreadFactoryImpl("PproxyTimeoutExecutorThreadPool_")
    );

    public static void init(int timeoutCheckerThreads) {
        timeoutCheckers = new RoundRobinPickerList<>(timeoutCheckerThreads);
        for (int i = 0; i < timeoutCheckerThreads; i++) {
            //1024*10ms = 10s for one wheel.
            HashedWheelTimer timeoutChecker = new HashedWheelTimer(
                    new ThreadFactoryBuilder().setNameFormat("ServerTimeoutChecker-%d").setDaemon(true).build(),
                    10, TimeUnit.MILLISECONDS, 1024);
            timeoutChecker.start();
            timeoutCheckers.add(timeoutChecker);
        }
        isInited = true;
    }

    public static HashedWheelTimer selectOneTimeOutChecker() {
        assert isInited;
        return timeoutCheckers.pick();
    }

    public static ExecutorService getTimeOutExecutor() {
        return timeOutExecutor;
    }
}