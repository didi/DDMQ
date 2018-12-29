package com.xiaojukeji.carrera.pproxy.utils;

import com.xiaojukeji.carrera.pproxy.metric.TopicMetrics;

import java.util.concurrent.ConcurrentHashMap;


public class MetricUtils {
    public static final String REQUEST_SYNC = "sync";
    public static final String REQUEST_DELAY_SYNC = "delaySync";
    public static final String REQUEST_ASYNC = "async";
    public static final String REQUEST_BATCHSYNC = "batchSync";
    public static final String TOPIC_ALL = "ALL";
    public static final String TOPIC_NOT_REGISTER = "topic_not_register";
    public static final String METRIC_REPORT_OPEN = "metric.report.open";
    public static final String RATE_LIMIT_TYPE_FAILED = "failed";
    public static final String RATE_LIMIT_TYPE_WARN = "warn";


    private static ConcurrentHashMap<String, TopicMetrics> topicMetrics = new ConcurrentHashMap<>();
    private static TopicMetrics allTopicMetric = new TopicMetrics(TOPIC_ALL);
    private static boolean started = ConfigUtils.getDefaultConfig(METRIC_REPORT_OPEN, true);

    static {
        topicMetrics.put(TOPIC_NOT_REGISTER, new TopicMetrics(TOPIC_NOT_REGISTER));
    }

    public static void addTopic(String topic) {
        topicMetrics.computeIfAbsent(topic, _topic -> new TopicMetrics(topic));
    }

    public static void deleteTopic(String topic) {
        if (topicMetrics.containsKey(topic)) {
            TopicMetrics metrics = topicMetrics.remove(topic);
            if (metrics != null) {
                metrics.shutDown();
            }
        }
    }

    public static void incRequestCounter(String topic, String type) {
        if (!started) return;

        allTopicMetric.getRequestCounter().inc(TOPIC_ALL, type);

        if (topicMetrics.containsKey(topic)) {
            topicMetrics.get(topic).getRequestCounter().inc(topic, type);
        } else {
            topicMetrics.get(TOPIC_NOT_REGISTER).getRequestCounter().inc(TOPIC_NOT_REGISTER, type);
        }
    }

    public static void incQPSCounter(String topic, String result) {
        if (!started) return;

        allTopicMetric.getQpsCounter().inc(TOPIC_ALL, result);
        if (topicMetrics.containsKey(topic)) {
            topicMetrics.get(topic).getQpsCounter().inc(topic, result);
        } else {
            topicMetrics.get(TOPIC_NOT_REGISTER).getQpsCounter().inc(TOPIC_NOT_REGISTER, result);
        }
    }

    public static void incBatchSendCounter(String topic, String result) {
        if (!started) return;

        allTopicMetric.getBatchSendRate().inc(TOPIC_ALL, result);
        if (topicMetrics.containsKey(topic)) {
            topicMetrics.get(topic).getBatchSendRate().inc(topic, result);
        } else {
            topicMetrics.get(TOPIC_NOT_REGISTER).getBatchSendRate().inc(TOPIC_NOT_REGISTER, result);
        }
    }

    public static void incRetryCounter(String topic) {
        if (!started) return;

        allTopicMetric.getRetryCounter().inc(TOPIC_ALL);
        if (topicMetrics.containsKey(topic)) {
            topicMetrics.get(topic).getRetryCounter().inc(topic);
        } else {
            topicMetrics.get(TOPIC_NOT_REGISTER).getRetryCounter().inc(TOPIC_NOT_REGISTER);
        }
    }

    public static void incLimitCounter(String topic) {
        if (!started) return;

        allTopicMetric.getLimitCounter().inc(TOPIC_ALL, RATE_LIMIT_TYPE_FAILED);
        if (topicMetrics.containsKey(topic)) {
            topicMetrics.get(topic).getLimitCounter().inc(topic, RATE_LIMIT_TYPE_FAILED);
        } else {
            topicMetrics.get(TOPIC_NOT_REGISTER).getLimitCounter().inc(TOPIC_NOT_REGISTER, RATE_LIMIT_TYPE_FAILED);
        }
    }

    public static void incWarnLimitCounter(String topic) {
        if (!started) return;

        allTopicMetric.getLimitCounter().inc(TOPIC_ALL, RATE_LIMIT_TYPE_WARN);
        if (topicMetrics.containsKey(topic)) {
            topicMetrics.get(topic).getLimitCounter().inc(topic, RATE_LIMIT_TYPE_WARN);
        } else {
            topicMetrics.get(TOPIC_NOT_REGISTER).getLimitCounter().inc(TOPIC_NOT_REGISTER, RATE_LIMIT_TYPE_WARN);
        }
    }

    public static void incDropCounter(String topic, String reason) {
        if (!started) return;

        allTopicMetric.getDropCounter().inc(TOPIC_ALL, reason);
        if (topicMetrics.containsKey(topic)) {
            topicMetrics.get(topic).getDropCounter().inc(topic, reason);
        } else {
            topicMetrics.get(TOPIC_NOT_REGISTER).getDropCounter().inc(TOPIC_NOT_REGISTER, reason);
        }
    }

    public static void putSendLatency(String topic, long latency) {
        if (!started) return;

        allTopicMetric.getSendLatencyPercent().put(latency, TOPIC_ALL);
        if (topicMetrics.containsKey(topic)) {
            topicMetrics.get(topic).getSendLatencyPercent().put(latency, topic);
        } else {
            topicMetrics.get(TOPIC_NOT_REGISTER).getSendLatencyPercent().put(latency, TOPIC_NOT_REGISTER);
        }
    }
}