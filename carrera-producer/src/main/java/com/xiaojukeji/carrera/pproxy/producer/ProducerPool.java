package com.xiaojukeji.carrera.pproxy.producer;

import com.xiaojukeji.carrera.pproxy.constants.TopicConfigValue;
import com.xiaojukeji.carrera.pproxy.producer.delay.DelayRequest;
import com.xiaojukeji.carrera.config.ConfigConstants;
import com.xiaojukeji.carrera.metric.MetricFactory;
import com.xiaojukeji.carrera.utils.CommonFastJsonUtils;
import com.xiaojukeji.carrera.pproxy.ratelimit.IGroupRequestLimiter;
import com.xiaojukeji.carrera.pproxy.ratelimit.TpsLimiter;
import com.xiaojukeji.carrera.pproxy.utils.ConfigConvertUtils;
import com.xiaojukeji.carrera.pproxy.utils.LogUtils;
import com.xiaojukeji.carrera.pproxy.utils.MetricUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.common.errors.RecordTooLargeException;
import org.apache.kafka.common.errors.UnknownTopicOrPartitionException;
import org.apache.rocketmq.client.common.ClientErrorCode;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.protocol.ResponseCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.xiaojukeji.carrera.chronos.constants.Constant.PROPERTY_KEY_FROM_CHRONOS;
import static com.xiaojukeji.carrera.pproxy.producer.ProxySendResult.ASYNC;
import static com.xiaojukeji.carrera.pproxy.producer.ProxySendResult.FAIL_ILLEGAL_MSG;
import static com.xiaojukeji.carrera.pproxy.producer.ProxySendResult.FAIL_NO_PRODUCER_FOR_CLUSTER;
import static com.xiaojukeji.carrera.pproxy.producer.ProxySendResult.FAIL_REFUSED_BY_RATE_LIMITER;
import static com.xiaojukeji.carrera.pproxy.producer.ProxySendResult.FAIL_TIMEOUT;
import static com.xiaojukeji.carrera.pproxy.producer.ProxySendResult.FAIL_TOPIC_IS_DELAY;
import static com.xiaojukeji.carrera.pproxy.producer.ProxySendResult.FAIL_TOPIC_NOT_ALLOWED;
import static com.xiaojukeji.carrera.pproxy.producer.ProxySendResult.FAIL_TOPIC_NOT_EXIST;
import static com.xiaojukeji.carrera.pproxy.producer.ProxySendResult.FAIL_UNKNOWN;


public class ProducerPool {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProducerPool.class);
    private static final Logger DROP_LOGGER = LogUtils.getDropLogger();

    private static final String DROP_REASON_MESSAGE_TOO_LARGE = "MESSAGE_TOO_LARGE";
    private static final String DROP_REASON_TOPIC_NOT_EXIST = "TOPIC_NOT_EXIST";
    private static final long LIMITER_FAILURE_RETRY_TIME_MS = 10;
    private static final int LIMITER_FAILURE_NOT_RETRY_COUNT = 0;

    private final ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor(r -> new Thread(r, "ProducerPoolScheduleThread"));

    private IGroupRequestLimiter requestLimiter;
    private ConfigManager configManager;
    private ProducerManager producerManager;
    private static ProducerPool INSTANCE = null;
    private final ScheduledThreadPoolExecutor limiterFailureRetrySchedule = new ScheduledThreadPoolExecutor(4, r -> new Thread(r, "LimiterFailureRetrySchedule"));


    public static synchronized void initInstance(ConfigManager configManager) {
        INSTANCE = new ProducerPool(configManager);
    }

    public static ProducerPool getInstance() {
        return INSTANCE;
    }

    private ProducerPool(ConfigManager configManager) {
        this.configManager = configManager;
        producerManager = new ProducerManager(configManager);
    }

    public void start() throws Exception {
        initMetric();

        if (configManager.getCarreraConfig().isUseRequestLimiter()) {
            initRateLimiter();
            configManager.registerLimiterForConfigUpdate(requestLimiter);
        }

        configManager.startWatchConfig();

        producerManager.initProducer();
        configManager.registerProducerForConfigUpdate(producerManager);
    }

    public void warmUp() {
        producerManager.warmUp();
    }

    private void initMetric() throws Exception {
        if (MapUtils.isNotEmpty(configManager.getTopicConfigManager().getTopicConfigs())) {
            configManager.getTopicConfigManager().getTopicConfigs().values().forEach(config -> {
                        MetricUtils.addTopic(config.getTopic());
                        if (config.isDelayTopic()) {
                            MetricUtils.addTopic(ConfigConvertUtils.addMarkForDelayTopic(config.getTopic()));
                        }
                    }
            );
        }
    }

    private void initRateLimiter() throws Exception {
        requestLimiter = new TpsLimiter(configManager.getCarreraConfig().getTpsWarningRatio(),
                configManager.getCarreraConfig().getMaxTps(), configManager.getTopicConfigManager());
        LOGGER.info("initRateLimiter finished");
    }

    public void close() {
        LOGGER.info("ProducerPool closing ...");
        scheduledExecutorService.shutdown();

        LOGGER.info("start closing producer");
        producerManager.shutdown();

        if (requestLimiter != null) {
            requestLimiter.shutdown();
        }

        MetricFactory.destroy();
        LOGGER.info("ProducerPool closed");
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public int getPartitionsSize(String topic, String brokerCluster) {
        if (StringUtils.isEmpty(brokerCluster)) {
            return 1;
        }
        ClusterProducer producer = producerManager.getProducer(brokerCluster);
        if (producer != null) {
            return producer.getPartitionsSize(topic);
        } else {
            throw new RuntimeException("no producer for cluster:" + brokerCluster);
        }
    }

    public ProxySendResult send(CarreraRequest request) {
        String brokerCluster = getBrokerCluster(request);
        if (StringUtils.isEmpty(brokerCluster)) {
            return FAIL_TOPIC_NOT_ALLOWED;
        }

        String topic = request.getTopic();
        if (request.isFromDelayRequest()) {
            // 检查用户 topic 是否被限流
            topic = ((DelayRequest) request).getOriginTopic();
        } else {
            // 具有延迟属性的 topic 不能发送给 sendSync 接口(除非是从 chronos 发送过来)
            if (isDelayTopic(request.getTopic()) && !isFromChronos(request)) {
                LOGGER.error("topic is delay, topic:{}", request.getTopic());
                return FAIL_TOPIC_IS_DELAY;
            }
            //msg expired, from chronos
            if (isDelayTopic(topic)) {
                topic = getConfigManager().getProxyConfig().getCarreraConfiguration().getDelay().getChronosInnerTopicPrefix() + "0";
            }
        }

        if (configManager.getCarreraConfig().isUseRequestLimiter() && !requestLimiter.tryEnter(topic)) {
            MetricUtils.incLimitCounter(topic);
            if (retryLimiterFailureRequest(request, topic)) {
                return ASYNC;
            } else {
                return FAIL_REFUSED_BY_RATE_LIMITER;
            }
        }

        try {
            ClusterProducer producer = producerManager.getProducer(brokerCluster);
            if (producer != null) {
                producer.send(request);
                return ASYNC;
            } else {
                LogUtils.logError("ProducerPool.send",
                        "not get producer by brokerCluster:" + brokerCluster + ", request:" + request);
                return FAIL_NO_PRODUCER_FOR_CLUSTER;
            }

        } catch (Throwable t) {
            return handleSendException(request, t);
        }
    }


    private boolean retryLimiterFailureRequest(CarreraRequest request, String realTopicName) {
        if (request.getLimiterFailureRetryCount() > LIMITER_FAILURE_NOT_RETRY_COUNT) {
            return false;
        }

        int queueSize = configManager.getProxyConfig().getCarreraConfiguration().getLimiterFailureRetryQueueSize();
        if (limiterFailureRetrySchedule.getQueue().size() > queueSize) {
            LOGGER.debug("limiterFailureRetrySchedule queue size is larger than threshold({})", queueSize);
            return false;
        }

        long retryInterval;
        long retryIntervalTopic = configManager.getTopicConfigManager().getTopicConfigs().get(realTopicName).getLimiterFailureRetryInterval();
        if (retryIntervalTopic == ConfigConstants.LIMITER_FAILURE_RETRY_INTERVAL_DISABLE) {
            return false;
        } else if (retryIntervalTopic == ConfigConstants.LIMITER_FAILURE_RETRY_INTERVAL_FROM_CLUSTER) {
            long timeout = request.timeout - LIMITER_FAILURE_RETRY_TIME_MS;

            if (timeout <= TopicConfigValue.LIMITER_FAILURE_RETRY_INTERVAL_MIN_MS) {
                return false;
            }
            retryInterval = Math.min(timeout, TopicConfigValue.LIMITER_FAILURE_RETRY_INTERVAL_MAX_MS);
        } else {
            retryInterval = retryIntervalTopic;
        }

        limiterFailureRetrySchedule.schedule(request::process, retryInterval, TimeUnit.MILLISECONDS);
        LOGGER.debug("limiter failure retry, topic:{}, key:{}, retryInterval:{}", realTopicName, request.getKey(), retryInterval);
        request.addLimiterFailureRetryCount();

        return true;
    }

    private String getBrokerCluster(CarreraRequest request) {
        String brokerCluster = request.getBrokerCluster();
        if (brokerCluster == null) {
            brokerCluster = configManager.getTopicConfigManager().getBrokerCluster(request.message);
            request.setBrokerCluster(brokerCluster);
        }
        return brokerCluster;
    }

    public ProxySendResult handleSendException(CarreraRequest message, Throwable exception) {
        ProxySendResult ret = FAIL_UNKNOWN;

        String errDetail = "";
        boolean printStackTrace = false;

        if (exception instanceof TimeoutException) {
            errDetail = String.format("shortmsg:%s, TimeoutException:%s", message.toShortString(), exception.getMessage());
            ret = FAIL_TIMEOUT;
        } else if (exception instanceof MQBrokerException) {
            MQBrokerException e = (MQBrokerException) exception;
            if (e.getResponseCode() == ResponseCode.TOPIC_NOT_EXIST) {
                dropMessage(DROP_REASON_TOPIC_NOT_EXIST, message, e);
                ret = FAIL_TOPIC_NOT_EXIST;
            } else {
                errDetail = String.format("shortmsg:%s, exceptionCode:%d", message.toShortString(), e.getResponseCode());
                printStackTrace = true;
            }
        } else if (exception instanceof MQClientException) {
            MQClientException e = (MQClientException) exception;
            if (e.getResponseCode() == ClientErrorCode.NOT_FOUND_TOPIC_EXCEPTION ||
                    e.getResponseCode() == ResponseCode.TOPIC_NOT_EXIST) {
                dropMessage(DROP_REASON_TOPIC_NOT_EXIST, message, e);
                ret = FAIL_TOPIC_NOT_EXIST;
            } else if (e.getResponseCode() == ResponseCode.MESSAGE_ILLEGAL) {
                dropMessage(DROP_REASON_MESSAGE_TOO_LARGE, message, e);
                ret = FAIL_ILLEGAL_MSG;
            } else {
                errDetail = String.format("shortmsg:%s, exceptionCode:%d", message.toShortString(), e.getResponseCode());
                printStackTrace = true;
            }
        } else if (exception instanceof RecordTooLargeException) {
            dropMessage(DROP_REASON_MESSAGE_TOO_LARGE, message, exception);
            ret = FAIL_ILLEGAL_MSG;
        } else if (exception instanceof UnknownTopicOrPartitionException) {
            dropMessage(DROP_REASON_TOPIC_NOT_EXIST, message, exception);
            ret = FAIL_TOPIC_NOT_EXIST;
        } else if (exception.getCause() != null) {
            return handleSendException(message, exception.getCause());
        } else if (StringUtils.containsIgnoreCase(exception.getMessage(), "timeout")) {
            errDetail = String.format("shortmsg:%s, TimeoutException:%s", message.toShortString(), exception.getMessage());
        } else {
            errDetail = String.format("shortmsg:%s", message.toShortString());
            printStackTrace = true;
        }

        if (errDetail.isEmpty()) {
            errDetail = String.format("%s, shortmsg:%s", ret.getResult().getMsg(), message.toShortString());
        }

        LogUtils.logError("ProducerPool." + (message.isSync() ? "SendSync" : "SendAsync"), errDetail, exception, printStackTrace);

        return ret;
    }

    private void dropMessage(String reason, CarreraMessage message, Throwable e) {
        LOGGER.info("DROP_MSG:{},MESSAGE:{},EXCEPTION:{}", reason, message.toShortString(),
                e == null ? null : e.getMessage());
        MetricUtils.incDropCounter(message.getTopic(), reason);
        if (reason.equals(DROP_REASON_TOPIC_NOT_EXIST) || reason.equals(DROP_REASON_MESSAGE_TOO_LARGE)) {
            return;
        }
        DROP_LOGGER.info("REASON:{},CARRERA_MESSAGE_JSON:{}", reason, CommonFastJsonUtils.toJsonStringDefault(message));
    }

    public ClusterProducer getProducer(String brokerCluster) {
        return producerManager.getProducer(brokerCluster);
    }

    public boolean isDelayTopic(final String topic) {
        return configManager.getTopicConfigManager().isDelayTopic(topic);
    }

    public boolean isFromChronos(final CarreraRequest request) {
        if (request.getRmqProperties() == null || request.getRmqProperties().size() == 0) {
            return false;
        }

        return request.getRmqProperties().containsKey(PROPERTY_KEY_FROM_CHRONOS);
    }
}