package com.xiaojukeji.carrera.cproxy.consumer.limiter;

import com.xiaojukeji.carrera.config.v4.GroupConfig;
import com.xiaojukeji.carrera.cproxy.utils.LogUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class LimiterMgr implements ILimiter {

    private transient static final Logger LOGGER = LoggerFactory.getLogger(LimiterMgr.class);

    private Map<String/*groupBrokerCluster*/, RateLimiter> groupHLRateLimiter = new ConcurrentHashMap<>();
    private Map<String/*groupBrokerCluster*/, CapacityLimiter> groupHLCapcityLimiter = new ConcurrentHashMap<>();
    private Map<String/*groupBrokerCluster*/, HttpRateLimiter> httpRateLimiterMap = new ConcurrentHashMap<>();

    private LimiterMgr() {}
    private static class SINGLETON {
        public static LimiterMgr INSTANCE = new LimiterMgr();
    }

    public static LimiterMgr getInstance() {
        return SINGLETON.INSTANCE;
    }

    @Override
    public void initLimiter(String groupCluster, GroupConfig groupConfig) {
        //lowlevel有独立的限流策略
        if (groupConfig.checkLowLevel()) {
            return;
        }
        RateLimiter rateLimiter = new RateLimiter(groupConfig);
        CapacityLimiter capacityLimiter = new CapacityLimiter(groupConfig);
        HttpRateLimiter httpRateLimiter = new HttpRateLimiter(groupConfig);
        groupHLRateLimiter.put(groupCluster, rateLimiter);
        groupHLCapcityLimiter.put(groupCluster, capacityLimiter);
        httpRateLimiterMap.put(groupCluster, httpRateLimiter);
    }

    @Override
    public void doBlockLimit(String groupCluster, String topic, int permits) throws InterruptedException{
        RateLimiter rateLimiter;
        if ((rateLimiter = groupHLRateLimiter.get(groupCluster)) != null) {
            rateLimiter.doBlockLimit(topic, permits);
        }

        CapacityLimiter capacityLimiter;
        if ((capacityLimiter = groupHLCapcityLimiter.get(groupCluster)) != null) {
            capacityLimiter.doBlockLimit(topic, permits);
        }
    }

    @Override
    public boolean doNonBlockLimit(String groupCluster, String topic, int permits) {
        RateLimiter rateLimiter;
        if ((rateLimiter = groupHLRateLimiter.get(groupCluster)) != null) {
            if (!rateLimiter.doNonBlockLimit(topic, permits)) {
                return false;
            }
        }
        CapacityLimiter capacityLimiter;
        if ((capacityLimiter = groupHLCapcityLimiter.get(groupCluster)) != null) {
            return capacityLimiter.doNonBlockLimit(topic, permits);
        }
        return true;
    }

    /**
     * 释放容量
     * @param groupCluster
     * @param topic
     * @param permits
     * @return
     */
    @Override
    public void release(String groupCluster, String topic, int permits) {
        CapacityLimiter capacityLimiter;
        if ((capacityLimiter = groupHLCapcityLimiter.get(groupCluster)) != null) {
            capacityLimiter.release(topic, permits);
            return;
        }
    }

    public int availablePermits(String groupCluster, String topic) {
        CapacityLimiter capacityLimiter;
        if ((capacityLimiter = groupHLCapcityLimiter.get(groupCluster)) != null) {
            return capacityLimiter.availablePermits(topic);
        }
        return 0;
    }

    /**
     * 调整限流
     * @param groupCluster
     * @param topic
     * @param threshold
     * @return
     */
    public void adjustThreshold(String groupCluster, String topic, Double threshold, Double httpThreshold) {
        LogUtils.logMainInfo("adjust limit threshold groupCluster:{}, topic:{}, threshold:{}, httpThreshold:{}.",
                groupCluster, topic, threshold, httpThreshold);
        RateLimiter rateLimiter;
        if ((rateLimiter = groupHLRateLimiter.get(groupCluster)) != null) {
            rateLimiter.adjustRateLimit(topic, threshold);
        }

        HttpRateLimiter httpRateLimiter;
        if ((httpRateLimiter = httpRateLimiterMap.get(groupCluster)) != null) {
            httpRateLimiter.adjustRateLimit(topic, httpThreshold);
        }
    }

    public double httpAcquire(String groupCluster, String topic, int permits) {
        HttpRateLimiter httpRateLimiter;
        if ((httpRateLimiter = httpRateLimiterMap.get(groupCluster)) != null) {
            return httpRateLimiter.acquire(topic, permits);
        }
        return 0;
    }

    public boolean httpTryAcquire(String groupCluster, String topic, int permits) {
        HttpRateLimiter httpRateLimiter;
        if ((httpRateLimiter = httpRateLimiterMap.get(groupCluster)) != null) {
            return httpRateLimiter.tryAcquire(topic, permits);
        }
        return true;
    }

    public double getHttpRate(String groupCluster, String topic) {
        HttpRateLimiter httpRateLimiter;
        if ((httpRateLimiter = httpRateLimiterMap.get(groupCluster)) != null) {
            return httpRateLimiter.getRate(topic);
        }
        return 0;
    }

    public void close(String groupCluster) {
        groupHLRateLimiter.remove(groupCluster);
        if (groupHLCapcityLimiter.get(groupCluster) != null) {
            groupHLCapcityLimiter.get(groupCluster).close();
        }
        groupHLCapcityLimiter.remove(groupCluster);
        httpRateLimiterMap.remove(groupCluster);
    }
}