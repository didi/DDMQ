package com.xiaoju.chronos;

import com.xiaojukeji.chronos.config.ConfigManager;
import com.xiaojukeji.chronos.db.RDB;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


public class TestPushWorker {
    private static String dbPath = "/Users/didi/rocks_db";
    private static String configPath = "/Users/didi/work/carrera-chronos/src/main/resources/chronos.yaml";
    @BeforeClass
    public static void init() {
        ConfigManager.initConfig(configPath);
        RDB.init(dbPath);
    }

    @Test
    public void testIT() {
        BlockingQueue bq = new ArrayBlockingQueue(1);
        ThreadPoolExecutor exector = new ThreadPoolExecutor(1, 1, 0, TimeUnit.SECONDS, bq, new RejectedExecutionHandler() {
            @Override
            public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                try {
                    executor.getQueue().put(r);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        exector.execute(()->{
            try {
                TimeUnit.SECONDS.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        exector.execute(()->{
            try {
                TimeUnit.SECONDS.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        exector.execute(()->{
            try {
                TimeUnit.SECONDS.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
    }
    @AfterClass
    public static void destructor() {
        RDB.close();
    }

}