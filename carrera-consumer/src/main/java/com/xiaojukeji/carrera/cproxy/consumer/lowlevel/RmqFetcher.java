package com.xiaojukeji.carrera.cproxy.consumer.lowlevel;

import com.google.common.util.concurrent.RateLimiter;
import com.xiaojukeji.carrera.config.v4.cproxy.RocketmqConfiguration;
import com.xiaojukeji.carrera.thrift.consumer.AckResult;
import com.xiaojukeji.carrera.thrift.consumer.FetchRequest;
import com.xiaojukeji.carrera.thrift.consumer.FetchResponse;
import com.xiaojukeji.carrera.thrift.consumer.Message;
import com.xiaojukeji.carrera.thrift.consumer.QidResponse;
import com.xiaojukeji.carrera.thrift.consumer.consumerProxyConstants;
import com.xiaojukeji.carrera.cproxy.config.ConsumerGroupConfig;
import com.xiaojukeji.carrera.cproxy.utils.CommonUtils;
import com.xiaojukeji.carrera.cproxy.utils.LogUtils;
import com.xiaojukeji.carrera.cproxy.utils.MetricUtils;
import com.xiaojukeji.carrera.cproxy.utils.QidUtils;
import com.xiaojukeji.carrera.cproxy.utils.StringUtils;
import com.xiaojukeji.carrera.cproxy.utils.TimeUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.rocketmq.client.consumer.DefaultMQPullConsumer;
import org.apache.rocketmq.client.consumer.MessageQueueListener;
import org.apache.rocketmq.client.consumer.PullResult;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.message.MessageConst;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.common.message.MessageQueue;
import org.apache.rocketmq.remoting.exception.RemotingException;
import org.apache.rocketmq.remoting.exception.RemotingTimeoutException;
import org.slf4j.Logger;

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static org.slf4j.LoggerFactory.getLogger;


public class RmqFetcher extends AbstractTpsLimitedFetcher implements MessageQueueListener {
    public static final Logger LOGGER = getLogger(RmqFetcher.class);

    private volatile DefaultMQPullConsumer consumer;

    private final String cid;
    private volatile Map<String/*Topic*/, Map<String/*Qid*/, MessageQueue>> qidToMqMap = new ConcurrentHashMap<>();

    private final ConsumerGroupConfig config;

    public RmqFetcher(ConsumerGroupConfig config, String consumerId, ConcurrentHashMap<String/*topic*/, RateLimiter> rateLimiterMap) {
        super(rateLimiterMap);
        this.config = config;
        cid = consumerId;
    }

    @Override
    public FetchResponse trueFetch(FetchRequest request) {
        FetchResponse response = new FetchResponse();
        if (consumer == null) {
            return response.setResults(Collections.emptyList());
        }
        // 1. parallelize all fetchMessage Call
        // 2. async caching the messages.
        qidToMqMap.forEach((topic, qmMap) -> {
            if (MapUtils.getObject(config.getTopicMap(), topic) == null) {
                return;
            }
            Map<String, Long> offsetMap = MapUtils.getObject(request.getFetchOffset(), topic);
            int maxBatchSize = Math.max(1, Math.min(request.getMaxBatchSize(),
                    config.getTopicMap().get(topic).getMaxPullBatchSize()));
            qmMap.forEach((qid, mq) -> {
                long offset;
                if (offsetMap != null && offsetMap.containsKey(qid)) {
                    offset = offsetMap.get(qid);
                } else {
                    offset = getOffsetInStore(mq);
                }
                QidResponse qidResponse = fetchMessage(qid, mq, offset, request, maxBatchSize);
                if (qidResponse.getMessagesSize() == 0) {
                    qidResponse.setMessages(Collections.emptyList());
                }
                if (!qidResponse.isSetNextRequestOffset()) {
                    qidResponse.setNextRequestOffset(offset);
                }
                response.addToResults(qidResponse);
            });
        });
        if (!response.isSetResults()) {
            response.setResults(Collections.emptyList());
        }
        return response;
    }

    private QidResponse fetchMessage(String qid, MessageQueue mq, long offset, FetchRequest request, int maxBatchSize) {
        QidResponse r = new QidResponse(mq.getTopic(), qid, null);
        PullResult pr;
        try {
            pr = consumer.pull(mq, "*", offset, maxBatchSize, request.getMaxLingerTime());
        } catch (RemotingTimeoutException e) {
            LOGGER.error("consumer.pull timeout. fetcher={},exception={}", this, e.getMessage());
            return r;
        } catch (MQClientException | RemotingException | MQBrokerException | InterruptedException e) {
            LOGGER.error("consumer.pull failed. fetcher=" + this, e);
            return r;
        }

        switch (pr.getPullStatus()) {
            case FOUND:
                LOGGER.debug("consumer.pull result:{}", pr);
                boolean isPressureTrafficEnable = isPressureTrafficEnable(mq.getTopic());
                for (MessageExt messageExt : pr.getMsgFoundList()) {
                    if (!isPressureTrafficEnable && isPressureTrafficMessage(messageExt)) {
                        LOGGER.debug("message ignore, stress test enable, message:{}", messageExt.getKeys());
                        continue;
                    }
                    Message msg = new Message(messageExt.getKeys(),
                            ByteBuffer.wrap(messageExt.getBody()),
                            messageExt.getTags(),
                            messageExt.getQueueOffset());
                    msg.setProperties(CommonUtils.mapRemoveKeys(messageExt.getProperties(), MessageConst.STRING_HASH_SET));
                    r.addToMessages(msg);
                }

                //压测时，一次获取消息为空，记录此次做大的offset，防止一直拉取不到消息，重启时大量重复
                if (!isPressureTrafficEnable && CollectionUtils.isEmpty(r.messages) && pr.getNextBeginOffset() > offset) {
                    try {
                        consumer.updateConsumeOffset(mq, pr.getNextBeginOffset());
                        MetricUtils.maxOffsetCount(request.getGroupId(), mq.getTopic(), qid, "ack", offset);
                        LOGGER.debug("update offset mq={},offset={}", mq, offset);
                    } catch (Exception e) {
                        LOGGER.error("consumer.updateConsumeOffset failed", e);
                    }
                }

            case NO_NEW_MSG:
            case NO_MATCHED_MSG:
            case OFFSET_ILLEGAL:
                r.setNextRequestOffset(pr.getNextBeginOffset());
        }

        return r;
    }

    private boolean isPressureTrafficEnable(String topic) {
        if (!config.getTopicMap().containsKey(topic)) {
            LOGGER.warn("topic:{} not in config");
            return false;
        }
        return config.getTopicMap().get(topic).isPressureTraffic();
    }

    private boolean isPressureTrafficMessage(MessageExt msg) {
        if (msg.getProperties() != null && msg.getProperties().containsKey(consumerProxyConstants.PRESSURE_TRAFFIC_KEY)) {
            return Boolean.valueOf(msg.getProperties().get(consumerProxyConstants.PRESSURE_TRAFFIC_KEY));
        }

        return false;
    }

    @Override
    public void messageQueueChanged(String topic, Set<MessageQueue> mqAll, Set<MessageQueue> mqDivided) {
        LOGGER.debug("RmqFetcher.messageQueueChanged,group={},cid={},topic={},newMQ={}",
                config.getGroup(), cid, topic, mqDivided);
        Map<String, MessageQueue> qidToMq = new HashMap<>();
        for (MessageQueue mq : mqDivided) {
            String qid = QidUtils.rmqMakeQid(config.getBrokerCluster(),
                    mq.getBrokerName(), mq.getQueueId());
            qidToMq.put(qid, mq);
        }
        qidToMqMap.put(topic, qidToMq);
    }

    public boolean ack(AckResult result) {
        Map<String, Map<String, Long>> topicOffsetMap = result.getOffsets();
        if (topicOffsetMap == null) {
            return true;
        }
        if (consumer == null) {
            return false;
        }
        topicOffsetMap.forEach((topic, offsetMap) -> {
            if (MapUtils.getObject(config.getTopicMap(), topic) == null) {
                return;
            }
            Map<String, MessageQueue> qmMap = qidToMqMap.get(topic);
            if (qmMap == null) {
                LOGGER.warn("invalid topic({}) in {},result={}", topic, this, result);
                return;
            }
            offsetMap.forEach((qid, offset) -> {
                MessageQueue mq = qmMap.get(qid);
                if (mq == null) {
                    LOGGER.warn("invalid qid({}) in {},result={}", qid, this, result);
                    return;
                }
                try {
                    consumer.updateConsumeOffset(mq, offset);
                    MetricUtils.maxOffsetCount(result.getGroupId(), topic, qid, "ack", offset);
                    LOGGER.debug("update offset mq={},offset={}, groupId={}, consumer={}", mq, offset, result.getGroupId(), this);
                } catch (Exception e) {
                    LOGGER.error("consumer.updateConsumeOffset failed", e);
                }
            });
        });
        long startPersist = TimeUtils.getCurTime();
        //TODO consumer内部有定时线程提交offset，如果耗时过大，可以去掉
        consumer.getDefaultMQPullConsumerImpl().persistConsumerOffset();
        LOGGER.info("persist offset cost:{}, fetcher={}", TimeUtils.getElapseTime(startPersist), this);
        return true;
    }

    @Override
    public void logMetrics() {

    }

    @Override
    public String toString() {
        return "RmqFetcher{" +
                "cid='" + cid + '\'' +
                ", groupCluster=" + config.getGroupBrokerCluster() +
                '}';
    }

    public synchronized void shutdown() {
        if (consumer != null) {
            consumer.shutdown();
            consumer = null;
        }
        LogUtils.logMainInfo("RmqFetcher.shutdown,this={}", this);
    }

    long getOffsetInStore(MessageQueue mq) {
        try {
            long offset = consumer.fetchConsumeOffset(mq, true);
            if (offset < 0) {//first consume, use latest offset.
                offset = consumer.maxOffset(mq);
                LOGGER.debug("use max offset,mq={},offset={}", mq, offset);
            } else {
                LOGGER.debug("get offset from store,mq={},offset={}", mq, offset);
            }
            return offset;
        } catch (MQClientException e) {
            LOGGER.info("get offset failed!");
        }
        return 0;
    }

    @Override
    public synchronized boolean start() {
        DefaultMQPullConsumer consumer = new DefaultMQPullConsumer(config.getGroup());
        RocketmqConfiguration rmqConfig = config.getcProxyConfig().getRocketmqConfigs().get(config.getBrokerCluster());
        String nameServers = StringUtils.join(rmqConfig.getNamesrvAddrs().iterator(), ";");
        consumer.setNamesrvAddr(nameServers);
        //必须这样设置。一个instance只能有一个group
        consumer.setInstanceName(cid + System.currentTimeMillis());
        //fix 2018.6.14 lowlevel实例过多，netty线程未共享
        consumer.setClientCallbackExecutorThreads(1);
        consumer.setMessageQueueListener(this);
        consumer.setPersistConsumerOffsetInterval(rmqConfig.getPersistConsumerOffsetInterval());
        consumer.setPollNameServerInterval(rmqConfig.getPollNameServerInterval());
        consumer.setRegisterTopics(new HashSet<>(config.getTopicNames()));
        try {
            consumer.start();
        } catch (MQClientException e) {
            LogUtils.MAIN_LOGGER.error(String.format("start RmqFetcher failed. group@Cluster=%s,consumerId=%s",
                    config.getGroupBrokerCluster(), cid), e);
            consumer.shutdown();
            return false;
        }
        this.consumer = consumer;
        LogUtils.logMainInfo("RmqFetcher.start, this={}", this);
        return true;
    }
}