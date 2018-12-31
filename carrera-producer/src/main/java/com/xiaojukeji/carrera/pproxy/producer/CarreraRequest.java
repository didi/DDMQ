package com.xiaojukeji.carrera.pproxy.producer;

import com.alibaba.fastjson.annotation.JSONField;
import com.xiaojukeji.carrera.thrift.Message;
import com.xiaojukeji.carrera.thrift.Result;
import com.xiaojukeji.carrera.pproxy.constants.Constant;
import com.xiaojukeji.carrera.pproxy.utils.ConfigConvertUtils;
import com.xiaojukeji.carrera.pproxy.utils.MetricUtils;
import com.xiaojukeji.carrera.pproxy.utils.MsgCheckUtils;
import com.xiaojukeji.carrera.pproxy.utils.RandomUtils;
import com.xiaojukeji.carrera.pproxy.utils.StatsUtils;
import com.xiaojukeji.carrera.pproxy.utils.TimeUtils;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.PartitionInfo;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.apache.thrift.async.AsyncMethodCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.xiaojukeji.carrera.pproxy.constants.Constant.DEFAULT_MQ_ASYNC_REQUEST_TIMEOUT_MS;


public class CarreraRequest extends CarreraMessage implements SendCallback {

    private static final Logger LOGGER = LoggerFactory.getLogger(CarreraRequest.class);

    @JSONField(serialize = false)
    protected ProducerPool producerPool;

    @JSONField(serialize = false)
    protected boolean sync;

    protected long timeout;

    protected String brokerCluster;

    protected long startTime = TimeUtils.getCurTime();

    @JSONField(serialize = false)
    protected AsyncMethodCallback<Result> resultHandler;

    @JSONField(serialize = false)
    protected volatile boolean finished = false;

    @JSONField(serialize = false)
    protected Timeout timeoutHandle;

    @JSONField(serialize = false)
    protected volatile int limiterFailureRetryCount = 0;

    public CarreraRequest() {
    }

    public CarreraRequest(ProducerPool producerPool, Message message, long timeout, AsyncMethodCallback resultHandler) {
        this(producerPool, true, message, timeout, resultHandler);
    }

    public CarreraRequest(ProducerPool producerPool, Message message, AsyncMethodCallback resultHandler) {
        this(producerPool, false, message, DEFAULT_MQ_ASYNC_REQUEST_TIMEOUT_MS, resultHandler);
    }

    public CarreraRequest(ProducerPool producerPool, boolean sync, Message message, long timeout, AsyncMethodCallback resultHandler) {
        super(message);
        this.producerPool = producerPool;
        this.sync = sync;
        this.timeout = timeout;
        this.resultHandler = resultHandler;
    }

    public void process() {
        ProxySendResult result = producerPool.send(this);
        if (result != ProxySendResult.ASYNC) {
            onFinish(result);
        }
    }

    public boolean checkValid() {
        if (!checkValidExceptBody(message)) {
            return false;
        }

        //校验body
        CarreraMessage.reformatMessage(message);

        if (message.body == null || message.body.remaining() == 0) {
            LOGGER.error("body is empty or length is too short, msg info:{}", toShortString());
            return false;
        }

        if (message.body.remaining() > producerPool.getConfigManager().getMaxMessageSize(message.getTopic())) {
            LOGGER.error("body is too long, msg info:{}", toShortString());
            return false;
        }

        return true;
    }

    protected boolean checkValidExceptBody(Message msg) {
        // check msg key.
        if (StringUtils.isNotEmpty(msg.getKey()) && msg.getKey().length() > 255) {
            LOGGER.warn("key is too long, topic={}, key={}", msg.getTopic(), msg.getKey());
            return false;
        }

        // check msg tags.
        if (StringUtils.isNotEmpty(msg.getTags()) && msg.getTags().length() > 255) {
            LOGGER.warn("tag is too long, topic={},  tag={}", msg.getTopic(), msg.getTags());
            return false;
        }

        //check msg properties
        String conflictProperty = MsgCheckUtils.checkProperties(message.getProperties());
        if (conflictProperty != null) {
            LOGGER.error("illegal properties(Conflicts with reserved attribute keywords):topic={}, {}", message.getTopic(), conflictProperty);
            return false;
        }

        // check for chronos
        String chronosInnerTopicPrefix = producerPool.getConfigManager().getProxyConfig().getCarreraConfiguration().getDelay().getChronosInnerTopicPrefix();
        if (StringUtils.isNotBlank(chronosInnerTopicPrefix)) {
            if (!isFromDelayRequest() && message.topic.startsWith(chronosInnerTopicPrefix)) {
                LOGGER.error("illegal topic for topic can not start with prefix={}, topic={}", chronosInnerTopicPrefix, message.topic);
                return false;
            }
        }

        return true;
    }

    public void checkTimeout(Timeout timeout) {
        LOGGER.info("checkTimeout, request={}", this);
        if (finished) return;
        TimeOutHandlerMgr.getTimeOutExecutor().execute(() -> {
            timeoutHandle = null;
            if (!tryRetrySend()) {
                onFinish(ProxySendResult.FAIL_TIMEOUT);
            }
        });
    }

    public boolean tryRetrySend() {
        //对批量接口不做重试
        if (this instanceof CarreraRequestForRMQBatch) {
            return false;
        }

        if(limiterFailureRetryCount > 0){
            return false;
        }

        if (!producerPool.getConfigManager().getTopicConfigManager().isStrongOrder(getTopic())) {
            int retries = getRetries();
            if (retries < Constant.DEFAULT_MQ_SEND_RETRY_TIMES) {
                LOGGER.info("do retry send!!!, topic:{}, msgId:{}", getTopic(), message.getKey());
                setRetries(retries + 1);
                registerTimeout(TimeOutHandlerMgr.selectOneTimeOutChecker(), Constant.DEFAULT_MQ_SEND_RETRY_TIMEOUT_MS);
                process();
                return true;
            }
        }
        return false;
    }

    public void setBrokerCluster(String brokerCluster) {
        this.brokerCluster = brokerCluster;
    }

    public String getBrokerCluster() {
        return brokerCluster;
    }

    public void setProducerPool(ProducerPool producerPool) {
        this.producerPool = producerPool;
    }

    public boolean isSync() {
        return sync;
    }

    public void setSync(boolean sync) {
        this.sync = sync;
    }

    @JSONField(serialize = false)
    public boolean isFinished() {
        return finished;
    }

    @JSONField(serialize = false)
    public double getElapseTime() {
        return TimeUtils.getElapseTime(startTime);
    }

    @JSONField(serialize = false)
    public long getTimeout() {
        return timeout;
    }

    @Override
    public String toString() {
        return toShortString();
    }

    public synchronized void onFinish(ProxySendResult result) {
        if (finished) {
            LOGGER.warn("duplicate onFinish! result={},{},{},request={},timeout:{},time:{}ms",
                    result, this, brokerCluster, sync ? "SYNC" : "ASYNC", timeout, TimeUtils.getElapseTime(startTime));
            return;
        }
        finished = true;

        if (resultHandler != null) {
            resultHandler.onComplete(result.getResult());
        }

        if (timeoutHandle != null) {
            timeoutHandle.cancel();
        }


        if (producerPool.getConfigManager().getTopicConfigManager().isDelayTopic(getTopic())) {
            MetricUtils.incQPSCounter(ConfigConvertUtils.addMarkForDelayTopic(getTopic()), result.toString());
        } else {
            MetricUtils.incQPSCounter(getTopic(), result.toString());
        }

        if (getRetries() == 0) { //do not collect messages retried
            long micros = TimeUtils.getElapseMicros(startTime);
            MetricUtils.putSendLatency(getTopic(), micros);
            if (producerPool.getConfigManager().getTopicConfigManager().isDelayTopic(getTopic())) {
                MetricUtils.putSendLatency(ConfigConvertUtils.addMarkForDelayTopic(getTopic()), micros);
            }
            StatsUtils.sendSync.add(micros);
        }

        LOGGER.info("sendResult:{},{},{},message:{},timeout:{},time:{}ms, limiterRetry:{}",
                result, brokerCluster, sync ? "SYNC" : "ASYNC", this, timeout, TimeUtils.getElapseTime(startTime), limiterFailureRetryCount);
    }

    public void onKafkaCompletion(RecordMetadata metadata, Exception exception) {
        if (isFinished()) return;
        if (exception != null) {
            onFinish(producerPool.handleSendException(this, exception));
        } else {
            onFinish(ProxySendResult.OK);
        }
    }

    /**
     * RMQ send callback
     *
     * @param sendResult
     */
    @Override
    public void onSuccess(SendResult sendResult) {
        if (isFinished()) return;
        if (sendResult.getSendStatus() != SendStatus.SEND_OK) {
            LOGGER.warn("RMQSendResult.Status={},request={},elapse:{}", sendResult.getSendStatus(), this, TimeUtils.getElapseTime(startTime));
        }
        onFinish(ProxySendResult.OK);
    }

    /**
     * RMQ send callback
     *
     * @param throwable
     */
    @Override
    public void onException(Throwable throwable) {
        if (isFinished()) return;
        if (producerPool == null) {
            onFinish(ProxySendResult.FAIL_UNKNOWN);
        } else {
            onFinish(producerPool.handleSendException(this, throwable));
        }
    }

    public org.apache.rocketmq.common.message.Message toRmqMessage() {
        org.apache.rocketmq.common.message.Message msg = new org.apache.rocketmq.common.message.Message(getTopic(), getTags(), getKey(), binary());
        Map<String, String> properties = this.message.getProperties();
        if (properties != null) {
            for (Map.Entry<String, String> kv : properties.entrySet()) {
                if (kv.getKey() != null && !kv.getKey().equals(""))
                    msg.putUserProperty(kv.getKey(), kv.getValue());
            }
        }
        return msg;
    }

    public ProducerRecord<String, byte[]> toKafkaRecord(KafkaProducer<String, byte[]> producer) {
        if (getPartitionId() >= 0) {
            return new ProducerRecord<>(getTopic(), getPartitionId(), getKey(), binary());
        } else if (getPartitionId() == -1) {
            List<PartitionInfo> partitionInfos = producer.partitionsFor(getTopic());
            return new ProducerRecord<>(getTopic(), (int) (getHashId() % partitionInfos.size()), getKey(), binary());
        } else {
            List<PartitionInfo> partitionInfos = producer.partitionsFor(getTopic());
            return new ProducerRecord<>(getTopic(), RandomUtils.nextInt(partitionInfos.size()), getKey(), binary());
        }
    }

    public void registerTimeout(HashedWheelTimer timeoutChecker) {
        registerTimeout(timeoutChecker, timeout);
    }

    public void registerTimeout(HashedWheelTimer timeoutChecker, long timeout) {
        if (timeout > 0) {
            timeoutHandle = timeoutChecker.newTimeout(this::checkTimeout, timeout, TimeUnit.MILLISECONDS);
        }
    }

    protected boolean isFromDelayRequest() {
        return false;
    }

    public int getLimiterFailureRetryCount() {
        return limiterFailureRetryCount;
    }

    public void addLimiterFailureRetryCount() {
        this.limiterFailureRetryCount++;
    }
}