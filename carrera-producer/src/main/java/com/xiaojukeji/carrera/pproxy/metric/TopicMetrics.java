package com.xiaojukeji.carrera.pproxy.metric;

import com.xiaojukeji.carrera.metric.CounterMetric;
import com.xiaojukeji.carrera.metric.MetricFactory;
import com.xiaojukeji.carrera.metric.PercentileMetric;
import com.xiaojukeji.carrera.metric.RateMetric;
import com.xiaojukeji.carrera.pproxy.utils.ConfigUtils;
import com.xiaojukeji.carrera.pproxy.utils.LogUtils;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;


public class TopicMetrics {
    private static final int REPORT_INTERVAL_S = ConfigUtils.getDefaultConfig("com.xiaojukeji.carrera.config.metric.report.interval.seconds", 10);
    private RateMetric qpsRate;
    private RateMetric batchSendRate;
    private CounterMetric retryCounter;
    private CounterMetric limitCounter;
    private RateMetric requestRate;
    private CounterMetric dropCounter;
    private PercentileMetric sendLatencyPercent;

    public TopicMetrics(String topic) {
        qpsRate = MetricFactory.getRateMetric("message.in.per.sec", REPORT_INTERVAL_S, TimeUnit.SECONDS, LogUtils.getMetricLogger(), "topic", "result");
        batchSendRate = MetricFactory.getRateMetric("batch.send.per.sec", REPORT_INTERVAL_S, TimeUnit.SECONDS, LogUtils.getMetricLogger(), "topic", "result");
        retryCounter = MetricFactory.getCounterMetric("retry.count", REPORT_INTERVAL_S, TimeUnit.SECONDS, LogUtils.getMetricLogger(), "topic");
        limitCounter = MetricFactory.getCounterMetric("rate.limit.count", REPORT_INTERVAL_S, TimeUnit.SECONDS, LogUtils.getMetricLogger(), "topic", "type");
        requestRate = MetricFactory.getRateMetric("request.per.sec", REPORT_INTERVAL_S, TimeUnit.SECONDS, LogUtils.getMetricLogger(), "topic", "type");
        dropCounter = MetricFactory.getCounterMetric("drop.count", REPORT_INTERVAL_S, TimeUnit.SECONDS, LogUtils.getMetricLogger(), "topic", "reason");
        sendLatencyPercent = MetricFactory.getPercentileMetric("send.latency.us", Arrays.asList(50, 75, 95, 99, 100),
                REPORT_INTERVAL_S, TimeUnit.SECONDS, LogUtils.getMetricLogger(), "topic");
    }

    public RateMetric getQpsCounter() {
        return qpsRate;
    }

    public RateMetric getBatchSendRate() {
        return batchSendRate;
    }

    public CounterMetric getRetryCounter() {
        return retryCounter;
    }

    public CounterMetric getLimitCounter() {
        return limitCounter;
    }

    public RateMetric getRequestCounter() {
        return requestRate;
    }

    public CounterMetric getDropCounter() {
        return dropCounter;
    }

    public PercentileMetric getSendLatencyPercent() {
        return sendLatencyPercent;
    }

    public void shutDown() {
        qpsRate.shutDown();
        retryCounter.shutDown();
        limitCounter.shutDown();
        requestRate.shutDown();
        dropCounter.shutDown();
        sendLatencyPercent.shutDown();
    }
}