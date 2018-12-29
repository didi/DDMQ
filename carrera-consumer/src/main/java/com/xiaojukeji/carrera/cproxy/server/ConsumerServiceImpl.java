package com.xiaojukeji.carrera.cproxy.server;

import com.xiaojukeji.carrera.config.v4.CProxyConfig;
import com.xiaojukeji.carrera.config.v4.GroupConfig;
import com.xiaojukeji.carrera.config.v4.cproxy.UpstreamTopic;
import com.xiaojukeji.carrera.cproxy.consumer.offset.CarreraOffsetManager;
import com.xiaojukeji.carrera.thrift.consumer.AckResult;
import com.xiaojukeji.carrera.thrift.consumer.ConsumeResult;
import com.xiaojukeji.carrera.thrift.consumer.ConsumeStats;
import com.xiaojukeji.carrera.thrift.consumer.ConsumeStatsRequest;
import com.xiaojukeji.carrera.thrift.consumer.ConsumerService;
import com.xiaojukeji.carrera.thrift.consumer.Context;
import com.xiaojukeji.carrera.thrift.consumer.FetchRequest;
import com.xiaojukeji.carrera.thrift.consumer.Message;
import com.xiaojukeji.carrera.thrift.consumer.PullRequest;
import com.xiaojukeji.carrera.thrift.consumer.PullResponse;
import com.xiaojukeji.carrera.cproxy.concurrent.CarreraExecutors;
import com.xiaojukeji.carrera.cproxy.config.ConsumerGroupConfig;
import com.xiaojukeji.carrera.cproxy.consumer.ConfigManager;
import com.xiaojukeji.carrera.cproxy.consumer.ConsumerManager;
import com.xiaojukeji.carrera.cproxy.actions.util.PullBuffer;
import com.xiaojukeji.carrera.cproxy.utils.ConfigUtils;
import com.xiaojukeji.carrera.cproxy.utils.LogUtils;
import com.xiaojukeji.carrera.cproxy.utils.MetricUtils;
import com.xiaojukeji.carrera.cproxy.utils.StringUtils;
import com.xiaojukeji.carrera.cproxy.utils.TimeUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.thrift.TException;
import org.apache.thrift.async.AsyncMethodCallback;
import org.apache.thrift.server.ThriftContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


public class ConsumerServiceImpl implements ConsumerService.AsyncIface {

    private static final long STATS_EXPIRED_TIME = 24 * 3600 * 1000;
    private static Logger LOGGER = LoggerFactory.getLogger(ConsumerServiceImpl.class);
    private static final Logger METRIC_LOGGER = LogUtils.METRIC_LOGGER;
    private static final int STATS_INTERVAL = 5;
    private static final int MIN_LINGER_TIME = 20;
    private static final PullResponse EMPTY_PULL_RESPONSE = new PullResponse(new Context(), Collections.emptyList());

    private static final ThreadLocal<ThriftContext> thriftContext = new ThreadLocal<>();
    private ConcurrentMap<String/*Group*/, PullBuffer> bufferMap = new ConcurrentHashMap<>();

    private Map<String/*group#topic*/, Map<String/*clientIP*/, PullStats>> pullStats = new ConcurrentHashMap<>();

    private ScheduledExecutorService pullScheduler;
    private ConsumerManager consumerManager = ConsumerManager.getInstance();
    private ExecutorService executor;

    private ConsumerServiceImpl() {
        ScheduledExecutorService scheduler = CarreraExecutors.newSingleThreadScheduledExecutor("PullStatsThread");
        scheduler.scheduleAtFixedRate(this::logStats, STATS_INTERVAL, STATS_INTERVAL, TimeUnit.SECONDS);
        pullScheduler = CarreraExecutors.newScheduledThreadPool(32, "PullRequestTimeoutChecker");
        executor = CarreraExecutors.newFixedThreadPool(32, "doGetConsumeStatsWorker", 100000 );
    }

    private void logStats() {
        PullStats totalStats = new PullStats();
        pullStats.forEach((groupTopic, groupPullStatsMap) -> {
            PullStats groupTotalStats = new PullStats();
            groupPullStatsMap.forEach((clientAddress, stats) -> {
                if (stats.isNotEmpty()) {
                    int req = stats.requestCnt.getAndSet(0);
                    int pulled = stats.pulledMsg.getAndSet(0);
                    int success = stats.ackSuccess.getAndSet(0);
                    int fail = stats.ackFail.getAndSet(0);
                    groupTotalStats.addAll(req, pulled, success, fail);
                    totalStats.addAll(req, pulled, success, fail);
                    logPullStats(groupTopic, clientAddress, req, pulled, success, fail);
                } else if (TimeUtils.getElapseTime(stats.lastStatTimeStamp) > STATS_EXPIRED_TIME) {
                    groupPullStatsMap.remove(clientAddress);
                    LOGGER.warn("client stats expired. groupTopic={},client={}", groupTopic, clientAddress);
                }
            });
            if (!groupPullStatsMap.isEmpty()) {
                logPullStats(groupTopic, "ALL",
                        groupTotalStats.requestCnt.get(),
                        groupTotalStats.pulledMsg.get(),
                        groupTotalStats.ackSuccess.get(),
                        groupTotalStats.ackFail.get());
            }
        });
        logPullStats("ALL|ALL", "ALL", totalStats.requestCnt.get(), totalStats.pulledMsg.get(),
                totalStats.ackSuccess.get(), totalStats.ackFail.get());
    }

    private void logPullStats(String groupTopic, String clientAddress, int req, int pulled, int success, int fail) {
        METRIC_LOGGER.info("[PULL_STATS] group|topic={},client={},stats.type=request,pullStats.value={}", groupTopic, clientAddress, req);
        METRIC_LOGGER.info("[PULL_STATS] group|topic={},client={},stats.type=pulled,pullStats.value={}", groupTopic, clientAddress, pulled);
        METRIC_LOGGER.info("[PULL_STATS] group|topic={},client={},stats.type=success,pullStats.value={}", groupTopic, clientAddress, success);
        METRIC_LOGGER.info("[PULL_STATS] group|topic={},client={},stats.type=fail,pullStats.value={}", groupTopic, clientAddress, fail);
        METRIC_LOGGER.info("[PULL_STATS] group|topic={},client={},stats.type=response,pullStats.value={}", groupTopic, clientAddress, success + fail);
    }

    public void setThriftContext(ThriftContext context) {
        thriftContext.set(context);
    }

    private String getClientAddress() {
        if (thriftContext.get() == null) {
            return "UNKNOWN";
        } else {
            return thriftContext.get().remoteAddress;
        }
    }

    private void stats(PullRequest request, PullResponse response) {
        if (response.getMessagesSize() != 0) {
            MetricUtils.incPullStatCount(request.getGroupId(), request.getTopic(),
                    response.getContext().getQid(), "pulled", response.getMessagesSize());
        }
        for (ConsumeResult r = request.getResult(); r != null; r = r.nextResult) {
            MetricUtils.incPullStatCount(request.getGroupId(), request.getTopic(),
                    r.getContext().getQid(), "success", r.getSuccessOffsetsSize());
            MetricUtils.incPullStatCount(request.getGroupId(), request.getTopic(),
                    r.getContext().getQid(), "fail", r.getFailOffsetsSize());
            MetricUtils.incPullStatCount(request.getGroupId(), request.getTopic(),
                    r.getContext().getQid(), "response",
                    (r.getSuccessOffsetsSize() + r.getFailOffsetsSize()));
        }

        PullStats clientPullStats = getPullStats(request.getGroupId(), request.getTopic());
        clientPullStats.stat(request, response);
    }

    private PullStats getPullStats(String group, String topic) {
        String id = group + "|" + (topic == null ? "ANY" : topic);
        Map<String, PullStats> groupPullStats = pullStats.computeIfAbsent(id, g -> new ConcurrentHashMap<>());
        String address = getClientAddress();

        return groupPullStats.computeIfAbsent(address, addr -> new PullStats());
    }

    private boolean doSubmit(ConsumeResult consumeResult) {
        LOGGER.debug("submit={},client={}", consumeResult, getClientAddress());
        for (ConsumeResult r = consumeResult; r != null; r = r.nextResult) {
            PullBuffer buffer = bufferMap.get(consumeResult.getContext().getGroupId());
            if (buffer == null) continue;
            buffer.processResult(r);
        }
        return true;
    }

    private void stats(ConsumeResult consumeResult) {
        PullStats clientPullStats = getPullStats(consumeResult.getContext().getGroupId(),
                consumeResult.getContext().getTopic());
        MetricUtils.incPullStatCount(consumeResult.getContext().getGroupId(),
                consumeResult.getContext().getTopic(), consumeResult.getContext().getQid(),
                "success", consumeResult.getSuccessOffsetsSize());
        MetricUtils.incPullStatCount(consumeResult.getContext().getGroupId(),
                consumeResult.getContext().getTopic(), consumeResult.getContext().getQid(),
                "fail", consumeResult.getFailOffsetsSize());
        MetricUtils.incPullStatCount(consumeResult.getContext().getGroupId(),
                consumeResult.getContext().getTopic(), consumeResult.getContext().getQid(),
                "response", (consumeResult.getSuccessOffsetsSize() + consumeResult.getFailOffsetsSize()));
        clientPullStats.stat(consumeResult);

    }

    private void doGetConsumeStats(String reqGroup, String reqTopic, AsyncMethodCallback resultHandler) {
        try {
            List<ConsumeStats> result = new ArrayList<>();
            GroupConfig groupConfig = ConfigUtils.findGroupByName(reqGroup, ConfigManager.getInstance());
            if (groupConfig == null) {
                resultHandler.onComplete(result);
                return;
            }

            List<UpstreamTopic> topics;
            //未指定查询topic,则默认查询所有topic
            if (StringUtils.isEmpty(reqTopic)) {
                topics = groupConfig.getTopics();
            } else {
                topics = new ArrayList<>(1);
                for(UpstreamTopic topic : groupConfig.getTopics()) {
                    if(topic.getTopic().equals(reqTopic)) {
                        topics.add(topic);
                    }
                }
                //未找到指定topic
                if(topics.size() == 0) {
                    resultHandler.onComplete(result);
                    return;
                }
            }

            Map<String/*cluster*/, List<String>> needQueryKafkaTopics = new HashMap<>();
            Map<String/*cluster*/, List<String>> needQueryRmqTopics = new HashMap<>();
            for (UpstreamTopic topic : topics) {
                String brokerCluster = topic.getBrokerCluster();
                CProxyConfig cProxyConfig = ConfigManager.getInstance().getCurCproxyConfig();
                if (ConfigUtils.isKafkaMQCluster(cProxyConfig, brokerCluster)) {
                    needQueryKafkaTopics.computeIfAbsent(brokerCluster, key -> new ArrayList<>()).add(topic.getTopic());
                }

                if (ConfigUtils.isRmqMQCluster(cProxyConfig, brokerCluster)) {
                    needQueryRmqTopics.computeIfAbsent(brokerCluster, key -> new ArrayList<>()).add(topic.getTopic());
                }
            }
            for (String cluster : needQueryKafkaTopics.keySet()) {
                result.addAll(CarreraOffsetManager.getInstance().getKafkaConsumeStats(cluster, groupConfig.getGroup(), needQueryKafkaTopics.get(cluster)));
            }
            for (String cluster : needQueryRmqTopics.keySet()) {
                result.addAll(CarreraOffsetManager.getInstance().getRmqConsumeStats(cluster, groupConfig.getGroup(), needQueryRmqTopics.get(cluster)));
            }

            LOGGER.debug("doGetConsumeStats result:{}", result);
            resultHandler.onComplete(result);
        } catch (Exception e) {
            resultHandler.onError(e);
            LOGGER.error("doGetConsumeStats error!, err.msg:{}", e.getMessage(), e);
        }
    }

    public void unRegister(ConsumerGroupConfig config) {
        PullBuffer buffer = bufferMap.get(config.getGroup());
        buffer.removeClusterConfig(config);
    }

    public PullBuffer register(ConsumerGroupConfig config) {
        PullBuffer buffer = bufferMap.computeIfAbsent(config.getGroup(), groupId -> {
            PullBuffer newBuffer = new PullBuffer(groupId, pullScheduler);
            pullScheduler.scheduleAtFixedRate(newBuffer::recoverTimeoutMessage, 2000, 100, TimeUnit.MILLISECONDS);
            pullScheduler.scheduleAtFixedRate(newBuffer::cleanWaitQueue, 2000, 5000, TimeUnit.MILLISECONDS);
            return newBuffer;
        });
        buffer.addClusterConfig(config);
        return buffer;
    }

    @Override
    public void pull(PullRequest request, AsyncMethodCallback resultHandler) {
        MetricUtils.incPullStatCount(request.getGroupId(), request.getTopic(), null, "request");

        String group = request.getGroupId();
        consumerManager.tryCreateConsumer(group);

        if (request.getResult() != null) {
            doSubmit(request.getResult());
        }

        Context context = new Context();
        context.setGroupId(request.getGroupId());
        context.setTopic(request.getTopic());

        PullBuffer buffer = bufferMap.get(request.getGroupId());

        if (buffer == null) {
            responsePull(request, resultHandler, context, Collections.emptyList());
            return;
        }

        List<Message> messages = buffer.pull(context, request.maxBatchSize);

        if (CollectionUtils.isEmpty(messages)) {
            int timeout = Math.max(request.getMaxLingerTime(), MIN_LINGER_TIME);

            DelayRequest delayRequest = new DelayRequest(request, context, resultHandler, thriftContext.get(), timeout);
            if (buffer.addDelayRequest(delayRequest)) {
                pullScheduler.schedule(delayRequest::timeout, timeout, TimeUnit.MILLISECONDS);
            } else {
                LOGGER.warn("add DelayRequest failed. context={}", context);
                responsePull(request, resultHandler, context, messages);
            }
        } else {
            responsePull(request, resultHandler, context, messages);
        }
    }

    public void responsePull(PullRequest request, AsyncMethodCallback resultHandler, Context context, List<Message> messages) {
        PullResponse response;

        if (CollectionUtils.isEmpty(messages)) {
            response = EMPTY_PULL_RESPONSE;
        } else {
            response = new PullResponse(context, messages);
        }

        stats(request, response);
        if (LOGGER.isDebugEnabled() && CollectionUtils.isNotEmpty(messages)) {
            LOGGER.debug("pull result:context={},clientAddress={},result={}, ", context, getClientAddress(),
                    messages.stream()
                            .map(msg -> String.format("[key=%s,offset=%d]", msg.getKey(), msg.getOffset()))
                            .collect(Collectors.toList())
            );
        }

        resultHandler.onComplete(response);
    }

    @Override
    public void submit(ConsumeResult result, AsyncMethodCallback resultHandler) {
        stats(result);
        resultHandler.onComplete(doSubmit(result));
    }

    @Override
    public void getConsumeStats(ConsumeStatsRequest request, AsyncMethodCallback resultHandler) {
        try {
            executor.execute(() -> doGetConsumeStats(request.topic, request.group, resultHandler));
            LOGGER.info("getConsumeStats group:{}, sdk version:{}", request.getGroup(), request.getVersion());
        } catch (RejectedExecutionException e) {
            resultHandler.onError(e);
            LOGGER.info("Error while submit doGetConsumeStats task to thread pool. err.msg:{}", e.getMessage(), e);
        }
    }

    @Override
    public void fetch(FetchRequest request, AsyncMethodCallback resultHandler) throws TException {
        LOGGER.trace("fetch:{}", request);
        MetricUtils.incPullStatCount(request.getGroupId(),
                null, null, "fetch");
        try {
            FetchChain fetchAction = new FetchChain(request, resultHandler);
            fetchAction.doNext();
            LOGGER.trace("[lowlevel] FetchChain start. consumer num:{}, group:{}.", fetchAction.getConsumerNum(), request.groupId);
        } catch (Exception e) {
            LOGGER.error("exception in fetch:" + request, e);
            resultHandler.onError(e);
        }
    }

    @Override
    public void ack(AckResult result, AsyncMethodCallback resultHandler) throws TException {
        LOGGER.info("ack:{}", result);
        MetricUtils.incPullStatCount(result.getGroupId(),
                null, null, "ack");
        if (MapUtils.isEmpty(result.getOffsets())) {
            resultHandler.onComplete(true);
            return;
        }
        try {
            AckChain ackChain = new AckChain(result, resultHandler);
            ackChain.doNext();
            LOGGER.trace("[lowlevel] AckChain start. consumer num:{}, group:{}", ackChain.getConsumerNum(), result.getGroupId());
        } catch (Exception e) {
            LOGGER.error("exception in ack:" + result, e);
            resultHandler.onError(e);
        }
    }

    private static class Singleton {
        private static ConsumerServiceImpl INSTANCE = new ConsumerServiceImpl();
    }

    public static ConsumerServiceImpl getInstance() {
        return Singleton.INSTANCE;
    }

}