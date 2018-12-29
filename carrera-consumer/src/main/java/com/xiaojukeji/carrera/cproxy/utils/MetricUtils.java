package com.xiaojukeji.carrera.cproxy.utils;

import com.xiaojukeji.carrera.metric.ErrorMetrics;
import com.xiaojukeji.carrera.cproxy.consumer.UpstreamJob;
import com.xiaojukeji.carrera.cproxy.metric.GroupMetrics;
import com.xiaojukeji.carrera.cproxy.metric.MutilGroupMetrics;
import com.xiaojukeji.carrera.cproxy.metric.SingleGroupMetrics;
import org.apache.http.HttpStatus;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;


public class MetricUtils {
    public static final String EffectiveMessage = "EffectiveMessage";
    public static final String IneffectiveMessage = "IneffectiveMessage";
    private static final String METRIC_REPORT_OPEN = "metric.report.open";
    private static final String GROUP_CLASS_ALL = "ALL|ALL";
    private static final String GROUP_CLASS_UNREGISTER = "unregistered";

    public enum ConsumeResult {
        INVALID, SUCCESS, FAILURE, EXCEPTION
    }

    public static ConcurrentHashMap<String/*group*/, ConcurrentHashMap<String/*topic*/, ConcurrentHashMap<String/*type*/, AtomicLong>>> monitorMetric = new ConcurrentHashMap<>();

    private static boolean started = ConfigUtils.getDefaultConfig(METRIC_REPORT_OPEN, true);
    private static ConcurrentHashMap<String, SingleGroupMetrics> groupMetrics = new ConcurrentHashMap<>();
    private static MutilGroupMetrics allGroupMetric = new MutilGroupMetrics(GROUP_CLASS_ALL);
    private static MutilGroupMetrics unRegisterGroupMetric = new MutilGroupMetrics(GROUP_CLASS_UNREGISTER);
    private static ErrorMetrics errorMetrics = new ErrorMetrics();


    public static void addGroup(String group) {
        groupMetrics.computeIfAbsent(group, _group -> new SingleGroupMetrics());
    }

    public static void deleteGroup(String group) {
        if (groupMetrics.containsKey(group)) {
            GroupMetrics metrics = groupMetrics.remove(group);
            if (metrics != null) {
                metrics.shutDown();
            }
        }
    }

    public static void incQpsCount(String group, String topic, String consumeType, String result, long n) {
        if (!started) return;

        allGroupMetric.incAllQpsCount(consumeType, result, n);
        if (groupMetrics.containsKey(group)) {
            groupMetrics.get(group).incQpsCount(group, topic, consumeType, result, n);
        } else {
            unRegisterGroupMetric.incAllQpsCount(consumeType, result, n);
        }
    }

    public static void incRateLimiterCount(String group, String topic) {
        if (!started) return;

        allGroupMetric.incAllLimiterCount();
        if (groupMetrics.containsKey(group)) {
            groupMetrics.get(group).incLimiterCount(group, topic);
        } else {
            unRegisterGroupMetric.incAllLimiterCount();
        }
    }

    public static void maxRetryCount(String group, String topic, String qid, long n) {
        if (!started) return;

        allGroupMetric.maxAllRetryCount(n);
        if (groupMetrics.containsKey(group)) {
            groupMetrics.get(group).maxRetryCount(group, topic, qid, n);
        } else {
            unRegisterGroupMetric.maxAllRetryCount(n);
        }
    }

    public static void incPullStatCount(String group, String topic, String qid, String type, long n) {
        if (!started) return;

        allGroupMetric.incAllPullStatCount(type, n);
        if (groupMetrics.containsKey(group)) {
            groupMetrics.get(group).incPullStatCount(group, topic, qid, type, n);
        } else {
            unRegisterGroupMetric.maxAllRetryCount(n);
        }
    }

    public static void incPullStatCount(String group, String topic, String qid, String type) {
        incPullStatCount(group, topic, qid, type, 1);
    }

    public static void incPushStatCount(String group, String topic, String result, String httpStatusCode, String responseErrno) {
        if (!started) return;

        allGroupMetric.incAllPushStatCount(result);
        if (groupMetrics.containsKey(group)) {
            groupMetrics.get(group).incPushStatCount(group, topic, result, httpStatusCode, responseErrno);
        } else {
            unRegisterGroupMetric.incAllPushStatCount(result);
        }
    }

    public static void maxOffsetCount(String group, String topic, String qid, String type, long cnt) {
        if (!started) return;

        allGroupMetric.maxAllOffsetCount(type, cnt);
        if (groupMetrics.containsKey(group)) {
            groupMetrics.get(group).maxOffsetCount(group, topic, qid, type, cnt);
        } else {
            unRegisterGroupMetric.maxAllOffsetCount(type, cnt);
        }
    }

    public static void maxOffsetLagCount(String group, String topic, String qid, String type, long cnt) {
        if (!started) return;

        allGroupMetric.maxAllOffsetLagCount(type, cnt);
        if (groupMetrics.containsKey(group)) {
            groupMetrics.get(group).maxOffsetLagCount(group, topic, qid, type, cnt);
        } else {
            unRegisterGroupMetric.maxAllOffsetLagCount(type, cnt);
        }
    }

    public static void incMsgFilterCount(String group, String topic, String type, String state) {
        if (!started) return;

        allGroupMetric.incAllMsgFilterCount(type, state);
        if (groupMetrics.containsKey(group)) {
            groupMetrics.get(group).incMsgFilterCount(group, topic, type, state);
        } else {
            unRegisterGroupMetric.incAllMsgFilterCount(type, state);
        }
    }

    public static void httpRequestLatencyMetric(UpstreamJob job, long cost) {
        if (!started) return;

        allGroupMetric.putAllConsumeLatency(cost);
        if (groupMetrics.containsKey(job.getGroupId())) {
            groupMetrics.get(job.getGroupId()).putConsumeLatency(job.getGroupId(), job.getTopic(), cost);
        } else {
            unRegisterGroupMetric.putAllConsumeLatency(cost);
        }
    }

    public static void pullAckLatencyMetric(UpstreamJob job, long cost) {
        if (!started) return;

        allGroupMetric.putAllConsumeLatency(cost);
        if (groupMetrics.containsKey(job.getGroupId())) {
            groupMetrics.get(job.getGroupId()).putConsumeLatency(job.getGroupId(), job.getTopic(), cost);
        } else {
            unRegisterGroupMetric.putAllConsumeLatency(cost);
        }
    }

    public static void httpRequestFailureMetric(UpstreamJob job, String httpStatusCode) {
        if (job.canDoErrorRetry()) {
            qpsAndFilterMetric(job, ConsumeResult.EXCEPTION);
            incPushStatCount(job.getGroupId(), job.getTopic(), "exception", httpStatusCode, null);
        } else {
            qpsAndFilterMetric(job, ConsumeResult.FAILURE);
            incPushStatCount(job.getGroupId(), job.getTopic(), "failure", httpStatusCode, null);
        }
    }

    public static void httpRequestSuccessMetric(UpstreamJob job, boolean processSuccess, String errno) {
        if (processSuccess) {
            qpsAndFilterMetric(job, ConsumeResult.SUCCESS);
            incPushStatCount(job.getGroupId(), job.getTopic(), "success",
                    Integer.toString(HttpStatus.SC_OK), errno);
        } else {
            qpsAndFilterMetric(job, ConsumeResult.FAILURE);
            incPushStatCount(job.getGroupId(), job.getTopic(), "failure",
                    Integer.toString(HttpStatus.SC_OK), errno);
        }
    }

    public static void qpsAndFilterMetric(UpstreamJob job, ConsumeResult consumeResult) {
        incQpsCount(job.getGroupId(), job.getTopic(), job.getConsumeType(), consumeResult.toString(), 1);
        incMsgFilterCount(job.getGroupId(), job.getTopic(),
                consumeResult == ConsumeResult.INVALID ? IneffectiveMessage : EffectiveMessage, job.getState());
    }

    public static void put(String group, String topic, String type) {
        monitorMetric.computeIfAbsent(group, _group -> new ConcurrentHashMap<>())
                .computeIfAbsent(topic, _topic -> new ConcurrentHashMap<>())
                .computeIfAbsent(type, _type -> new AtomicLong(0))
                .incrementAndGet();
    }

    public static void clear() {
        monitorMetric.forEach((group, groupMap) ->
                groupMap.forEach((topic, topicMap) ->
                        topicMap.forEach((type, value) -> value.set(0))));
    }


    public static void incError(String tag) {
        if (!started) return;
        errorMetrics.incErrorCount(tag);
    }
}