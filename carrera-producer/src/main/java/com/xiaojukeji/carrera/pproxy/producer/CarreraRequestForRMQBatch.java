package com.xiaojukeji.carrera.pproxy.producer;

import com.xiaojukeji.carrera.thrift.Message;
import com.xiaojukeji.carrera.pproxy.constants.Constant;
import com.xiaojukeji.carrera.pproxy.utils.MetricUtils;
import com.xiaojukeji.carrera.pproxy.utils.MsgCheckUtils;
import com.xiaojukeji.carrera.pproxy.utils.StatsUtils;
import com.xiaojukeji.carrera.pproxy.utils.TimeUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.impl.producer.TopicPublishInfo;
import org.apache.rocketmq.common.MixAll;
import org.apache.rocketmq.common.message.MessageBatch;
import org.apache.rocketmq.common.message.MessageQueue;
import org.apache.thrift.async.AsyncMethodCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;


public class CarreraRequestForRMQBatch extends CarreraRequest {
    private static final Logger LOGGER = LoggerFactory.getLogger(CarreraRequestForRMQBatch.class);
    private List<Message> messageList;

    private Map<String, List<CarreraMessage>> requestMap = new ConcurrentHashMap<>();
    private volatile String curProcBroker;
    private volatile MessageQueue curMsgQueue;
    private volatile boolean isClassified = false;

    public CarreraRequestForRMQBatch(ProducerPool producerPool, List<Message> messageList, AsyncMethodCallback resultHandler) {
        super(producerPool, messageList.get(0), Constant.DEFAULT_MQ_SEND_TIMEOUT_MS, resultHandler);
        this.messageList = messageList;
    }

    @Override
    public void process() {
        if (!isClassified) {
            if (classifyMessage()) {
                super.process();
            }
        } else {
            super.process();
        }
    }

    private boolean classifyMessage() {
        Map<String, MessageQueue> defaultMsgQueue = new HashMap<>();

        for (Message message : messageList) {
            if (brokerCluster == null) {
                onFinish(ProxySendResult.FAIL_TOPIC_NOT_ALLOWED);
                return false;
            }

            CarreraMessage carreraMessage = new CarreraMessage(message);
            if (message.getPartitionId() < -1 && defaultMsgQueue.containsKey(message.getTopic())) {
                carreraMessage.setMessageQueue(defaultMsgQueue.get(message.getTopic()));
            } else {
                RmqClusterProducer producer = (RmqClusterProducer) producerPool.getProducer(brokerCluster);
                if (producer == null) {
                    onFinish(ProxySendResult.FAIL_NO_PRODUCER_FOR_CLUSTER);
                    return false;
                }

                TopicPublishInfo topicInfo = RmqSender.tryToFindTopicPublishInfo(producer.pickRocketMQProducer(), message.topic);
                if (topicInfo == null || !topicInfo.ok()) {
                    LOGGER.error("get topic info failed, topic={}", message.topic);
                    onFinish(ProxySendResult.FAIL_UNKNOWN);
                    return false;
                }

                if (CollectionUtils.isEmpty(topicInfo.getMessageQueueList())) {
                    LOGGER.error("message queue list is null or empty");
                    onFinish(ProxySendResult.FAIL_UNKNOWN);
                    return false;
                }

                MessageQueue mq = RmqSender.select(carreraMessage, topicInfo);
                carreraMessage.setMessageQueue(mq);
                if (message.getPartitionId() < -1 && !defaultMsgQueue.containsKey(message.getTopic())) {
                    defaultMsgQueue.put(message.getTopic(), mq);
                }
            }

            requestMap.computeIfAbsent(carreraMessage.getMessageQueue().getBrokerName(), _name -> new ArrayList<>()).add(carreraMessage);
        }

        LOGGER.info("topic:{}, firs msg key:{}, message count:{}, will send to broker:{}", getTopic(), getKey(), messageList.size(), requestMap.keySet());
        isClassified = true;
        return true;
    }

    @Override
    public MessageQueue getMessageQueue() {
        return curMsgQueue;
    }

    @Override
    public org.apache.rocketmq.common.message.Message toRmqMessage() {
        if (requestMap.isEmpty()) {
            throw new RuntimeException("no message left to send");
        }
        curProcBroker = requestMap.keySet().toArray()[0].toString();
        List<CarreraMessage> msgList = requestMap.get(curProcBroker);
        if (CollectionUtils.isEmpty(msgList)) {
            LOGGER.error("get message list failed, cur broker=" + curProcBroker);
            throw new RuntimeException("get message list failed, cur broker=" + curProcBroker);
        }
        LOGGER.debug("get batch data, broker={}, message count={}", curProcBroker, msgList.size());
        curMsgQueue = msgList.get(0).getMessageQueue();

        ByteBuffer buffer = BatchMQProducer.getEncodeBuffer();
        Map<String, List<CarreraMessage>> topicRequestMap = msgList.stream().collect(Collectors.groupingBy(CarreraMessage::getTopic));
        List<String> topics = new ArrayList<>();
        topicRequestMap.forEach((topic, msgs) -> {
            int topicIdx = topics.size();
            topics.add(topic);
            msgs.forEach(msg -> {
                assert msg.getMessageQueue() != null;
                if (!BatchMQProducer.encode(buffer, msg, topicIdx)) {
                    LOGGER.error("encode error. cur request={},batch={}", msg, msgList);
                    throw new RuntimeException("encode error");
                }
            });

        });

        MessageBatch messageBatch = new MessageBatch();
        buffer.flip();
        byte[] body = new byte[buffer.remaining()];
        System.arraycopy(buffer.array(), 0, body, 0, body.length);
        messageBatch.setBody(body);
        messageBatch.setMultiTopic(true);
        messageBatch.setTopic(String.join(MixAll.BATCH_TOPIC_SPLITTER, topics));
        messageBatch.setWaitStoreMsgOK(true);
        return messageBatch;
    }

    @Override
    public synchronized void onFinish(ProxySendResult result) {
        if (finished) {
            LOGGER.warn("send batch duplicate onFinish! result={}, request={}, time:{}ms",
                    result, this, TimeUtils.getElapseTime(startTime));
            return;
        }

        LOGGER.debug("broker={}, send completely, result={}", curProcBroker, result);
        procSingleBrokerResult(curProcBroker, result);
        if (!ProxySendResult.OK.equals(result)) {
            procResult(result);
            LOGGER.warn("send batch failed, send result={}", result);
            return;
        }

        //send next broker
        requestMap.remove(curProcBroker);
        if (!requestMap.isEmpty()) {
            super.process();
            return;
        }

        procResult(result);
    }

    private void procSingleBrokerResult(String brokerName, ProxySendResult result) {
        if (StringUtils.isEmpty(brokerName)) {
            LOGGER.warn("broker name is empty");
            return;
        }
        List<CarreraMessage> messages = requestMap.get(brokerName);
        if (messages == null) {
            LOGGER.warn("message of broker={} is empty", brokerName);
            return;
        }

        for (CarreraMessage message : messages) {
            LOGGER.info("send batch Result:{}, broker={}, first msg key:{}, message:{},timeout:{},time:{}ms, limiterRetry:{}",
                    result, brokerName, getKey(), message.toShortString(), timeout, TimeUtils.getElapseTime(startTime), limiterFailureRetryCount);
        }

        limiterFailureRetryCount = 0;

        MetricUtils.incBatchSendCounter(getTopic(), result.toString());
    }

    private void procResult(ProxySendResult result) {
        if (resultHandler != null) {
            resultHandler.onComplete(result.getResult());
        }

        if (timeoutHandle != null) {
            timeoutHandle.cancel();
        }

        for (Message msg : messageList) {
            MetricUtils.incQPSCounter(msg.getTopic(), result.toString());
            if (getRetries() == 0) {//do not collect messages retried
                long micros = TimeUtils.getElapseMicros(startTime);
                MetricUtils.putSendLatency(msg.getTopic(), micros);
                StatsUtils.sendSync.add(micros);
            }
        }

        LOGGER.info("send batch completely, topic:{}, result:{}, firs msg key:{}, message count:{}", getTopic(), result, getKey(), messageList.size());
        finished = true;
    }


    public boolean checkValid() {
        if (!producerPool.getConfigManager().getProxyConfig().getCarreraConfiguration().isUseRocketmq()
                || !producerPool.getConfigManager().getProxyConfig().getCarreraConfiguration().isUseAutoBatch())
            return false;
        if (CollectionUtils.isEmpty(messageList))
            return false;

        boolean ret = true;
        int totalSize = 0;
        Set<String> topics = new HashSet<>();
        String clusterSend = producerPool.getConfigManager().getTopicConfigManager().getDefaultRmqBrokerCluster(messageList.get(0));
        if (StringUtils.isEmpty(clusterSend)) {
            return false;
        }
        for (Message message : messageList) {
            topics.add(message.getTopic());

            if (!checkValidExceptBody(message)) {
                ret = false;
            }

            String msgClusterSend = producerPool.getConfigManager().getTopicConfigManager().getDefaultRmqBrokerCluster(message);
            if (StringUtils.isEmpty(msgClusterSend) || !clusterSend.equals(msgClusterSend)) {
                LOGGER.warn("message not to the same cluster, topic={}", message.getTopic());
                ret = false;
            }

            //校验body
            reformatMessage(message);
            if (message.body == null || message.body.remaining() == 0) {
                LOGGER.error("body is empty or length is 0 for batch send, topic={}, key={}", message.getTopic(), message.getKey());
                ret = false;
            }
            totalSize += message.body.remaining();
            if (totalSize > producerPool.getConfigManager().getMaxMessageSize(message.getTopic())) {
                LOGGER.error("body of all message is too long for batch send, topic={}", message.getTopic());
                ret = false;
            }

            String conflictProperty = MsgCheckUtils.checkProperties(message.getProperties());
            if (conflictProperty != null) {
                LOGGER.error("illegal properties(Conflicts with reserved attribute keywords):topic={}, {}", message.getTopic(), conflictProperty);
                ret = false;
            }
        }

        brokerCluster = clusterSend;

        for (String topic : topics) {
            MetricUtils.incRequestCounter(topic, MetricUtils.REQUEST_BATCHSYNC);
        }

        return ret;
    }

    @Override
    public String toString() {
        return "RMQBatch-" + super.toString();
    }

}