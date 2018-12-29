package com.xiaojukeji.carrera.cproxy.consumer;

import com.xiaojukeji.carrera.config.v4.cproxy.UpstreamTopic;
import com.xiaojukeji.carrera.cproxy.actions.Action;
import com.xiaojukeji.carrera.cproxy.actions.ActionBuilder;
import com.xiaojukeji.carrera.cproxy.actions.hbase.HbaseCommand;
import com.xiaojukeji.carrera.cproxy.actions.http.HttpParam;
import com.xiaojukeji.carrera.cproxy.actions.redis.RedisCommand;
import com.xiaojukeji.carrera.cproxy.utils.LogUtils;
import com.xiaojukeji.carrera.cproxy.utils.MetricUtils;
import com.xiaojukeji.carrera.cproxy.utils.StringUtils;
import com.xiaojukeji.carrera.thrift.consumer.Message;
import com.xiaojukeji.carrera.thrift.consumer.consumerProxyConstants;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.xiaojukeji.carrera.chronos.constants.Constant.PROPERTY_KEY_FROM_CHRONOS;
import static com.xiaojukeji.carrera.cproxy.actions.http.HttpParam.HttpParamType.FORM;
import static com.xiaojukeji.carrera.cproxy.actions.http.HttpParam.HttpParamType.HEADER;
import static com.xiaojukeji.carrera.cproxy.actions.http.HttpParam.HttpParamType.QUERY;


public class UpstreamJob {

    private static final Logger LOGGER = LoggerFactory.getLogger(UpstreamJob.class);

    private static final Logger DROP_LOGGER = LogUtils.DROP_LOGGER;

    private final UpstreamTopic upstreamTopic;

    private final CommonMessage message;

    private final ConsumeContext context;

    private final ResultCallBack resultCallBack;

    private final List<Callback> jobFinishedCallbacks;

    private final Map<String, Action> actionMap;

    private volatile String state; // track message state。

    private volatile int actionIndex; // index of current executing job.

    private volatile boolean isTerminated;

    private volatile Object data;

    private volatile UpstreamJob next = null; // used to build order list with the same orderId.

    private volatile Integer orderId;

    private volatile List<HttpParam> httpParams;

    private volatile long pullTimestamp;

    private volatile int retryIdx = -1; // for topic.retryIntervals.

    private volatile int errorRetryCnt = 0; // for topic.maxErrorRetry.

    private volatile List<RedisCommand> redisCommands;

    private volatile List<HbaseCommand> hbaseCommands;

    private int workerId;

    private int delayRequestHandlerThreads;

    public UpstreamJob(CarreraConsumer consumer, UpstreamTopic topicConf, CommonMessage message, ConsumeContext context, ResultCallBack resultCallBack) {
        this.upstreamTopic = topicConf;
        this.message = message;
        this.context = context;
        this.resultCallBack = resultCallBack;
        this.actionMap = consumer.getActionMap();
        this.jobFinishedCallbacks = Collections.synchronizedList(new ArrayList<>(4));
        this.actionIndex = 0;
        this.data = message.getValue();
        this.isTerminated = false;
        this.state = "init";
        this.delayRequestHandlerThreads = consumer.getConfig().getDelayRequestHandlerThreads();
    }

    public void setWorkerId(int workerId) {
        this.workerId = workerId;
    }

    public long getPullTimestamp() {
        return pullTimestamp;
    }

    public void setPullTimestamp(long pullTimestamp) {
        this.pullTimestamp = pullTimestamp;
    }

    public String getQid() {
        return context.getQid();
    }

    public ConsumeContext getContext() {
        return context;
    }

    public long getOffset() {
        return context.getOffset();
    }

    public String getTags() {
        if (context.getOriginMessage() != null) {
            return context.getOriginMessage().getTags();
        } else {
            return null;
        }
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public List<String> getUrls() {
        return getUpstreamTopic().getUrls();
    }

    public Integer getOrderId() {
        return orderId;
    }

    public void setOrderId(Integer orderId) {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("orderId set to {}, job={}", orderId);
        }
        this.orderId = orderId;
    }

    public boolean canDoErrorRetry() {
        return errorRetryCnt < upstreamTopic.getMaxRetry() || upstreamTopic.getMaxRetry() == -1;
    }

    public int getRetryIdx() {
        return retryIdx;
    }

    public int nextRetryDelay() {
        List<Integer> retryIntervals = upstreamTopic.getRetryIntervals();
        retryIdx++;
        if (retryIdx < CollectionUtils.size(retryIntervals)) {
            int interval = retryIntervals.get(retryIdx);
            if (interval == -1) {
                retryIdx--;
                if (retryIdx >= 0) {
                    return retryIntervals.get(retryIdx);
                }
            } else {
                return interval;
            }
        }
        return -1;
    }

    public int getErrorRetryCnt() {
        return errorRetryCnt;
    }

    public int incErrorRetryCnt() {
        return ++errorRetryCnt;
    }

    public String getGroupId() {
        return context.getGroupId();
    }

    public UpstreamTopic getUpstreamTopic() {
        return upstreamTopic;
    }

    public String getTopic() {
        return upstreamTopic.getTopic();
    }

    public void addFormParam(String key, String value) {
        if (httpParams == null) httpParams = new ArrayList<>();
        httpParams.add(new HttpParam(FORM, key, value));
    }

    public void addQueryParam(String key, String value) {
        if (httpParams == null) httpParams = new ArrayList<>();
        httpParams.add(new HttpParam(QUERY, key, value));
    }

    public void addHttpHeader(String key, String value) {
        if (httpParams == null) httpParams = new ArrayList<>();
        httpParams.add(new HttpParam(HEADER, key, value));
    }

    public List<HttpParam> getHttpParams() {
        return httpParams;
    }

    public String getMsgKey() {
        return message.getKey();
    }

    public UpstreamJob getNext() {
        return next;
    }

    public CommonMessage getCommonMessage() {
        return message;
    }

    public String getTokenKey() {
        return this.upstreamTopic.getTokenKey() == null ? "" : this.upstreamTopic.getTokenKey();
    }

    public UpstreamJob setNextIfNull(UpstreamJob next) {
        if (this.next == null) {
            this.next = next;
            return null;
        }
        return this.next;
    }

    public void registerJobFinishedCallback(Callback onJobFinished) {
        jobFinishedCallbacks.add(onJobFinished);
    }

    public List<String> getActions() {
        return upstreamTopic.getActions();
    }

    public Message getPullMessage() {
        Message msg = new Message(message.getKey(), ByteBuffer.wrap(message.getValue()), getTags(), context.getOffset());
        msg.setProperties(context.getProperties());
        return msg;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public void addRedisCmd(RedisCommand cmd) {
        if (redisCommands == null) {
            redisCommands = new ArrayList<>();
        }
        redisCommands.add(cmd);
    }

    public List<RedisCommand> getRedisCommands() {
        return redisCommands;
    }

    public void addHbaseCmd(HbaseCommand cmd) {
        if (hbaseCommands == null) {
            hbaseCommands = new ArrayList<>();
        }
        hbaseCommands.add(cmd);
    }

    public List<HbaseCommand> getHbaseCommands() {
        return hbaseCommands;
    }

    public void execute() {
        if (!upstreamTopic.isPressureTraffic() && isPressureTrafficMessage()) {
            onFinished(true);
            LOGGER.debug("pressure traffic message, do not send, group:{}, topic:{}, key:{}", getGroupId(), getTopic(), message.getKey());
            return;
        }
        if (actionIndex == CollectionUtils.size(getActions())) {
            onFinished(true);
            return;
        }
        String actionName = getActions().get(actionIndex++);
        state = actionName;
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("job executing... {} actionIndex={}, act={}, thread={}", info(), actionIndex - 1, actionName, Thread.currentThread());
        }

        Action action = actionMap.get(actionName);
        if (action == null) {
            LOGGER.error("wrong act: {}", actionName);
            onFinished(false);
            return;
        }

        if (isTerminated) {
            LOGGER.info("job is terminated! job={}", info());
            terminate();
            return;
        }

        Action.Status status;
        try {
            status = action.act(this);
        } catch (Throwable e) {
            LOGGER.error("unexpected err, job=" + info(), e);
            onFinished(false);
            return;
        }
        switch (status) {
            case FAIL:
                LOGGER.error("execute error,job={}", info());
                onFinished(false);
                break;
            case FINISH:
                onFinished(true);
                break;
            case ASYNCHRONIZED:
                break;
            case CONTINUE:
                execute();
        }
    }

    private boolean isPressureTrafficMessage() {
        if (context.getProperties() != null && context.getProperties().containsKey(consumerProxyConstants.PRESSURE_TRAFFIC_KEY)) {
            return Boolean.valueOf(context.getProperties().get(consumerProxyConstants.PRESSURE_TRAFFIC_KEY));
        }

        return false;
    }

    public synchronized void onFinished(boolean success) {
        LOGGER.debug("job.onFinished({}), job={}", success, this);
        for (int i = jobFinishedCallbacks.size() - 1; i >= 0; i--) {
            try {
                jobFinishedCallbacks.get(i).callback(this, success);
            } catch (Throwable t) {
                LOGGER.error("Job.Finish callback.i=" + i + info(), t);
            }
        }
        resultCallBack.setResult(success);
        if (actionIndex < CollectionUtils.size(getActions())) {
            MetricUtils.put(this.getGroupId(), this.getTopic(), MetricUtils.IneffectiveMessage);
            if (!success) {
                DROP_LOGGER.info("[job:{},msg:{}]", this.info(), StringUtils.newString(this.getCommonMessage().getValue()));
            }
        } else {
            MetricUtils.put(this.getGroupId(), this.getTopic(), MetricUtils.EffectiveMessage);
        }
    }

    public void markTerminate() {
        LOGGER.info("job.markTerminate. job={},state={}", this, state);
        setState("markTerminate");
        isTerminated = true;
    }

    public void terminate() {
        onFinished(true);// 这条消息的处理结果不会被提交到server。
    }

    public boolean isTerminated() {
        return isTerminated;
    }

    public String info() {
        return "UpstreamJob{" +
                "group:" + getGroupId() +
                ",topic:" + getTopic() +
                ",qid:" + getQid() +
                ",offset:" + getOffset() +
                ",key:" + getMsgKey() +
                ",state:" + getState() +
                '}';
    }

    @Override
    public String toString() {
        return info();
    }

    public boolean canProcessDelayRequest() {
        return delayRequestHandlerThreads <= 0 || workerId < delayRequestHandlerThreads;
    }

    private enum ConsumeType {
        SDK, HTTP, REDIS, UNKNOWN
    }

    public String getConsumeType() {
        int lastActionIdx = getActions().size() - 1;
        try {
            switch (getActions().get(lastActionIdx)) {
                case ActionBuilder.ASYNC_HTTP:
                    return ConsumeType.HTTP.toString();
                case ActionBuilder.PULL_SERVER:
                    return ConsumeType.SDK.toString();
                case ActionBuilder.REDIS:
                    return ConsumeType.REDIS.toString();
                default:
                    return ConsumeType.UNKNOWN.toString();
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            return ConsumeType.UNKNOWN.toString();
        }
    }

    public interface Callback {
        void callback(UpstreamJob job, boolean success);
    }

    public boolean isFromChronos() {
        if (this.context.getProperties() == null || this.context.getProperties().size() == 0) {
            return false;
        }

        return this.context.getProperties().containsKey(PROPERTY_KEY_FROM_CHRONOS);

    }
}