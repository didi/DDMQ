package com.xiaojukeji.carrera.pproxy.producer;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.xiaojukeji.carrera.config.v4.pproxy.BatchMQProducerConfiguration;
import com.xiaojukeji.carrera.pproxy.constants.Constant;
import com.xiaojukeji.carrera.pproxy.utils.LogUtils;
import com.xiaojukeji.carrera.pproxy.utils.TimeUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.impl.producer.TopicPublishInfo;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.MessageQueueSelector;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.MixAll;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageBatch;
import org.apache.rocketmq.common.message.MessageDecoder;
import org.apache.rocketmq.common.message.MessageQueue;
import org.apache.rocketmq.common.stats.TimeStats;
import org.apache.rocketmq.remoting.exception.RemotingException;
import org.slf4j.Logger;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import static com.xiaojukeji.carrera.pproxy.producer.ProxySendResult.FAIL_SERVER_SHUTDOWN;
import static org.slf4j.LoggerFactory.getLogger;


class BatchMQProducer {

    private static final Logger LOGGER = getLogger(BatchMQProducer.class);

    private static final Logger METRIC_LOGGER = LogUtils.getMetricLogger();

    private static final int BUFFER_SIZE = 3 * 1024 * 1024; // 3MB

    private static final ThreadLocal<ByteBuffer> BATCH_BODY_BUFFER = ThreadLocal.withInitial(() -> ByteBuffer.allocate(BUFFER_SIZE));

    private final Map<String, Deque<CarreraRequest>> requestQMap = new ConcurrentHashMap<>();

    private final BatchMQProducerConfiguration config;

    // the following properties are used for runtime stats.
    private final AtomicLong emptyQueueCnt = new AtomicLong();
    private final AtomicLong batchSendCnt = new AtomicLong();
    private final AtomicLong nonBatchSendCnt = new AtomicLong();
    private final AtomicLong totalMsgCnt = new AtomicLong();
    private final TimeStats.RolloverTimeStats statsBatchSendTime = new TimeStats.RolloverTimeStats(100000);
    private final TimeStats.RolloverTimeStats statsBatchEncodeTime = new TimeStats.RolloverTimeStats(100000);
    private ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(new ThreadFactoryBuilder().setNameFormat("BatchMQProducer-MetricReporter-%d").setDaemon(true).build());

    private volatile boolean running = false;
    private final ScheduledExecutorService workers;

    private RmqClusterProducer clusterProducer;

    public BatchMQProducer(RmqClusterProducer clusterProducer, BatchMQProducerConfiguration config) {
        this.clusterProducer = clusterProducer;
        this.config = config;
        workers = Executors.newScheduledThreadPool(config.getEncodeWorkerThreads(),
            new ThreadFactoryBuilder().setNameFormat("BatchMQProducer-Worker-%d").build());
    }

    public void start() {
        LOGGER.info("starting BatchMQProducer, this={}", this);
        if (running) {
            LOGGER.warn("BatchMQProducer already started!, this={}", this);
            return;
        }

        running = true;
        scheduler.scheduleAtFixedRate(this::reportMetric, 1, 1, TimeUnit.SECONDS);
    }

    private void reportMetric() {
        long curEmptyQueueCnt = emptyQueueCnt.getAndSet(0);
        long curTotalMsgCnt = totalMsgCnt.getAndSet(0);
        long curBatchSendCnt = batchSendCnt.getAndSet(0);
        long curNonBatchSendCnt = nonBatchSendCnt.getAndSet(0);
        int totalQueueSize = this.getQueueSize();
        String info = String.format("[BatchMQProducer]QSize:%8d,emptyTry:%8d,msgSend:%8d,nonBatchSend:%8d,batchSend:%8d,avgBatch:%8.2f",
            totalQueueSize, curEmptyQueueCnt,
            curTotalMsgCnt, curNonBatchSendCnt, curBatchSendCnt,
            1.0 * (curTotalMsgCnt - curNonBatchSendCnt) / curBatchSendCnt);
        METRIC_LOGGER.info(info);
        METRIC_LOGGER.info("[BatchMQProducer].statsBatchSendTime:{}", statsBatchSendTime.reportAndReset(1e-3));
        METRIC_LOGGER.info("[BatchMQProducer].statsBatchEncodeTime:{}", statsBatchEncodeTime.reportAndReset(1e-3));
    }

    private int getQueueSize() {
        return requestQMap.values().stream().mapToInt(Queue::size).sum();
    }

    public void shutdown() {
        if (!running) {
            LOGGER.warn("BatchMQProducer is already shutdown.");
            return;
        }
        running = false;
        scheduler.shutdown();
        workers.shutdown();
        requestQMap.values().forEach(q -> {
            CarreraRequest r;
            while ((r = q.poll()) != null) {
                r.onFinish(FAIL_SERVER_SHUTDOWN);
            }
        });
    }

    private void sendSingleMessage(CarreraRequest request)
        throws RemotingException, MQClientException, InterruptedException, MQBrokerException {
        nonBatchSendCnt.incrementAndGet();
        long startSend = TimeUtils.getCurTime();
        Message message = request.toRmqMessage();
        if (request.getMessageQueue() != null) {
            RmqSender.send(clusterProducer.pickRocketMQProducer(), message, request.getMessageQueue(), request, Constant.DEFAULT_MQ_SEND_TIMEOUT_MS);
        } else {
            RmqSender.send(clusterProducer.pickRocketMQProducer(), request, request, Constant.DEFAULT_MQ_SEND_TIMEOUT_MS);
        }
        LOGGER.debug("sendSingleMessage:r={},cost={}", request, TimeUtils.getElapseTime(startSend));
    }

    private void sendBatchMessage(List<CarreraRequest> batchRequest)
        throws MQClientException, RemotingException, InterruptedException, MQBrokerException {
        assert batchRequest.size() > 0;

        batchSendCnt.incrementAndGet();
        long startEncode = TimeUtils.getCurTime();
        Message msg = buildBatchRmqMessage(batchRequest);
        statsBatchEncodeTime.add(TimeUtils.getElapseMicros(startEncode));
        long startSend = TimeUtils.getCurTime();
        MessageQueue mq = batchRequest.get(0).getMessageQueue();
        RmqSender.send(clusterProducer.pickRocketMQProducer(), msg, mq, new SendCallback() {
            private void logResult(Throwable t, SendResult sendResult) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("sendBatchMessage:result={},topicKeys:{},batchMsgs={},batchSize={},encode.time={}ms,send.time:{}",
                        t == null ? sendResult : t,
                        batchRequest.stream().map(CarreraRequest::getKey).collect(Collectors.toList()),
                        batchRequest.size(), CollectionUtils.size(msg.getBody()),
                        TimeUtils.getElapseTime(startEncode), TimeUtils.getElapseTime(startSend));
                }
                statsBatchSendTime.add(TimeUtils.getElapseMicros(startSend));
            }

            @Override
            public void onSuccess(SendResult sendResult) {
                logResult(null, sendResult);
                batchRequest.forEach(r -> r.onSuccess(sendResult));
            }

            @Override
            public void onException(Throwable e) {
                logResult(e, null);
                batchRequest.forEach(r -> r.onException(e));
            }
        }, Constant.DEFAULT_MQ_SEND_TIMEOUT_MS);
    }

    public static ByteBuffer getEncodeBuffer() {
        ByteBuffer buffer = BATCH_BODY_BUFFER.get();
        buffer.clear();
        return buffer;
    }

    /**
     * refer: {@link DefaultMQProducer#batch(java.util.Collection)}
     */
    private MessageBatch buildBatchRmqMessage(List<CarreraRequest> batchRequest) {
        Map<String, List<CarreraRequest>> topicRequestMap = batchRequest.stream().collect(Collectors.groupingBy(CarreraRequest::getTopic));
        List<String> topics = new ArrayList<>();
        ByteBuffer buffer = getEncodeBuffer();
        topicRequestMap.forEach((topic, requestList) -> {
            int topicIdx = topics.size();
            topics.add(topic);
            requestList.forEach(request -> {
                assert request.getMessageQueue() != null;
                if (!encode(buffer, request, topicIdx)) {
                    LOGGER.debug("encode error. cur request={},batch={}", request, batchRequest);
                    request.onException(new MQClientException(-1, "encode batch message error."));
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
    public String toString() {
        return "BatchMQProducer{" +
            "config=" + config +
            '}';
    }

    private void addRequestQueue(Deque<CarreraRequest> requestQ, String brokerName, int taskNum) {
        long avgWait = TimeUnit.NANOSECONDS.toMicros(config.getBatchWaitMills()) / taskNum;
        for (int i = 1; i <= taskNum; i++) {
            workers.schedule(new BatchWorker(requestQ, brokerName + "-" + i), i * avgWait, TimeUnit.NANOSECONDS);
        }
    }

    private class BatchWorker implements Runnable {
        private final Deque<CarreraRequest> requestQ;
        private final String brokerName;

        BatchWorker(Deque<CarreraRequest> requestQ, String brokerName) {
            this.requestQ = requestQ;
            this.brokerName = brokerName;
        }

        @Override
        public void run() {
            boolean noDelay = false;
            try {
                noDelay = doSend();
            } catch (Throwable t) {
                LogUtils.logError("addRequestQueue.doSend", "Unknown Throwable in " + this, t);
            } finally {
                if (!running) {
                    LOGGER.info("BatchWorker finished. this=" + this);
                } else {
                    long delay = noDelay ? 0 : config.getBatchWaitMills();
                    workers.schedule(this, delay, TimeUnit.MILLISECONDS);
                }
            }
        }

        private boolean doSend() {
            long startTime = TimeUtils.getCurTime();
            while (running) {
                try {
                    if (!trySend(requestQ)) {
                        emptyQueueCnt.incrementAndGet();
                        return false;
                    }
                } catch (InterruptedException e) {
                    LogUtils.logWarn("BatchWorker.doSend", this + " is interrupted. " + e.getMessage());
                    Thread.interrupted();
                    return true;
                }
                if (TimeUtils.getElapseTime(startTime) > config.getMaxContinuouslyRunningMills()) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public String toString() {
            return "BatchWorker{" +
                "requestQ.size=" + CollectionUtils.size(requestQ) +
                ", brokerName='" + brokerName + '\'' +
                '}';
        }
    }

    private boolean trySend(Deque<CarreraRequest> requestQ) throws InterruptedException {
        CarreraRequest carreraRequest = requestQ.poll();
        if (carreraRequest == null) {
            return false;
        }
        if (carreraRequest.isFinished()) {
            return true;
        }

        List<CarreraRequest> batchRequests = new ArrayList<>();
        batchRequests.add(carreraRequest);
        int totalSize = carreraRequest.binarySize();

        while (batchRequests.size() < config.getMaxBatchMessagesNumber() && (carreraRequest = requestQ.poll()) != null && running) {
            if (carreraRequest.isFinished()) {
                continue;
            }

            int len = carreraRequest.binarySize();
            if (totalSize + len < config.getMaxBathBytes()) {
                totalSize += len;
                batchRequests.add(carreraRequest);
            } else {
                requestQ.addFirst(carreraRequest);
                break;
            }
        }

        if (!running) {
            batchRequests.forEach(_r -> _r.onFinish(FAIL_SERVER_SHUTDOWN));
            return true;
        }

        try {
            if (batchRequests.size() == 1) {
                sendSingleMessage(batchRequests.get(0));
            } else {
                sendBatchMessage(batchRequests);
            }
        } catch (RemotingException | MQClientException | MQBrokerException e) {
            //retry in non-batch mode.
            for (CarreraRequest request : batchRequests) {
                request.onException(e);
            }
        }

        totalMsgCnt.addAndGet(batchRequests.size());
        return true;
    }

    public void send(CarreraRequest request, MessageQueueSelector messageQueueSelector)
        throws RemotingException, MQClientException, InterruptedException, MQBrokerException {
        TopicPublishInfo topicInfo = clusterProducer.getRocketMQProducerByIndex(0).getDefaultMQProducerImpl().getTopicPublishInfoTable().get(request.getTopic());
        if (topicInfo == null || !topicInfo.ok()) { //new topic
            sendSingleMessage(request);
            return;
        }

        MessageQueue mq = messageQueueSelector.select(topicInfo.getMessageQueueList(), null, request);
        request.setMessageQueue(mq);

        requestQMap.computeIfAbsent(mq.getBrokerName(), _name -> {
            Deque<CarreraRequest> q = new ConcurrentLinkedDeque<>();
            addRequestQueue(q, _name, config.getMaxEncodeWorkerForEachBroker());
            return q;
        }).add(request);
    }

    /**
     * refer: {@link org.apache.rocketmq.common.message.MessageDecoder#encodeMessage}
     */
    public static boolean encode(ByteBuffer byteBuffer, CarreraMessage message, int topicIdx) {
        ByteBuffer body = message.bodyAsByteBuffer();
        int bodyLen = body.remaining();
        String properties = MessageDecoder.messageProperties2String(message.getRmqProperties());

        byte[] propertiesBytes = properties.getBytes(MessageDecoder.CHARSET_UTF8);
        if (propertiesBytes.length > Short.MAX_VALUE) {
            LogUtils.logError("BatchMQProducer.encode", "message properties size too large. message="
                + message + ",property.len=" + propertiesBytes.length);
            return false;
        }
        //note properties length must not more than Short.MAX
        short propertiesLength = (short) propertiesBytes.length;
        int storeSize = 4 // 1 TOTALSIZE
            + 4 // 2 MAGICCOD
            + 4 // 3 BODYCRC
            + 4 // 4 FLAG
            + 4 + bodyLen // 5 BODY
            + 2 + propertiesLength // 6 properties
            + 4 // 7 topicIdx
            + 4; //8 QueueId;

        if (byteBuffer.remaining() < storeSize) {
            LogUtils.logError("BatchMQProducer.encode",
                String.format("store size overflow. buffer.remaining:%d,%s,storeSize=%d,message=%s",
                    byteBuffer.remaining(), byteBuffer, storeSize, message));
            return false;
        }
        // 1 TOTALSIZE
        byteBuffer.putInt(storeSize);
        // 2 MAGICCODE
        byteBuffer.putInt(0);
        // 3 BODYCRC
        byteBuffer.putInt(0);
        // 4 FLAG
        byteBuffer.putInt(0);
        // 5 BODY
        byteBuffer.putInt(bodyLen);
        byteBuffer.put(body);

        // 6 properties
        byteBuffer.putShort(propertiesLength);
        byteBuffer.put(propertiesBytes);

        // 7 topicIdx
        byteBuffer.putInt(topicIdx);
        //8 QueueId;
        byteBuffer.putInt(message.getMessageQueue().getQueueId());
        return true;
    }
}