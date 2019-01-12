package com.xiaojukeji.chronos.metrics;

import com.xiaojukeji.carrera.metric.MetricFactory;
import com.xiaojukeji.carrera.metric.PercentileMetric;
import com.xiaojukeji.carrera.metric.RateMetric;
import com.xiaojukeji.chronos.utils.LogUtils;
import org.slf4j.Logger;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;


public class MetricService {
    private static Logger LOGGER = LogUtils.METRIC_LOGGER;

    private static final int REPORT_INTERVAL_IN_SECOND = 10;

    private static RateMetric pullQps;

    private static RateMetric writeQps;
    private static RateMetric writeQpsAfterSplit;

    private static RateMetric pushQps;
    private static RateMetric pushQpsBeforeMerge;

    private static PercentileMetric pushLatencyPercent;
    private static PercentileMetric seekLatencyPercent;
    private static PercentileMetric msgSizePercent;

    public static boolean init() {
        try {
            pullQps = MetricFactory.getRateMetric("chronos.pull.qps", REPORT_INTERVAL_IN_SECOND, TimeUnit.SECONDS, LOGGER, "topic", "action", "type", "result"); // ok

            writeQps = MetricFactory.getRateMetric("chronos.write.qps", REPORT_INTERVAL_IN_SECOND, TimeUnit.SECONDS, LOGGER, "topic", "action", "type", "to", "result"); // ok
            writeQpsAfterSplit = MetricFactory.getRateMetric("chronos.write.split.qps", REPORT_INTERVAL_IN_SECOND, TimeUnit.SECONDS, LOGGER, "topic", "action", "type");

            pushQps = MetricFactory.getRateMetric("chronos.push.qps", REPORT_INTERVAL_IN_SECOND, TimeUnit.SECONDS, LOGGER, "topic", "type", "from", "result");
            pushQpsBeforeMerge = MetricFactory.getRateMetric("chronos.push.merge.qps", REPORT_INTERVAL_IN_SECOND, TimeUnit.SECONDS, LOGGER, "topic");

            pushLatencyPercent = MetricFactory.getPercentileMetric("chronos.push.latency.us", Arrays.asList(50, 75, 95, 99, 100), REPORT_INTERVAL_IN_SECOND, TimeUnit.SECONDS, LOGGER, "topic");
            seekLatencyPercent = MetricFactory.getPercentileMetric("chronos.seek.latency.sec", Arrays.asList(50, 75, 95, 99, 100), REPORT_INTERVAL_IN_SECOND, TimeUnit.SECONDS, LOGGER, "role");
            msgSizePercent = MetricFactory.getPercentileMetric("chronos.msg.size", Arrays.asList(50, 75, 95, 99, 100), REPORT_INTERVAL_IN_SECOND, TimeUnit.SECONDS, LOGGER, "topic");

            return true;
        } catch (Throwable e) {
            LOGGER.error("metric error", e);
            return false;
        }
    }

    /**
     * 从cproxy拉取消息的qps
     * @param topic     真实的topic
     * @param msgAction    添加或者取消
     * @param msgType   消息类型
     * @param result    合法或者不合法
     */
    public static void incPullQps(String topic, MetricMsgAction msgAction, MetricMsgType msgType, MetricPullMsgResult result) {
        pullQps.inc(topic, msgAction.getValue(), msgType.getValue(), result.getValue());
    }

    public static void incPullQps(String topic, MetricMsgAction msgAction, MetricMsgType msgType) {
        incPullQps(topic, msgAction, msgType, MetricPullMsgResult.VALID);
    }

    public static void incWriteQps(String topic, MetricMsgAction msgAction, MetricMsgType msgType, MetricMsgToOrFrom to, MetricWriteMsgResult result) {
        writeQps.inc(topic, msgAction.getValue(), msgType.getValue(), to.getValue(), result.getValue());
    }

    public static void incWriteQps(String topic, MetricMsgAction msgAction, MetricMsgType msgType, MetricMsgToOrFrom to) {
        incWriteQps(topic, msgAction, msgType, to, MetricWriteMsgResult.OK);
    }

    public static void incWriteQpsAfterSplit(String topic, MetricMsgAction msgAction, MetricMsgType msgType) {
        writeQpsAfterSplit.inc(topic, msgAction.getValue(), msgType.getValue());
    }

    public static void incPushQps(String topic, MetricMsgType msgType, MetricMsgToOrFrom from, MetricPushMsgResult result) {
        pushQps.inc(topic, msgType.getValue(), from.getValue(), result.getValue());
    }

    public static void incPushQpsBeforeMerge(String topic) {
        pushQpsBeforeMerge.inc(topic);
    }

    public static void putPushLatency(String topic, long latency) {
        pushLatencyPercent.put(latency, topic);
    }

    public static void putSeekLatency(String role, long latency) {
        seekLatencyPercent.put(latency, role);
    }

    public static void putMsgSizePercent(String topic, long size) {
        msgSizePercent.put(size, topic);
    }
}