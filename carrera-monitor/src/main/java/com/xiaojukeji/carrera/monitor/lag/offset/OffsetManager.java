package com.xiaojukeji.carrera.monitor.lag.offset;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;


public class OffsetManager {
    private ConcurrentMap<String/* cluster|mqserver|topic */, ProduceOffsetTracker> produceOffsetMap = new ConcurrentHashMap<>();
    private ConcurrentMap<String/* cluster|mqserver|group|topic */, ConsumeOffsetTracker> consumeOffsetMap = new ConcurrentHashMap<>();

    public void markProduce(String mqserver, String topic, String qid, long offset) {
        ProduceOffsetTracker tracker = produceOffsetMap.computeIfAbsent(buildClusterMqserverTopicKey(mqserver, topic),
                r -> new ProduceOffsetTracker(mqserver, topic));
        tracker.mark(qid, offset);
    }

    public void markConsume(String mqserver, String group, String topic, String qid, long offset, long lag) {
        ConsumeOffsetTracker tracker = consumeOffsetMap.computeIfAbsent(buildClusterMqserverGroupTopic(mqserver, group, topic),
                r -> new ConsumeOffsetTracker(group, mqserver, topic));
        tracker.mark(qid, offset, lag);
    }

    public long getConsumeDelay(String mqserver, String topic, String qid, long offset) {
        ProduceOffsetTracker tracker = produceOffsetMap.get(buildClusterMqserverTopicKey(mqserver, topic));
        if (tracker == null) return 0;
        return tracker.getConsumeDelayTime(qid, offset);
    }

    public long getConsumeTime(String mqserver, String group, String topic) {
        ConsumeOffsetTracker tracker = consumeOffsetMap.get(buildClusterMqserverGroupTopic(mqserver, group, topic));
        if (tracker == null) return 0;

        return tracker.getConsumeTime();
    }

    public List<Long> getConsumeOffsetRecord(String mqserver, String group, String topic, String qid, int size) {
        ConsumeOffsetTracker tracker = consumeOffsetMap.get(buildClusterMqserverGroupTopic(mqserver, group, topic));
        if (tracker == null) return Collections.emptyList();

        return tracker.getRecentCommittedOffset(qid, size);
    }

    public List<Long> getConsumeLagRecord(String mqserver, String group, String topic, String qid, int size) {
        ConsumeOffsetTracker tracker = consumeOffsetMap.get(buildClusterMqserverGroupTopic(mqserver, group, topic));
        if (tracker == null) return Collections.emptyList();

        return tracker.getRecentCommittedLag(qid, size);
    }

    public Map<String, Long> getProduceOffsetLatest(String mqserver, String topic) {
        ProduceOffsetTracker tracker = produceOffsetMap.get(buildClusterMqserverTopicKey(mqserver, topic));
        if (tracker == null) return Collections.EMPTY_MAP;

        return tracker.getProduceOffset();
    }

    private String buildClusterMqserverTopicKey(String mqserver, String topic) {
        return mqserver + "|" + topic;
    }

    private String buildClusterMqserverGroupTopic(String mqserver, String group, String topic) {
        return mqserver + "|" + group + "|" + topic;
    }
}
