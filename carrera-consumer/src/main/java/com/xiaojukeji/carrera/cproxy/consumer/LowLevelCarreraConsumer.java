package com.xiaojukeji.carrera.cproxy.consumer;

import com.google.common.util.concurrent.RateLimiter;
import com.xiaojukeji.carrera.config.v4.cproxy.UpstreamTopic;
import com.xiaojukeji.carrera.cproxy.consumer.exception.CarreraClientException;
import com.xiaojukeji.carrera.thrift.consumer.AckResult;
import com.xiaojukeji.carrera.thrift.consumer.FetchRequest;
import com.xiaojukeji.carrera.thrift.consumer.FetchResponse;
import com.xiaojukeji.carrera.thrift.consumer.QidResponse;
import com.xiaojukeji.carrera.cproxy.concurrent.CarreraExecutors;
import com.xiaojukeji.carrera.cproxy.config.ConsumerGroupConfig;
import com.xiaojukeji.carrera.cproxy.consumer.lowlevel.Fetcher;
import com.xiaojukeji.carrera.cproxy.consumer.lowlevel.KafkaFetcher;
import com.xiaojukeji.carrera.cproxy.consumer.lowlevel.RmqFetcher;
import com.xiaojukeji.carrera.cproxy.server.AckChain;
import com.xiaojukeji.carrera.cproxy.server.FetchChain;
import com.xiaojukeji.carrera.cproxy.utils.LogUtils;
import com.xiaojukeji.carrera.cproxy.utils.MetricUtils;
import com.xiaojukeji.carrera.cproxy.utils.TimeUtils;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.slf4j.LoggerFactory.getLogger;


public class LowLevelCarreraConsumer extends CarreraConsumer {
    public static final Logger LOGGER = getLogger(LowLevelCarreraConsumer.class);
    private static final long CONSUMER_TIMEOUT = 30 * 1000;

    private final Map<String/*consumerId*/, Fetcher> consumerMap;
    private final ScheduledExecutorService es;
    private volatile boolean isShutdown = false;
    private ConcurrentHashMap<String/*topic*/, RateLimiter> rateLimiterMap = new ConcurrentHashMap<>();

    public LowLevelCarreraConsumer(ConsumerGroupConfig config) {
        super(config);
        for (UpstreamTopic upstreamTopic : config.getGroupConfig().getTopics()) {
            rateLimiterMap.put(upstreamTopic.getTopic(), RateLimiter.create(upstreamTopic.getMaxTps()));
        }
        consumerMap = new ConcurrentHashMap<>();
        es = CarreraExecutors.newScheduledThreadPool(config.getGroupConfig().getAsyncThreads(),
                "LowLevelCarreraConsumer-PoolThread-" + getConfig().getGroupBrokerCluster());
        es.scheduleWithFixedDelay(this::checkTimeoutConsumer, 1, CONSUMER_TIMEOUT / 2, TimeUnit.MILLISECONDS);
    }

    @Override
    public void start() throws CarreraClientException {
        //DO NOTHING
        LogUtils.logMainInfo("LowLevelConsumer.start,groupCluster:{} threadNum:{}",
                getConfig().getGroupBrokerCluster(), getConfig().getGroupConfig().getAsyncThreads());
    }

    @Override
    public void stop() {
        long start = TimeUtils.getCurTime();
        isShutdown = true;
        List<Fetcher> fetchers;
        synchronized (this) {
            fetchers = new ArrayList<>(consumerMap.values());
            consumerMap.clear();
        }
        fetchers.forEach(Fetcher::shutdown);
        es.shutdown();
        LogUtils.logMainInfo("LowLevelConsumer.stop,groupCluster:{},fetchers:size={},{}, elapse={}",
                getConfig().getGroupBrokerCluster(), fetchers.size(), fetchers, TimeUtils.getElapseTime(start));
    }

    @Override
    public void logActionMetric() {
        consumerMap.values().forEach(Fetcher::logMetrics);
    }

    public void fetch(FetchChain fetchAction) {
        final long taskSubmitTime = TimeUtils.getCurTime();
        es.execute(() -> {
            final FetchRequest request = fetchAction.getRequest();

            Fetcher fetcher = getFetcher(request.consumerId);
            if (fetcher == null) {
                fetchAction.doNext();
                return;
            }
            try {
                long startFetchTs = TimeUtils.getCurTime();
                FetchResponse response = fetcher.fetch(request);
                long elapse = TimeUtils.getElapseTime(startFetchTs);
                int msgNumber = 0;
                if (response != null && response.getResultsSize() != 0) {
                    fetchAction.saveResponse(response);
                    for (QidResponse qidResponse : response.getResults()) {
                        msgNumber += qidResponse.getMessagesSize();
                        MetricUtils.incQpsCount(request.getGroupId(), qidResponse.getTopic(),
                                "LowLevel", "SUCCESS", qidResponse.getMessagesSize());
                        MetricUtils.incPullStatCount(request.getGroupId(), qidResponse.getTopic(),
                                qidResponse.getQid(), "pulled", qidResponse.getMessagesSize());
                    }
                }
                if (LOGGER.isDebugEnabled() && msgNumber > 0) {
                    LOGGER.debug("fetch cost:{}ms, request={}, response={}", elapse, request, response);
                } else if (msgNumber > 0) {
                    LOGGER.info("fetch cost:{}ms, request={}, response.msg.num={}", elapse, request, msgNumber);
                }

                long taskSubmitElapse = TimeUtils.getElapseTime(taskSubmitTime);
                if (taskSubmitElapse > 1000) {
                    LOGGER.warn("fetch request total cost more than {} , fetch cost:{}", taskSubmitElapse, elapse);
                }
                fetchAction.doNext();
            } catch (Exception e) {
                LogUtils.logErrorInfo("lowlevel_fetch_error","error while fetch in lowlevel. err.msg:{}", e.getMessage(), e);
                fetchAction.onError(e);
            }
        });
    }

    public void ack(AckChain ackChain) {
        es.execute(() -> {
            AckResult result = ackChain.getRequest();
            try {
                Fetcher fetcher = getFetcher(result.consumerId);
                if (fetcher == null || !fetcher.ack(result)) {
                    ackChain.saveResponse(false);
                }
                ackChain.doNext();
            } catch (Exception e) {
                LogUtils.logErrorInfo("lowlevel_ack_error", "error while do ack in lowlevel, err.msg:{}", e.getMessage(), e);
                ackChain.onError(e);
            }
        });
    }

    private Fetcher getFetcher(String consumerId) {
        if (isShutdown) {
            return null;
        }
        return consumerMap.computeIfAbsent(consumerId, this::createConsumer);
    }

    private void checkTimeoutConsumer() {
        consumerMap.forEach((cid, rmqFetcher) -> {
            long elapse = TimeUtils.getElapseTime(rmqFetcher.getLastFetchTimestamp());
            if (elapse > CONSUMER_TIMEOUT) {
                LogUtils.logMainInfo("consumer cid={} in {} timeout, elapse={}",
                        cid, getConfig().getGroupBrokerCluster(), elapse);
                shutdownFetcher(cid);
            }
        });
    }

    private synchronized void shutdownFetcher(String cid) {
        LogUtils.logMainInfo("LowLevelConsumer.shutdownFetcher,groupCluster:{},cid={}",
                getConfig().getGroupBrokerCluster(), cid);
        es.execute(() -> {
            long start = TimeUtils.getCurTime();
            Fetcher fetcher = consumerMap.get(cid);
            if (fetcher != null) {
                fetcher.shutdown();
                consumerMap.remove(cid);
            }
            LogUtils.logMainInfo("shutdown consumer truly! groupCluster={},fetcher={}, cost={}",
                    getConfig().getGroupBrokerCluster(), fetcher, TimeUtils.getElapseTime(start));
        });

    }

    private synchronized Fetcher createConsumer(String consumerId) {
        if (isShutdown) return null;
        if (consumerMap.size() > getConfig().getGroupConfig().getAsyncThreads()) return null;
        LogUtils.logMainInfo("create Fetcher for groupCluster={},cid={}", getConfig().getGroupBrokerCluster(), consumerId);
        Fetcher fetcher;
        if (getConfig().getcProxyConfig().getKafkaConfigs().containsKey(getConfig().getBrokerCluster())) {
            fetcher = new KafkaFetcher(getConfig(), this.es, consumerId, rateLimiterMap);
        } else {
            fetcher = new RmqFetcher(getConfig(), consumerId, rateLimiterMap);
        }
        final Fetcher localFetcher = fetcher;
        es.execute(new Runnable() {
            @Override
            public void run() {
                if (isShutdown)
                    return;
                if (!localFetcher.start()) {
                    es.schedule(this, 1000, TimeUnit.MILLISECONDS);
                }
            }
        });
        return fetcher;
    }

    @Override
    public String toString() {
        return "LowLevelCarreraConsumer#" + getConfig().getGroupBrokerCluster();
    }
}