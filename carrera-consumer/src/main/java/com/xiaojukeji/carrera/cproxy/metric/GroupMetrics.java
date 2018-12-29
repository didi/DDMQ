package com.xiaojukeji.carrera.cproxy.metric;

import com.xiaojukeji.carrera.metric.CounterMetric;
import com.xiaojukeji.carrera.metric.MetricFactory;
import com.xiaojukeji.carrera.metric.PercentileMetric;
import com.xiaojukeji.carrera.metric.RateMetric;
import com.xiaojukeji.carrera.cproxy.utils.LogUtils;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;


public class GroupMetrics {
    private static final int REPORT_INTERVAL_S = 10;
    private static final int OFFSET_REPORT_INTERVAL_S = 60;
    protected static final String TAG_ALL = "ALL";

    protected RateMetric qpsRate;
    protected CounterMetric limiterCounter;
    protected RateMetric pullStatRate;
    protected RateMetric pushStatRate;
    protected RateMetric msgFilterRate;
    protected CounterMetric retryMax;
    protected CounterMetric offsetCounter;
    protected CounterMetric offsetLagCounter;
    protected PercentileMetric consumeLatencyPercent;

    public GroupMetrics() {
        qpsRate = MetricFactory.getRateMetric("message.out.per.sec", REPORT_INTERVAL_S, TimeUnit.SECONDS, LogUtils.METRIC_LOGGER, "group|topic", "type", "result");
        limiterCounter = MetricFactory.getRateMetric("limit.count", REPORT_INTERVAL_S, TimeUnit.SECONDS, LogUtils.METRIC_LOGGER, "group|topic");
        retryMax = MetricFactory.getCounterMetric("push.retry.max", REPORT_INTERVAL_S, TimeUnit.SECONDS, LogUtils.METRIC_LOGGER, "group|topic", "qid");
        pullStatRate = MetricFactory.getRateMetric("pull.stat.per.sec", REPORT_INTERVAL_S, TimeUnit.SECONDS, LogUtils.METRIC_LOGGER, "group|topic", "qid", "type");
        offsetCounter = MetricFactory.getCounterMetric("offset", OFFSET_REPORT_INTERVAL_S, TimeUnit.SECONDS, LogUtils.METRIC_LOGGER, "group|topic", "qid", "type");
        offsetLagCounter = MetricFactory.getCounterMetric("offset.lag", OFFSET_REPORT_INTERVAL_S, TimeUnit.SECONDS, LogUtils.METRIC_LOGGER, "group|topic", "qid", "type");
        consumeLatencyPercent = MetricFactory.getPercentileMetric("consume.latency.ms", Arrays.asList(50, 75, 95, 99, 100), REPORT_INTERVAL_S, TimeUnit.SECONDS, LogUtils.METRIC_LOGGER, "group|topic");
        pushStatRate = MetricFactory.getRateMetric("push.stat.per.sec", REPORT_INTERVAL_S, TimeUnit.SECONDS, LogUtils.METRIC_LOGGER, "group|topic", "result", "httpCode", "errno");
        msgFilterRate = MetricFactory.getRateMetric("msg.filter.per.sec", REPORT_INTERVAL_S, TimeUnit.SECONDS, LogUtils.METRIC_LOGGER, "group|topic", "type", "state");
    }

    public void shutDown() {
        qpsRate.shutDown();
        limiterCounter.shutDown();
        pullStatRate.shutDown();
        pushStatRate.shutDown();
        msgFilterRate.shutDown();
        retryMax.shutDown();
        offsetCounter.shutDown();
        offsetLagCounter.shutDown();
        consumeLatencyPercent.shutDown();
    }

    protected String getGroupTopic(String group, String topic) {
        return group + "|" + topic;
    }

    protected String getGroupTopicAll(String group) {
        return group + "|ALL";
    }

    protected String getGroupAllTopicAll() {
        return "ALL|ALL";
    }
}