package com.xiaojukeji.carrera.consumer.thrift.client;

import com.xiaojukeji.carrera.consumer.thrift.AckResult;
import com.xiaojukeji.carrera.consumer.thrift.Context;
import com.xiaojukeji.carrera.consumer.thrift.FetchRequest;
import com.xiaojukeji.carrera.consumer.thrift.FetchResponse;
import com.xiaojukeji.carrera.consumer.thrift.Message;
import com.xiaojukeji.carrera.consumer.thrift.QidResponse;
import org.apache.thrift.TException;
import org.apache.thrift.transport.TTransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;


public class LowLevelCarreraConsumer extends BaseCarreraConsumer<FetchRequest, FetchResponse> {
    private static final Logger LOGGER = LoggerFactory.getLogger(LowLevelCarreraConsumer.class);
    public static final String CARRERA_CONSUMER_TYPE = "LowLevel";

    private final String consumerId;

    private long lastAckTime = 0;
    private Map<String/*Topic*/, Map<String/*QID*/, Long>> offsetMap = new HashMap<>();

    /**
     * 单线程消费一个consumer proxy的客户端。config.servers只能指定一个server。
     *
     * @param config
     */
    public LowLevelCarreraConsumer(LowLevelCarreraConfig config) {
        this(config, null);

    }

    /**
     * 只消费group中指定topic的消息。
     *
     * @param config 消费的配置
     * @param topic  指定消费的topic。null表示不指定。
     */
    public LowLevelCarreraConsumer(LowLevelCarreraConfig config, String topic) {
        super(config, topic);
        this.type = CARRERA_CONSUMER_TYPE;
        this.consumerId = genConsumerId();
    }

    private static final AtomicInteger consumerNumber = new AtomicInteger(0);

    private String genConsumerId() {
        RuntimeMXBean runtime = ManagementFactory.getRuntimeMXBean();
        String name = runtime.getName(); // format: "pid@hostname"
        return name + "#" + consumerNumber.getAndIncrement();
    }

    @Override
    protected synchronized void initRequest() {
        request = new FetchRequest(this.consumerId, config.getGroupId(), ((LowLevelCarreraConfig) config).getClusterName());
        request.setVersion(getVersion());
        request.setMaxBatchSize(config.getMaxBatchSize());
        request.setMaxLingerTime(config.getMaxLingerTime());
        request.setFetchOffset(new HashMap<String, Map<String, Long>>());
    }

    @Override
    protected void doClose() throws Exception {
        commitAck();
    }

    public synchronized boolean commitAck() {
        if (offsetMap.size() == 0) {
            return true;
        }
        AckResult ret = new AckResult(this.consumerId, this.config.getGroupId(), ((LowLevelCarreraConfig) config).getClusterName(), offsetMap);
        for (int i = 0; i < config.getSubmitMaxRetries(); i++) {
            try {
                ensureConnection();
                if (client.ack(ret)) {
                    LOGGER.debug("Client.ackResult={}", ret);
                    offsetMap.clear();
                    return true;
                } else {
                    LOGGER.warn("Client.ackResult failed!, retryCnt=" + i);
                }
            } catch (TTransportException e) {
                LOGGER.error("Client.ackResult transport error, retryCnt=" + i, e);
                transport.close();
            } catch (TException e) {
                LOGGER.error("Client.ackResult error, retryCnt=" + i, e);
            }
            doRetrySleep();
        }
        return false;
    }

    public synchronized void ack(String topic, String qid, long offset) {
        updateOffsetMap(topic, qid, offset + 1);
    }

    @Override
    protected FetchResponse doPullMessage() throws TException {
        FetchResponse response = client.fetch(request);
        LOGGER.debug("Client Request for {}:{}, Response:{}", host, request, response);
        if (response == null || response.getResults() == null) {
            LOGGER.debug("retry in {}ms, response={}", config.getRetryInterval(), response);
            return null;
        }

        request.getFetchOffset().clear();
        for (QidResponse qidResponse : response.getResults()) {
            updateRequestOffsets(qidResponse);
        }
        return response;
    }

    private void updateRequestOffsets(QidResponse qidResponse) {
        if (qidResponse.isSetNextRequestOffset()) {
            Map<String, Long> qidOffsetMap = request.getFetchOffset().get(qidResponse.getTopic());
            if (qidOffsetMap == null) {
                qidOffsetMap = new HashMap<>();
                request.putToFetchOffset(qidResponse.getTopic(), qidOffsetMap);
            }
            qidOffsetMap.put(qidResponse.getQid(), qidResponse.getNextRequestOffset());
        }
    }

    private synchronized void updateOffsetMap(String topic, String qid, long offset) {
        if (offsetMap.containsKey(topic)) {
            offsetMap.get(topic).put(qid, offset);
        } else {
            Map<String/*QID*/, Long> qidMap = new HashMap<>();
            qidMap.put(qid, offset);
            offsetMap.put(topic, qidMap);
        }
    }

    @Override
    protected void doProcessMessage(FetchResponse response, MessageProcessor processor) {

        for (QidResponse qidResponse : response.getResults()) {
            if (qidResponse.getMessages() == null || qidResponse.getMessages().size() == 0) {
                continue;
            }
            for (Message msg : qidResponse.getMessages()) {
                Context context = new Context(config.getGroupId(), qidResponse.getTopic(), qidResponse.getQid());
                try {
                    MessageProcessor.Result pResult = processor.process(msg, context);
                    LOGGER.debug("ProcessResult:{},msg.key={},group={},topic={},qid={}，offset={}", pResult,
                            msg.getKey(), context.getGroupId(), context.getTopic(), context.getQid(), msg.getOffset());

                    ack(qidResponse.getTopic(), qidResponse.getQid(), msg.getOffset());
                } catch (Throwable e) {
                    LOGGER.error("exception when processing message, msg=" + msg + ",context=" + context, e);
                }
            }
        }

        tryCommitAck();
    }

    public boolean tryCommitAck() {
        boolean ret = true;
        if (System.currentTimeMillis() - lastAckTime > ((LowLevelCarreraConfig) config).getCommitAckInterval()) {
            ret = commitAck();
            lastAckTime = System.currentTimeMillis();
        }
        return ret;
    }

    @Override
    public String toString() {
        return "LowLevelCarreraConsumer{" +
                super.toString() +
                ", consumerId=" + consumerId +
                '}';
    }
}