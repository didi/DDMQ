package com.xiaojukeji.carrera.cproxy.metric;


public class SingleGroupMetrics extends GroupMetrics {
    public SingleGroupMetrics() {
        super();
    }

    public void incQpsCount(String group, String topic, String consumeType, String result, long n) {
        qpsRate.inc(n, getGroupTopic(group, topic), consumeType, result);
        qpsRate.inc(n, getGroupTopicAll(group), consumeType, result);
    }

    public void incLimiterCount(String group, String topic) {
        limiterCounter.inc(getGroupTopic(group, topic));
        limiterCounter.inc(getGroupTopicAll(group));
    }

    public void maxRetryCount(String group, String topic, String qid, long n) {
        retryMax.max(n, getGroupTopic(group, topic), qid);
        retryMax.max(n, getGroupTopic(group, topic), TAG_ALL);
        retryMax.max(n, getGroupTopicAll(group), TAG_ALL);
    }

    public void incPullStatCount(String group, String topic, String qid, String type, long n) {
        if (topic != null) {
            if (qid != null) {
                pullStatRate.inc(n, getGroupTopic(group, topic), qid, type);
            }
            pullStatRate.inc(n, getGroupTopic(group, topic), TAG_ALL, type);
        }
        pullStatRate.inc(n, getGroupTopicAll(group), TAG_ALL, type);
    }

    public void maxOffsetCount(String group, String topic, String qid, String type, long cnt) {
        offsetCounter.max(cnt, getGroupTopic(group, topic), qid, type);
        offsetCounter.max(cnt, getGroupTopic(group, topic), TAG_ALL, type);
        offsetCounter.max(cnt, getGroupTopicAll(group), TAG_ALL, type);
    }

    public void maxOffsetLagCount(String group, String topic, String qid, String type, long cnt) {
        offsetLagCounter.max(cnt, getGroupTopic(group, topic), qid, type);
        offsetLagCounter.max(cnt, getGroupTopic(group, topic), TAG_ALL, type);
        offsetLagCounter.max(cnt, getGroupTopicAll(group), TAG_ALL, type);
    }

    public void putConsumeLatency(String group, String topic, long latency) {
        consumeLatencyPercent.put(latency, getGroupTopic(group, topic));
        consumeLatencyPercent.put(latency, getGroupTopicAll(group));
    }

    public void incPushStatCount(String group, String topic, String result, String httpStatusCode, String responseErrno) {
        pushStatRate.inc(getGroupTopic(group, topic), result, httpStatusCode, responseErrno);
        pushStatRate.inc(getGroupTopicAll(group), result, httpStatusCode, responseErrno);
        pushStatRate.inc(getGroupAllTopicAll(), result, httpStatusCode, responseErrno);
        pushStatRate.inc(getGroupTopic(group, topic), result, TAG_ALL, TAG_ALL);
        pushStatRate.inc(getGroupTopicAll(group), result, TAG_ALL, TAG_ALL);
    }

    public void incMsgFilterCount(String group, String topic, String type, String state) {
        msgFilterRate.inc(getGroupTopic(group, topic), type, state);
        msgFilterRate.inc(getGroupTopicAll(group), type, state);
    }
}