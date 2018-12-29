package com.xiaojukeji.carrera.monitor.lag.offset;

import com.xiaojukeji.carrera.utils.TimeUtils;
import org.apache.commons.collections4.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;


public class ConsumeOffsetTracker {
    private static final int MAX_RECORD_SIZE = 30;
    private String group;
    private String mqserver;
    private String topic;
    private ConcurrentMap<String, List<Long>> committedOffsetRecord = new ConcurrentHashMap<>();
    private ConcurrentMap<String, List<Long>> committedLagRecord = new ConcurrentHashMap<>();

    private long startTs;

    public ConsumeOffsetTracker(String group, String mqserver, String topic) {
        this.group = group;
        this.mqserver = mqserver;
        this.topic = topic;
        startTs = TimeUtils.getCurTime();
    }

    public void mark(String qid, Long committedOffset, Long committedLag) {
        List<Long> offsets = committedOffsetRecord.computeIfAbsent(qid, r -> new CopyOnWriteArrayList());
        offsets.add(committedOffset);
        if (offsets.size() > MAX_RECORD_SIZE) {
            offsets.remove(0);
        }

        List<Long> lags = committedLagRecord.computeIfAbsent(qid, r -> new CopyOnWriteArrayList());
        lags.add(committedLag);
        if (lags.size() > MAX_RECORD_SIZE) {
            lags.remove(0);
        }
    }

    public List<Long> getRecentCommittedOffset(String qid, int size) {
        List<Long> offsets = committedOffsetRecord.get(qid);
        if (CollectionUtils.isEmpty(offsets)) return Collections.EMPTY_LIST;
        return offsets.subList(offsets.size() > size ? offsets.size() - size : 0, offsets.size());
    }

    public List<Long> getRecentCommittedLag(String qid, int size) {
        List<Long> lags = committedLagRecord.get(qid);
        if (CollectionUtils.isEmpty(lags)) return Collections.EMPTY_LIST;
        return lags.subList(lags.size() > size ? lags.size() - size : 0, lags.size());
    }

    public long getConsumeTime() {
        return TimeUtils.getElapseTime(startTs);
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    @Override
    public String toString() {
        return "ConsumeOffsetTracker{" +
                ", group='" + group + '\'' +
                ", mqserver='" + mqserver + '\'' +
                ", topic='" + topic + '\'' +
                ", committedOffsetRecord=" + committedOffsetRecord +
                ", committedLagRecord=" + committedLagRecord +
                '}';
    }
}
