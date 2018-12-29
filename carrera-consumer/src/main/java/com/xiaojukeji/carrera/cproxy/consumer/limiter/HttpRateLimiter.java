package com.xiaojukeji.carrera.cproxy.consumer.limiter;

import com.google.common.util.concurrent.RateLimiter;
import com.xiaojukeji.carrera.config.v4.GroupConfig;
import com.xiaojukeji.carrera.config.v4.cproxy.UpstreamTopic;
import com.xiaojukeji.carrera.cproxy.actions.ActionBuilder;

import java.util.concurrent.ConcurrentHashMap;


public class HttpRateLimiter {

    private ConcurrentHashMap<String, RateLimiter> httpRateLimiterMap = new ConcurrentHashMap<>();

    public HttpRateLimiter(GroupConfig groupConfig) {
        for (UpstreamTopic upstreamTopic : groupConfig.getTopics()) {
            double httpMaxTps = upstreamTopic.getHttpMaxTps() > 0 ? upstreamTopic.getHttpMaxTps() : upstreamTopic.getMaxTps();
            if (upstreamTopic.getActions().contains(ActionBuilder.ASYNC_HTTP)) {
                httpRateLimiterMap.put(upstreamTopic.getTopic(), RateLimiter.create(httpMaxTps));
            }
        }
    }

    /**
     * @param topic
     * @param httpNewRate
     * @return
     */
    public void adjustRateLimit(String topic, double httpNewRate) {
        RateLimiter oldLimiter = httpRateLimiterMap.get(topic);
        if (oldLimiter == null) {
            return;
        }
        oldLimiter.setRate(httpNewRate);
    }

    public boolean tryAcquire(String topic, int permits) {
        RateLimiter limiter = httpRateLimiterMap.get(topic);
        return limiter == null || limiter.tryAcquire(permits);
    }

    public double acquire(String topic, int permits) {
        RateLimiter limiter = httpRateLimiterMap.get(topic);
        if (limiter != null) {
            return limiter.acquire(permits);
        }
        return 0;
    }

    public double getRate(String topic) {
        RateLimiter limiter = httpRateLimiterMap.get(topic);
        if (limiter != null) {
            return limiter.getRate();
        }
        return 0;
    }
}