package com.xiaojukeji.carrera.pproxy.producer.delay;

import com.xiaojukeji.carrera.chronos.enums.Actions;
import com.xiaojukeji.carrera.chronos.enums.MsgTypes;
import com.xiaojukeji.carrera.chronos.model.BodyExt;
import com.xiaojukeji.carrera.chronos.model.InternalKey;
import com.xiaojukeji.carrera.thrift.DelayMessage;
import com.xiaojukeji.carrera.thrift.DelayResult;
import com.xiaojukeji.carrera.thrift.Message;
import com.xiaojukeji.carrera.utils.CommonFastJsonUtils;
import com.xiaojukeji.carrera.pproxy.producer.CarreraRequest;
import com.xiaojukeji.carrera.pproxy.producer.ProducerPool;
import com.xiaojukeji.carrera.pproxy.producer.ProxySendResult;
import com.xiaojukeji.carrera.pproxy.utils.MetricUtils;
import com.xiaojukeji.carrera.pproxy.utils.TimeUtils;
import io.netty.util.Timeout;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.thrift.async.AsyncMethodCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class DelayRequest extends CarreraRequest {

    private static final Logger LOGGER = LoggerFactory.getLogger(DelayRequest.class);

    private static final int PARTITION_BY_HASH = -1;

    private static final long TIME_AFTER_TIMESTAMP = 2 * 24 * 60 * 60;

    private static final long MAX_DELAY_SECONDS = 62 * 24 * 60 * 60; // 62天

    private static final int MIN_INTERVAL = 3;

    private static final int MIN_EXPONENT_BASE = 2;

    private AsyncMethodCallback<DelayResult> resultHandler;

    private String uniqDelayMsgId;

    private DelayMessage delayMessage;

    public DelayRequest(ProducerPool producerPool, DelayMessage delayMessage, Message message, long timeout, AsyncMethodCallback resultHandler) {
        super(producerPool, message, timeout, resultHandler);
        this.resultHandler = resultHandler;
        this.delayMessage = delayMessage;
    }

    @Override
    public void process() {
        ProxySendResult result = producerPool.send(this);
        if (result != ProxySendResult.ASYNC) {
            onFinish(new ProxySendDelayResult(result));
        }
    }

    @Override
    public void checkTimeout(Timeout timeout) {
        LOGGER.info("delay checkTimeout, request={}", this);
        if (finished) return;
        timeoutHandle = null;
        if (!tryRetrySend()) {
            onFinish(ProxySendResult.FAIL_TIMEOUT);
        }
    }

    public synchronized void onFinish(ProxySendDelayResult result) {
        if (finished) {
            LOGGER.warn("delay duplicate onFinish! result={},{},{},request={},timeout:{},time:{}ms",
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

        MetricUtils.incQPSCounter(getTopic(), result.getProxySendResult().toString());
        MetricUtils.incQPSCounter(getOriginTopic(), result.getProxySendResult().toString());
        if (getRetries() == 0) {//do not collect messages retried
            MetricUtils.putSendLatency(getTopic(), Math.round(TimeUtils.getElapseTime(startTime)));
            MetricUtils.putSendLatency(getOriginTopic(), Math.round(TimeUtils.getElapseTime(startTime)));
        }

        LOGGER.info("sendDelayResult:{},{},{}, origin topic:{}, message:{},timeout:{},time:{}ms, limiterRetry:{}",
                result, brokerCluster, sync ? "SYNC" : "ASYNC", getOriginTopic(), this, timeout,
                TimeUtils.getElapseTime(startTime), limiterFailureRetryCount);
    }

    public String getOriginTopic() {
        return delayMessage.getTopic();
    }

    @Override
    public void onSuccess(SendResult sendResult) {
        if (isFinished()) return;
        onFinish(new ProxySendDelayResult(ProxySendResult.OK, this.uniqDelayMsgId));
    }

    @Override
    public void onException(Throwable throwable) {
        if (isFinished()) return;
        onFinish(new ProxySendDelayResult(ProxySendResult.FAIL_TIMEOUT));
    }

    public void buildAndUpdateMessage() {
        DelayMessage dMsg = delayMessage;
        long hashId;
        int innerTopicSeq;

        // [timestamp]-[type]-[expire]-[times]-[loopTimes]-[interval]-[innerTopicSeq]-[uuid]
        // normal uniqDelayMsgId: 1507609164-2-1507609214-0-0-0-0-14f22520-ad72-11e7-91be-6ee54da579e3
        // loop uniqDelayMsgId:   1507609164-3-1507609214-9-0-0-0-14f22520-ad72-11e7-91be-6ee54da579e3
        if (dMsg.getAction() == Actions.ADD.getValue()) {
            hashId = dMsg.getUuid().hashCode();
            innerTopicSeq = getInnerTopicSeq(hashId, producerPool.getConfigManager().getProxyConfig().getCarreraConfiguration().getDelay().getInnerTopicNum());

            long expire = dMsg.getExpire();
            if (dMsg.getDmsgtype() == MsgTypes.DELAY.getValue()) {
                expire = dMsg.getTimestamp() + TIME_AFTER_TIMESTAMP;
            }
            uniqDelayMsgId = new InternalKey(dMsg.getTimestamp(), dMsg.getDmsgtype(), expire, dMsg.getTimes(), 0L,
                    dMsg.getInterval(), innerTopicSeq, dMsg.getUuid()).genUniqDelayMsgId();
        } else {
            final InternalKey internalKey = new InternalKey(dMsg.getUniqDelayMsgId());
            hashId = internalKey.getUuid().hashCode();
            innerTopicSeq = internalKey.getInnerTopicSeq();
            uniqDelayMsgId = dMsg.getUniqDelayMsgId();
        }

        final BodyExt bodyExt = new BodyExt()
                .setTopic(dMsg.getTopic())
                .setAction(dMsg.getAction())
                .setTags(dMsg.getTags())
                .setUniqDelayMsgId(uniqDelayMsgId)
                .setBody((dMsg.getBody() == null) ? StringUtils.EMPTY : new String(dMsg.getBody()))
                .setProperties(dMsg.getProperties());

        final Message msg = new Message();
        msg.setTags(dMsg.getTags());
        msg.setVersion(dMsg.getVersion());
        msg.setPartitionId(PARTITION_BY_HASH);
        msg.setTopic(getInnerTopic(innerTopicSeq, producerPool));
        msg.setHashId(hashId);
        msg.setBody(CommonFastJsonUtils.toJsonStringDefault(bodyExt).getBytes());
        msg.setKey(uniqDelayMsgId);

        message = msg;
    }

    private static String getInnerTopic(long innerTopicSeq, ProducerPool producerPool) {
        return producerPool.getConfigManager().getProxyConfig().getCarreraConfiguration().getDelay().getChronosInnerTopicPrefix() + innerTopicSeq;
    }

    private static int getInnerTopicSeq(long hashId, int innerTopicNum) {
        return (int) (hashId % innerTopicNum);
    }

    public ProxySendDelayResult checkDelayMessage() {

        // 检查topic是否在白名单中
        if (!producerPool.getConfigManager().getTopicConfigManager().containsTopic(delayMessage.getTopic())) {
            LOGGER.error("topic is not in the proxy white list, topic:{}", delayMessage.getTopic());
            return new ProxySendDelayResult(ProxySendResult.FAIL_TOPIC_NOT_ALLOWED);
        }

        // 不能给以chronos_inner_开头的topic发送消息
        if (delayMessage.getTopic().startsWith(producerPool.getConfigManager().getProxyConfig().getCarreraConfiguration().getDelay().getChronosInnerTopicPrefix())) {
            LOGGER.error("illegal topic for topic can not start with prefix={}, topic={}", producerPool.getConfigManager().getProxyConfig().getCarreraConfiguration().getDelay().getChronosInnerTopicPrefix(), delayMessage.getTopic());
            return new ProxySendDelayResult(ProxySendResult.FAIL_ILLEGAL_MSG);
        }

        // 只能给sendDelay接口发送有延时属性的topic
        if (!producerPool.isDelayTopic(delayMessage.getTopic())) {
            LOGGER.error("topic is not delay, topic:{}", delayMessage.getTopic());
            return new ProxySendDelayResult(ProxySendResult.FAIL_TOPIC_IS_NOT_DELAY);
        }

        if (delayMessage.getAction() == Actions.CANCEL.getValue()) {

            // 取消延迟消息时, uniqDelayMsgId不能为空
            if (StringUtils.isBlank(delayMessage.getUniqDelayMsgId())) {
                LOGGER.error("illegal uniqDelayMsgId for it is blank");
                return new ProxySendDelayResult(ProxySendResult.FAIL_ILLEGAL_UNIQ_DELAY_MSG_ID, delayMessage.getUniqDelayMsgId());
            }

            // 取消延迟消息时, uniqDelayMsgId的格式必须严格按照格式
            String reg = "^\\d{10}-\\d-\\d{10}-\\d+-\\d+-\\d+-\\d+-[0-9a-z]{8}-[0-9a-z]{4}-[0-9a-z]{4}-[0-9a-z]{4}-[0-9a-z]{12}$";
            Pattern p = Pattern.compile(reg);
            Matcher m = p.matcher(delayMessage.getUniqDelayMsgId());
            if (!m.find()) {
                LOGGER.error("illegal uniqDelayMsgId:{}", delayMessage.getUniqDelayMsgId());
                return new ProxySendDelayResult(ProxySendResult.FAIL_ILLEGAL_UNIQ_DELAY_MSG_ID, delayMessage.getUniqDelayMsgId());
            }

            final InternalKey internalKey = new InternalKey(delayMessage.getUniqDelayMsgId());

            // 消息类型只支持一次延迟、循环延迟和循环指数延迟三种
            if (internalKey.getType() != MsgTypes.DELAY.getValue()
                    && internalKey.getType() != MsgTypes.LOOP_DELAY.getValue()
                    && internalKey.getType() != MsgTypes.LOOP_EXPONENT_DELAY.getValue()) {
                LOGGER.error("illegal delay message type:{}", internalKey.getType());
                return new ProxySendDelayResult(ProxySendResult.FAIL_ILLEGAL_DELAY_MSG_TYPE, delayMessage.getUniqDelayMsgId());
            }

            // 不能超过配置的inner topic的数量
            if (internalKey.getInnerTopicSeq() >= producerPool.getConfigManager().getProxyConfig().getCarreraConfiguration().getDelay().getInnerTopicNum()) {
                LOGGER.error("illegal delay inner topic seq:{}", internalKey.getInnerTopicSeq());
                return new ProxySendDelayResult(ProxySendResult.FAIL_ILLEGAL_DELAY_INNER_TOPIC_SEQ, delayMessage.getUniqDelayMsgId());
            }
        } else if (delayMessage.getAction() == Actions.ADD.getValue()) {

            // timestamp不能超过最大的延迟时间
            if (delayMessage.getTimestamp() < 1000000000 || delayMessage.getTimestamp() > (System.currentTimeMillis() / 1000 + MAX_DELAY_SECONDS)) {
                LOGGER.error("illegal delay timestamp:{}", delayMessage.getTimestamp());
                return new ProxySendDelayResult(ProxySendResult.FAIL_ILLEGAL_DELAY_TIMESTAMP);
            }

            // 消息类型只支持一次延迟、循环延迟和循环指数延迟三种
            if (delayMessage.getDmsgtype() != MsgTypes.DELAY.getValue()
                    && delayMessage.getDmsgtype() != MsgTypes.LOOP_DELAY.getValue()
                    && delayMessage.getDmsgtype() != MsgTypes.LOOP_EXPONENT_DELAY.getValue()) {
                LOGGER.error("illegal delay message type:{}", delayMessage.getDmsgtype());
                return new ProxySendDelayResult(ProxySendResult.FAIL_ILLEGAL_DELAY_MSG_TYPE, delayMessage.getUniqDelayMsgId());
            }

            // 如果是延迟类型, loop times 必须是0; 如果是循环延迟消息, loop times 必须大于0
            if (delayMessage.getDmsgtype() == MsgTypes.DELAY.getValue()) {
                if (delayMessage.getTimes() != 0) {
                    LOGGER.error("illegal delay loop times:{}", delayMessage.getTimes());
                    return new ProxySendDelayResult(ProxySendResult.FAIL_ILLEGAL_DELAY_LOOP_TIMES);
                }
            } else {
                if (delayMessage.getTimes() <= 0) {
                    LOGGER.error("illegal delay loop times:{}", delayMessage.getTimes());
                    return new ProxySendDelayResult(ProxySendResult.FAIL_ILLEGAL_DELAY_LOOP_TIMES);
                }

                if (delayMessage.getExpire() <= delayMessage.getTimestamp()) {
                    LOGGER.error("illegal delay expire:{} for le timestamp:{}", delayMessage.getExpire(), delayMessage.getTimestamp());
                    return new ProxySendDelayResult(ProxySendResult.FAIL_ILLEGAL_DELAY_LOOP_EXPIRE);
                }

                if (delayMessage.getDmsgtype() == MsgTypes.LOOP_DELAY.getValue()) {
                    if (delayMessage.getInterval() < MIN_INTERVAL) {
                        LOGGER.error("illegal delay interval:{} for lt minInterval:{}", delayMessage.getInterval(), MIN_INTERVAL);
                        return new ProxySendDelayResult(ProxySendResult.FAIL_ILLEGAL_DELAY_LOOP_INTERVAL);
                    }
                }

                if (delayMessage.getDmsgtype() == MsgTypes.LOOP_EXPONENT_DELAY.getValue()) {
                    if (delayMessage.getInterval() < MIN_EXPONENT_BASE) {
                        LOGGER.error("illegal delay exponent base:{} for lt minExponentBase:{}", delayMessage.getInterval(), MIN_EXPONENT_BASE);
                        return new ProxySendDelayResult(ProxySendResult.FAIL_ILLEGAL_DELAY_LOOP_EXPONENT_BASE);
                    }
                }
            }
        } else {
            LOGGER.error("illegal delay action:{}", delayMessage.getAction());
            return new ProxySendDelayResult(ProxySendResult.FAIL_ILLEGAL_DELAY_ACTION);
        }

        return new ProxySendDelayResult(ProxySendResult.OK);
    }

    @Override
    protected boolean isFromDelayRequest() {
        return true;
    }

    public static Message buildMessageForCheck(DelayMessage delayMessage) {
        Message message = new Message();
        message.setTopic(delayMessage.getTopic());
        message.setBody(delayMessage.getBody());
        message.setProperties(delayMessage.getProperties());
        message.setTags(delayMessage.getTags());
        return message;
    }
}