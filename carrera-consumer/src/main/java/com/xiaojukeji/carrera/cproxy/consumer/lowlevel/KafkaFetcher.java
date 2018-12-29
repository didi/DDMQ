package com.xiaojukeji.carrera.cproxy.consumer.lowlevel;

import com.google.common.util.concurrent.RateLimiter;
import com.xiaojukeji.carrera.cproxy.consumer.CommonMessage;
import com.xiaojukeji.carrera.cproxy.consumer.ConsumeContext;
import com.xiaojukeji.carrera.cproxy.consumer.LowLevelKafkaConsumer;
import com.xiaojukeji.carrera.cproxy.consumer.ResultCallBack;
import com.xiaojukeji.carrera.cproxy.consumer.exception.CarreraClientException;
import com.xiaojukeji.carrera.cproxy.consumer.handler.AsyncMessageHandler;
import com.xiaojukeji.carrera.thrift.consumer.AckResult;
import com.xiaojukeji.carrera.thrift.consumer.FetchRequest;
import com.xiaojukeji.carrera.thrift.consumer.FetchResponse;
import com.xiaojukeji.carrera.thrift.consumer.Message;
import com.xiaojukeji.carrera.thrift.consumer.QidResponse;
import com.xiaojukeji.carrera.cproxy.config.ConsumerGroupConfig;
import com.xiaojukeji.carrera.cproxy.utils.LogUtils;
import com.xiaojukeji.carrera.cproxy.utils.MetricUtils;
import com.xiaojukeji.carrera.cproxy.utils.QidUtils;
import org.apache.commons.collections4.MapUtils;
import org.slf4j.Logger;

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import static org.slf4j.LoggerFactory.getLogger;


public class KafkaFetcher extends AbstractTpsLimitedFetcher implements Fetcher, AsyncMessageHandler {
    public static final Logger LOGGER = getLogger(KafkaFetcher.class);

    private final String cid;

    private LinkedBlockingQueue<LowLevelMessage> totalQueue = new LinkedBlockingQueue<>(1000);

    private ConcurrentHashMap<CarreraQueue, Long> pulledMessage = new ConcurrentHashMap<>();
    private volatile FetchResponse lastWheelResponse;
    private ReentrantLock lock = new ReentrantLock();

    protected ConsumerGroupConfig config;
    protected volatile boolean isShutdown = false;
    protected volatile LowLevelKafkaConsumer consumer;

    private ScheduledExecutorService scheduler;

    public KafkaFetcher(ConsumerGroupConfig config, ScheduledExecutorService scheduler, String consumerId, ConcurrentHashMap<String/*topic*/, RateLimiter> rateLimiterMap) {
        super(rateLimiterMap);
        this.config = config;
        this.scheduler = scheduler;
        this.cid = consumerId;
    }

    @Override
    public synchronized boolean start() {
        LogUtils.logMainInfo("KafkaFetcher.start, this={}", this);
        try {
            lock.lock();
            if (!isShutdown && consumer == null) {
                LowLevelKafkaConsumer temp = createConsumer(this.config);
                temp.startConsume();
                consumer = temp;
            }
            return true;
        } catch (CarreraClientException e) {
            LogUtils.MAIN_LOGGER.error("[Fetcher] start error,this=" + this, e);
            return false;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void logMetrics() {
        LogUtils.METRIC_LOGGER.info("[KAFKA_FETCHER] group={},cid={},totalQueue.size={}",
                config.getGroupBrokerCluster(), cid, totalQueue.size());
    }

    public LowLevelKafkaConsumer createConsumer(ConsumerGroupConfig config) {
        LowLevelKafkaConsumer consumer = new LowLevelKafkaConsumer(config, this);
        consumer.enableOffsetAutoCommit(scheduler);
        return consumer;
    }

    @Override
    public synchronized void shutdown() {
        LogUtils.logMainInfo("KafkaFetcher.shutdown, this={},isShutdown={}", this, isShutdown);
        if (isShutdown)
            return;
        try {
            lock.lock();
            isShutdown = true;
            if (consumer != null) {
                consumer.shutdown();
                consumer = null;
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    public FetchResponse trueFetch(FetchRequest request) {
        FetchResponse response = new FetchResponse();
        if (isShutdown || consumer == null) {
            return response.setResults(Collections.emptyList());
        }
        if (request.getFetchOffset() != null) {
            final int[] errCnt = {0};
            request.getFetchOffset().forEach((topic, qidOffsetMap) -> {
                if (MapUtils.getObject(config.getTopicMap(), topic) == null) {
                    return;
                }
                Set<String> currentQids = consumer.getCurrentTopicQids(topic);
                qidOffsetMap.forEach((qid, requestOffset) -> {
                    if (!currentQids.contains(qid)) {
                        return;
                    }
                    CarreraQueue cq = new CarreraQueue(qid, topic);
                    long offset = pulledMessage.computeIfAbsent(cq, key -> requestOffset);
                    if (requestOffset < offset) {
                        errCnt[0]++;
                        if (LOGGER.isDebugEnabled())
                            LOGGER.info("offset:{} request offset:{}", offset, requestOffset);
                    }
                });
            });
            if (errCnt[0] > 0) {
                LOGGER.warn("fetch offset < last pull offset.fetcher={},errCnt={},req={}", this, errCnt[0], request);
                // return last response
                return lastWheelResponse;
            }
        }
        try {
            HashMap<CarreraQueue, QidResponse> resultCache = new HashMap<>();
            LowLevelMessage lmsg = totalQueue.poll(request.getMaxLingerTime(), TimeUnit.MILLISECONDS);
            int sendCount = 0;
            while (lmsg != null) {
                CarreraQueue cq = new CarreraQueue(lmsg.getContext().getQid(), lmsg.getMessage().getTopic());
                long pulledOffset = pulledMessage.computeIfAbsent(cq, key -> -1L);
                if (lmsg.getContext().getOffset() > pulledOffset) {
                    Message msg = new Message(lmsg.getMessage().getKey(), ByteBuffer.wrap(lmsg.getMessage().getValue()),
                            "", lmsg.getContext().getOffset());
                    QidResponse qr = resultCache.computeIfAbsent(cq,
                            key -> new QidResponse(key.getTopic(), key.getQid(), null));
                    qr.addToMessages(msg);
                    qr.setNextRequestOffset(lmsg.getContext().getOffset() + 1);
                    pulledMessage.put(cq, lmsg.getContext().getOffset());

                    if (qr.getMessagesSize() > config.getTopicMap().get(lmsg.getMessage().getTopic()).getMaxPullBatchSize()) {
                        break;
                    }
                    if (++sendCount >= request.maxBatchSize) {
                        break;
                    }
                } else if (pulledOffset >= 0) {
                    LOGGER.warn("fetcher={},skip topic {} qid {} pulledOffset {} msgoffset {}", this,
                            cq.getTopic(), cq.getQid(), pulledOffset, lmsg.getContext().getOffset());
                }
                lmsg = totalQueue.poll();
            }
            resultCache.values().forEach(response::addToResults);
            lastWheelResponse = response;

            if (!response.isSetResults()) {
                response.setResults(Collections.emptyList());
            }
            return response;
        } catch (InterruptedException e) {
            LOGGER.error("fetch error,fetcher=" + this, e);
            return null;
        }
    }

    @Override
    public boolean ack(AckResult result) {
        if (result.getOffsetsSize() == 0) {
            return true;
        }
        result.getOffsets().forEach((topic, qidMap) -> {
            if (MapUtils.getObject(config.getTopicMap(), topic) == null) {
                LOGGER.warn("invalid topic({}) in {},result={}", topic, this, result);
                return;
            }
            Set<String> currentQids = consumer.getCurrentTopicQids(topic);
            qidMap.forEach((qid, offset) -> {
                if (!currentQids.contains(qid)) {
                    LOGGER.warn("invalid qid({}) in {},result={}", qid, this, result);
                    return;
                }

                MetricUtils.maxOffsetCount(result.getGroupId(), topic, qid, "ack", offset);
                consumer.setCommitOffset(topic, QidUtils.getKafkaQid(config.getBrokerCluster(), qid), offset);
                LOGGER.debug("commit offset groupId:{}, topic:{}, qid:{}, offset:{}, consumer:{}", result.getGroupId(), topic, qid, offset, this);
            });
        });
        return true;
    }

    @Override
    public void process(CommonMessage message, ConsumeContext context, ResultCallBack resultCallBack) throws InterruptedException {
        while (!isShutdown) {
            if (totalQueue.offer(new LowLevelMessage(message, context), 100, TimeUnit.MILLISECONDS)) {
                break;
            }
        }
    }

    @Override
    public String toString() {
        return "KafkaFetcher{" +
                "cid='" + cid + '\'' +
                "groupCluster='" + config.getGroupBrokerCluster() + '\'' +
                '}';
    }
}