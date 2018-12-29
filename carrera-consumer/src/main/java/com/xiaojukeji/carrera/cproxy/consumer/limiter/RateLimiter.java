package com.xiaojukeji.carrera.cproxy.consumer.limiter;

import com.xiaojukeji.carrera.config.v4.GroupConfig;
import com.xiaojukeji.carrera.config.v4.cproxy.UpstreamTopic;
import com.xiaojukeji.carrera.cproxy.utils.MetricUtils;
import com.xiaojukeji.carrera.cproxy.utils.TimeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;


public class RateLimiter {

    private static final Logger LOGGER = LoggerFactory.getLogger(RateLimiter.class);

    private String groupName;

    private ConcurrentHashMap<String, com.google.common.util.concurrent.RateLimiter> rateLimiterMap = new ConcurrentHashMap<>();

    public RateLimiter(GroupConfig config) {
        this.groupName = config.getGroup();
        for (UpstreamTopic upstreamTopic : config.getTopics()) {
            rateLimiterMap.put(upstreamTopic.getTopic(), com.google.common.util.concurrent.RateLimiter.create(upstreamTopic.getMaxTps()));
        }
    }

    /**
     * 会阻塞
     * @param topic
     * @param permits
     * @return
     * @throws InterruptedException
     */
    public void doBlockLimit(String topic, int permits) throws InterruptedException {
        if (rateLimiterMap.get(topic) == null) {
            return;
        }
        long start = TimeUtils.getCurTime();
        double wait = rateLimiterMap.get(topic).acquire(permits);
        if (wait > 0.0) {
            MetricUtils.incRateLimiterCount(groupName, topic);
        }
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("doBlockLimit blocked {} ms in submit, rateLimiter cost {}ms, group:{}.",
                    TimeUtils.getElapseTime(start), wait * 1000, groupName);
        }
    }

    /**
     * 限流 非阻塞
     * @throws InterruptedException
     */
    public boolean doNonBlockLimit(String topic, int permits) {
        if (rateLimiterMap.get(topic) == null) {
            return true;
        }
        if (!rateLimiterMap.get(topic).tryAcquire(permits)) {
            MetricUtils.incRateLimiterCount(groupName, topic);
            LOGGER.trace("doNonBlockLimit rateLimit is effective. group:{}", groupName);
            return false;
        }

        return true;
    }

    /**
     * @param topic
     * @param newRate
     * @return
     */
    public void adjustRateLimit(String topic, double newRate) {
        com.google.common.util.concurrent.RateLimiter oldLimiter = rateLimiterMap.get(topic);
        if (oldLimiter == null) {
            return;
        }
        oldLimiter.setRate(newRate);
    }
}