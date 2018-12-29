package com.xiaojukeji.carrera.cproxy.actions;

import com.google.common.collect.Sets;
import com.xiaojukeji.carrera.config.v4.cproxy.UpstreamTopic;
import com.xiaojukeji.carrera.cproxy.concurrent.CarreraExecutors;
import com.xiaojukeji.carrera.cproxy.config.ConsumerGroupConfig;
import com.xiaojukeji.carrera.cproxy.consumer.UpstreamJob;
import com.xiaojukeji.carrera.cproxy.actions.http.CarreraAsyncRequest;
import com.xiaojukeji.carrera.cproxy.utils.LogUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;


public class AsyncHttpAction implements Action {
    private static final Logger LOGGER = LoggerFactory.getLogger(AsyncHttpAction.class);
    private static final ScheduledExecutorService SCHEDULER = CarreraExecutors.newScheduledThreadPool(32, "AsyncHttpRetryThread");
    private final ScheduledFuture<?> retryFuture;

    private Map<String/*topic*/, AtomicInteger/*slowDown状态的持续消息数.*/> permitsMap;

    private Map<String/*topic*/, BlockingQueue<CarreraAsyncRequest>> limitedOnRetryRequests;
    private ConsumerGroupConfig config;
    private final ScheduledFuture<?> timeoutCheckerFuture;
    private final Set<CarreraAsyncRequest> inflightRequests = Sets.newConcurrentHashSet();

    public AsyncHttpAction(ConsumerGroupConfig config) {
        this.config = config;
        limitedOnRetryRequests = new HashMap<>();
        permitsMap = new HashMap<>();
        for (UpstreamTopic upstreamTopic : config.getGroupConfig().getTopics()) {
            String topic = upstreamTopic.getTopic();
            permitsMap.put(topic, new AtomicInteger());
            limitedOnRetryRequests.put(topic, new LinkedBlockingQueue<>());
        }
        retryFuture = SCHEDULER.scheduleAtFixedRate(this::resendLimitedRequests, 5, 5, TimeUnit.SECONDS);
        timeoutCheckerFuture = SCHEDULER.scheduleAtFixedRate(this::checkTimeout, 30, 30, TimeUnit.SECONDS);
    }

    /**
     * 针对AsyncHttpClient可能丢失请求做兜底检查。
     */
    private void checkTimeout() {
        try {
            inflightRequests.forEach(CarreraAsyncRequest::checkTimeout);
        } catch (Exception e) {
            LogUtils.logErrorInfo("AsyncHttp_error", "AsyncHttpAction.checkTimeout, group=" + config.getGroupBrokerCluster(), e);
        }
    }

    /**
     * 处理limitedOnRetryRequests剩余的未被发送的消息。
     */
    public void resendLimitedRequests() {
        try {
            limitedOnRetryRequests.values().forEach(queue -> {
                CarreraAsyncRequest request = queue.poll();
                if (request != null) {
                    request.tryRequest();
                }
            });
        } catch (Exception e) {
            LogUtils.logErrorInfo("AsyncHttp_error", "AsyncHttpAction.resendLimitedRequests, group=" + config.getGroupBrokerCluster(), e);
        }

    }

    @Override
    public Status act(UpstreamJob job) {
        if (CollectionUtils.isEmpty(job.getUrls())) {
            return Status.FAIL;
        }
        String topic = job.getTopic();
        CarreraAsyncRequest request = new CarreraAsyncRequest(job, permitsMap.get(topic),
                config.getGroupBrokerCluster(), limitedOnRetryRequests.get(topic), inflightRequests, SCHEDULER);
        request.tryRequest();
        return Status.ASYNCHRONIZED;
    }

    @Override
    public void shutdown() {
        LOGGER.info("shutdown AsyncAction for group={}", config.getGroupBrokerCluster());
        if (retryFuture != null) {
            if (!retryFuture.cancel(true)) {
                LOGGER.warn("cancel retryFuture failed! group={}", config.getGroupBrokerCluster());
            }
        }
        if (timeoutCheckerFuture != null) {
            if (!timeoutCheckerFuture.cancel(true)) {
                LOGGER.warn("cancel timeoutCheckerFuture failed! group={}", config.getGroupBrokerCluster());
            }
        }
    }
}