package com.xiaojukeji.carrera.cproxy.metric;


public class MutilGroupMetrics extends GroupMetrics {
    private String groupClass;
    public MutilGroupMetrics(String groupClass) {
        super();
        this.groupClass = groupClass;
    }

    public void incAllQpsCount(String consumeType, String result, long n) {
        qpsRate.inc(n, groupClass, consumeType, result);
    }

    public void incAllLimiterCount() {
        limiterCounter.inc(groupClass);
    }

    public void maxAllRetryCount(long n) {
        retryMax.max(n, groupClass, TAG_ALL);
    }

    public void incAllPullStatCount(String type, long n) {
        pullStatRate.inc(n, groupClass, TAG_ALL, type);
    }

    public void maxAllOffsetCount(String type, long cnt) {
        offsetCounter.max(cnt, groupClass, TAG_ALL, type);
    }


    public void maxAllOffsetLagCount(String type, long cnt) {
        offsetCounter.max(cnt, groupClass, TAG_ALL, type);
    }

    public void putAllConsumeLatency(long latency) {
        consumeLatencyPercent.put(latency, groupClass);
    }

    public void incAllPushStatCount(String result) {
        pushStatRate.inc(groupClass, result, TAG_ALL, TAG_ALL);
        pushStatRate.inc(groupClass, TAG_ALL, TAG_ALL, TAG_ALL);
    }

    public void incAllMsgFilterCount(String type, String state) {
        msgFilterRate.inc(groupClass, type, state);
    }
}