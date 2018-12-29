package com.xiaojukeji.chronos.workers;

import com.google.common.base.Charsets;
import com.xiaojukeji.chronos.config.ConfigManager;
import com.xiaojukeji.chronos.config.DeleteConfig;
import com.xiaojukeji.chronos.db.CFManager;
import com.xiaojukeji.chronos.db.RDB;
import com.xiaojukeji.chronos.services.MetaService;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


public class DeleteBgWorker {
    private static final Logger LOGGER = LoggerFactory.getLogger(DeleteBgWorker.class);

    private static final DeleteConfig DELETE_CONFIG = ConfigManager.getConfig().getDeleteConfig();
    private static final int SAVE_HOURS_OF_DATA = DELETE_CONFIG.getSaveHours();
    private static final long INITIAL_DELAY_MINUTES = 1; // 1 分钟
    private static final long PERIOD_MINUTES = 10;       // 10 分钟

    private static volatile DeleteBgWorker instance = null;
    private static final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /**
     * 2017/10/13 00:00:00
     */
    private static final long MIN_TIMESTAMP = 1507824000;

    private static final ScheduledExecutorService SCHEDULE = new ScheduledThreadPoolExecutor(1,
            new BasicThreadFactory.Builder().namingPattern("delete-bg-worker-schedule-%d").daemon(true).build());

    private DeleteBgWorker() {
    }

    public void start() {
        SCHEDULE.scheduleAtFixedRate(() -> {
            byte[] beginKey = String.valueOf(MIN_TIMESTAMP).getBytes(Charsets.UTF_8);

            final long seekTimestampInSecond = MetaService.getSeekTimestamp();
            byte[] endKey = String.valueOf(seekTimestampInSecond - SAVE_HOURS_OF_DATA * 60 * 60).getBytes(Charsets.UTF_8);
            deleteRange(beginKey, endKey);
        }, INITIAL_DELAY_MINUTES, PERIOD_MINUTES, TimeUnit.MINUTES);
        LOGGER.info("DeleteBgWorker has started, initialDelayInMinutes:{}", INITIAL_DELAY_MINUTES);
    }

    private void deleteRange(final byte[] beginKey, final byte[] endKey) {
        LOGGER.info("deleteRange start, beginKey:{}, endKey:{}", new String(beginKey), new String(endKey));
        final long start = System.currentTimeMillis();
        RDB.deleteFilesInRange(CFManager.CFH_DEFAULT, beginKey, endKey);
        LOGGER.info("deleteRange end, beginKey:{}({}), endKey:{}({}), cost:{}ms",
                new String(beginKey), formatter.format(Long.parseLong(new String(beginKey)) * 1000),
                new String(endKey), formatter.format(Long.parseLong(new String(endKey)) * 1000),
                System.currentTimeMillis() - start);
    }

    public void stop() {
        SCHEDULE.shutdownNow();
        while (!SCHEDULE.isShutdown()) {
            LOGGER.info("DeleteBgWorker is shutting down!");
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                LOGGER.info("DeleteBgWorker was forced to shutdown, err:{}", e.getMessage(), e);
            }
        }
        LOGGER.info("DeleteBgWorker was shutdown!");
    }

    public static DeleteBgWorker getInstance() {
        if (instance == null) {
            synchronized (DeleteBgWorker.class) {
                if (instance == null) {
                    instance = new DeleteBgWorker();
                }
            }
        }
        return instance;
    }
}