package com.xiaojukeji.chronos.services;

import com.xiaojukeji.chronos.autobatcher.Batcher;
import com.xiaojukeji.chronos.config.ConfigManager;
import com.xiaojukeji.chronos.config.DbConfig;
import com.xiaojukeji.chronos.ha.MasterElection;
import com.xiaojukeji.chronos.utils.FileIOUtils;
import com.xiaojukeji.chronos.utils.TsUtils;
import com.xiaojukeji.chronos.utils.ZkUtils;
import com.xiaojukeji.chronos.utils.Constants;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


public class MetaService {
    private static final Logger LOGGER = LoggerFactory.getLogger(MetaService.class);

    private static volatile long seekTimestamp = -1;
    private static volatile long zkSeekTimestamp = -1;
    private static volatile Map<String, Long> zkQidOffsets = new ConcurrentHashMap<>();
    private static final DbConfig dbConfig = ConfigManager.getConfig().getDbConfig();

    private static final ScheduledExecutorService SCHEDULER = new ScheduledThreadPoolExecutor(1,
            new BasicThreadFactory.Builder().namingPattern("offset-seektimestamp-schedule-%d").daemon(true).build());

    public static void load() {
        final long start = System.currentTimeMillis();
        if (seekTimestamp == -1) {
            seekTimestamp = loadSeekTimestampFromFile();
        }
        final long cost = System.currentTimeMillis() - start;
        LOGGER.info("succ load seekTimestamp, seekTimestamp:{}, cost:{}ms", seekTimestamp, cost);

        SCHEDULER.scheduleWithFixedDelay(() -> {
            // 如果是master则拉取并上报zk offset和seekOffset
            if (MasterElection.isMaster()) {
                MqConsumeStatService.getInstance().uploadOffsetsToZk();
                uploadSeekTimestampToZk();
            }
        }, 5, 1, TimeUnit.SECONDS);
    }

    private static long loadSeekTimestampFromFile() {
        String seekTimestampStr = FileIOUtils.readFile2String(dbConfig.getSeekTimestampPath());
        if (StringUtils.isBlank(seekTimestampStr)) {
            final long initSeekTimestamp = TsUtils.genTS();
            boolean result = FileIOUtils.writeFileFromString(dbConfig.getSeekTimestampPath(), String.valueOf(initSeekTimestamp));
            if (result) {
                LOGGER.info("init seekTimestamp and succ save, current seekTimestamp:{}", initSeekTimestamp);
            } else {
                LOGGER.error("init seekTimestamp and fail to save, current seekTimestamp:{}", initSeekTimestamp);
            }
            return initSeekTimestamp;
        }
        LOGGER.info("succ load seekTimestamp from file, seekTimestamp:{}", Long.parseLong(seekTimestampStr));
        return Long.parseLong(seekTimestampStr);
    }

    public static long getSeekTimestamp() {
        return seekTimestamp;
    }

    /**
     * 此处的lock不能去掉
     * 判断消息超时时
     */
    public static void nextSeekTimestamp() {
        Batcher.lock.lock();
        try {
            seekTimestamp++;
            boolean result = FileIOUtils.writeFileFromString(dbConfig.getSeekTimestampPath(), String.valueOf(seekTimestamp));
            if (result) {
                LOGGER.info("incr seekTimestamp and succ save, next seekTimestamp:{}", seekTimestamp);
            } else {
                LOGGER.error("incr seekTimestamp and fail to save, next seekTimestamp:{}", seekTimestamp);
            }
        } finally {
            Batcher.lock.unlock();
        }
    }

    public static void uploadSeekTimestampToZk() {
        String seekTimestampStr = String.valueOf(MetaService.getSeekTimestamp());
        ZkUtils.createOrUpdateValue(Constants.SEEK_TIMESTAMP_ZK_PATH, seekTimestampStr);
        LOGGER.debug("upload seekTimestamp to zk, seekTimestamp:{}", seekTimestampStr);
    }

    public static Map<String, Long> getZkQidOffsets() {
        return zkQidOffsets;
    }

    public static void setZkQidOffsets(Map<String, Long> zkQidOffsets) {
        MetaService.zkQidOffsets = zkQidOffsets;
    }

    public static long getZkSeekTimestamp() {
        return zkSeekTimestamp;
    }

    public static void setZkSeekTimestamp(long zkSeekTimestamp) {
        MetaService.zkSeekTimestamp = zkSeekTimestamp;
    }
}