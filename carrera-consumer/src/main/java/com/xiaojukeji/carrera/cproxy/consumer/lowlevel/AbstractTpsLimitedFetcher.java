package com.xiaojukeji.carrera.cproxy.consumer.lowlevel;

import com.google.common.util.concurrent.RateLimiter;
import com.xiaojukeji.carrera.thrift.consumer.FetchRequest;
import com.xiaojukeji.carrera.thrift.consumer.FetchResponse;
import com.xiaojukeji.carrera.cproxy.utils.MetricUtils;
import com.xiaojukeji.carrera.cproxy.utils.TimeUtils;

import java.util.concurrent.ConcurrentHashMap;


public abstract class AbstractTpsLimitedFetcher implements Fetcher {

    private volatile long lastFetchTimestamp = TimeUtils.getCurTime();
    private ConcurrentHashMap<String/*topic*/, RateLimiter> rateLimiterMap;

    AbstractTpsLimitedFetcher(ConcurrentHashMap<String/*topic*/, RateLimiter> rateLimiterMap) {
        this.rateLimiterMap = rateLimiterMap;
    }

    @Override
    public final FetchResponse fetch(FetchRequest request) {
        lastFetchTimestamp = TimeUtils.getCurTime();
        FetchResponse response = trueFetch(request);
        if (response != null && response.getResults() != null) {
            response.results.forEach(qid -> {
                if (qid.getMessagesSize() > 0) {
                    if (rateLimiterMap.get(qid.getTopic()).acquire(qid.getMessagesSize()) > 0.0) {
                        MetricUtils.incRateLimiterCount(request.getGroupId(), qid.getTopic());
                    }
                }
            });
        }
        return response;
    }

    @Override
    public long getLastFetchTimestamp() {
        return lastFetchTimestamp;
    }

    protected abstract FetchResponse trueFetch(FetchRequest request);
}