package com.xiaojukeji.carrera.cproxy.consumer;

import com.xiaojukeji.carrera.config.v4.CProxyConfig;
import com.xiaojukeji.carrera.config.v4.GroupConfig;
import com.xiaojukeji.carrera.config.v4.cproxy.UpstreamTopic;
import com.xiaojukeji.carrera.cproxy.consumer.exception.CarreraClientException;
import com.xiaojukeji.carrera.cproxy.consumer.handler.AsyncMessageHandler;
import com.xiaojukeji.carrera.cproxy.consumer.limiter.LimiterMgr;
import com.xiaojukeji.carrera.cproxy.consumer.offset.CommitLagLimiter;
import com.xiaojukeji.carrera.cproxy.consumer.offset.ConsumeOffsetTracker;
import com.xiaojukeji.carrera.cproxy.consumer.offset.OffsetTrackSnapshot;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Collectors;


public abstract class BaseCarreraConsumer {

    protected static final Logger LOGGER = LoggerFactory.getLogger(BaseCarreraConsumer.class);

    protected String brokerCluster;

    protected String group;

    protected GroupConfig groupConfig;

    protected CProxyConfig cProxyConfig;

    protected AsyncMessageHandler handler;

    volatile boolean autoCommit = false;

    protected ScheduledExecutorService autoOffsetCommitService;

    protected volatile CommitLagLimiter commitLagLimiter;

    protected ConsumeOffsetTracker tracker;

    protected volatile boolean isRunning;

    public BaseCarreraConsumer(String brokerCluster, String group, GroupConfig groupConfig,
                               CProxyConfig cProxyConfig, AsyncMessageHandler handler, Map<String, Long> maxCommitLagMap) {
        this.brokerCluster = brokerCluster;
        this.group = group;
        this.groupConfig = groupConfig;
        this.cProxyConfig = cProxyConfig;
        this.handler = handler;

        if (maxCommitLagMap != null) {
            commitLagLimiter = new CommitLagLimiter(maxCommitLagMap);
        }

        LimiterMgr.getInstance().initLimiter(getGroupBrokerCluster(), groupConfig);
    }

    public String getGroupName() {
        return group;
    }

    public void startConsume() throws CarreraClientException {
        try {
            doStart();
        } catch (Throwable e) {
            throw new CarreraClientException("start consumer failed!", e);
        }
    }

    protected abstract void doStart() throws Exception;

    public abstract void commitOffset();

    public abstract void onConsumeFailed(CommonMessage commonMessage, ConsumeContext context);

    public void enableOffsetAutoCommit(ScheduledExecutorService autoOffsetCommitService) {
        if (autoCommit) {
            throw new RuntimeException("auto commit offset already enabled!");
        }
        this.autoOffsetCommitService = autoOffsetCommitService;
        autoCommit = true;
    }

    protected void handleMessage(CommonMessage commonMessage, ConsumeContext context) throws InterruptedException {
        tracker.trackStart(commonMessage, context);
        handler.process(commonMessage, context, success -> onConsumeFinished(commonMessage, context, success));
    }

    protected void onConsumeFinished(CommonMessage commonMessage, ConsumeContext context, boolean success) {
        if (!success) {
            onConsumeFailed(commonMessage, context);
        }
        tracker.trackFinish(commonMessage, context);
        if (commitLagLimiter != null) {
            commitLagLimiter.release(tracker, commonMessage.getTopic(), context);
        }
        LimiterMgr.getInstance().release(getGroupBrokerCluster(), commonMessage.getTopic(), 1);
        logConsumeResult(success, commonMessage, context);
    }

    protected void logConsumeResult(boolean success, CommonMessage commonMessage, ConsumeContext context) {
        long elapse = System.currentTimeMillis() - context.getStartTime();
        String result;
        if (!isRunning) {
            result = "IGNORED";
        } else {
            result = success ? "SUCCESS" : "FAILED";
        }
        LOGGER.info("Consume Result {}! Message:{}, Context:{}, Cost:{}ms",
                result, commonMessage.info(), context.info(), elapse);
    }

    void logConsumeException(CommonMessage commonMessage, ConsumeContext context, Throwable throwable) {
        long elapse = System.currentTimeMillis() - context.getStartTime();
        LOGGER.error(String.format("Consume Result FAILED! Message:%s, Context:%s, Cost:%dms",
                commonMessage.info(), context.info(), elapse), throwable);
    }

    public void shutdown() {
        if (commitLagLimiter != null) {
            commitLagLimiter.shutdown();
        }
        LimiterMgr.getInstance().close(getGroupBrokerCluster());
    }

    public Map<String, Set<String>> getCurrentQids() {
        Map<String, Set<String>> qids = new HashMap<>();
        for (UpstreamTopic topicConf : groupConfig.getTopics()) {
            qids.put(topicConf.getTopic(), getCurrentTopicQids(topicConf.getTopic()));
        }
        return qids;
    }

    public abstract Set<String> getCurrentTopicQids(String topic);

    protected List<OffsetTrackSnapshot> removeUnsubscriptedQids(List<OffsetTrackSnapshot> snapshots) {
        Map<String, Set<String>> subscribedQids = getCurrentQids();
        snapshots = snapshots.stream().filter(snapshot -> {
            Set<String> qids = subscribedQids.get(snapshot.getTopic());
            return CollectionUtils.isNotEmpty(qids) && qids.contains(snapshot.getQid());
        }).collect(Collectors.toList());
        return snapshots;
    }

    protected String getGroupBrokerCluster() {
        return group + "@" + brokerCluster;
    }

    public String getBrokerCluster() {
        return brokerCluster;
    }
}