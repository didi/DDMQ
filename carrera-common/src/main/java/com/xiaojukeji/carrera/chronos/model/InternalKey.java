package com.xiaojukeji.carrera.chronos.model;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.xiaojukeji.carrera.chronos.enums.MsgTypes;

import java.util.List;


public class InternalKey {
    private static final String SEPARATOR = "-";
    private static final int LEN_UUID = 36;
    private static final long ONE_DAY_SECONDS = 24 * 60 * 60;

    private long timestamp;
    private int type;
    private long expire;
    private long times;
    private long timed;
    private long interval;
    private int innerTopicSeq;
    private String uuid;
    private int segmentNum;
    private int segmentIndex;

    public InternalKey(String uniqDelayMsgId) {
        List<String> items = Splitter.on(SEPARATOR).limit(8).splitToList(uniqDelayMsgId);
        timestamp = Long.valueOf(items.get(0));
        type = Integer.valueOf(items.get(1));
        expire = Long.valueOf(items.get(2));
        times = Integer.valueOf(items.get(3));
        timed = Integer.valueOf(items.get(4));
        interval = Integer.valueOf(items.get(5));
        innerTopicSeq = Integer.valueOf(items.get(6));
        if (items.get(7).length() == LEN_UUID) {
            uuid = items.get(7);
        } else {
            List<String> subItems = Splitter.on(SEPARATOR).limit(7).splitToList(items.get(7));
            uuid = items.get(7).substring(0, LEN_UUID);
            segmentNum = Integer.valueOf(subItems.get(5));
            segmentIndex = Integer.valueOf(subItems.get(6));
        }
    }

    public InternalKey(long timestamp, int type, long expire, long times, long timed, long interval, int innerTopicSeq, String uuid) {
        this.timestamp = timestamp;
        this.type = type;
        this.expire = expire;
        this.times = times;
        this.timed = timed;
        this.interval = interval;
        this.innerTopicSeq = innerTopicSeq;
        this.uuid = uuid;
    }

    public InternalKey(InternalKey internalKey) {
        this.timestamp = internalKey.getTimestamp();
        this.type = internalKey.getType();
        this.expire = internalKey.getExpire();
        this.times = internalKey.getTimes();
        this.timed = internalKey.getTimed();
        this.interval = internalKey.getInterval();
        this.innerTopicSeq = internalKey.getInnerTopicSeq();
        this.uuid = internalKey.getUuid();
        this.segmentNum = internalKey.getSegmentNum();
        this.segmentIndex = internalKey.getSegmentIndex();
    }

    public InternalKey nextUniqDelayMsgId() {
        if (this.type == MsgTypes.LOOP_DELAY.getValue()) {
            this.timestamp += interval;
            this.timed++;
            return this;
        } else if (this.type == MsgTypes.LOOP_EXPONENT_DELAY.getValue()) {
            this.timed++;
            long diff = (long) Math.pow(this.interval, this.timed);
            if (diff > ONE_DAY_SECONDS || diff <= 0) {
                this.timestamp += ONE_DAY_SECONDS;
            } else {
                this.timestamp += diff;
            }
            return this;
        }

        return this;
    }

    public String genUniqDelayMsgId() {
        return Joiner.on(SEPARATOR).join(timestamp, type, expire, times, timed, interval, innerTopicSeq, uuid);
    }

    public String genUniqDelayMsgIdWithSegmentInfoIfHas() {
        if (segmentNum > 0) {
            return Joiner.on(SEPARATOR).join(timestamp, type, expire, times, timed, interval, innerTopicSeq, uuid, segmentNum, segmentIndex);
        }
        return genUniqDelayMsgId();
    }

    public InternalKey cloneTombstoneInternalKey() {
        InternalKey tombstoneInternalKey = new InternalKey(this);
        tombstoneInternalKey.setType(MsgTypes.TOMBSTONE.getValue());
        return tombstoneInternalKey;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public long getExpire() {
        return expire;
    }

    public void setExpire(long expire) {
        this.expire = expire;
    }

    public long getTimes() {
        return times;
    }

    public void setTimes(long times) {
        this.times = times;
    }

    public long getTimed() {
        return timed;
    }

    public void setTimed(long timed) {
        this.timed = timed;
    }

    public long getInterval() {
        return interval;
    }

    public void setInterval(long interval) {
        this.interval = interval;
    }

    public int getInnerTopicSeq() {
        return innerTopicSeq;
    }

    public void setInnerTopicSeq(int innerTopicSeq) {
        this.innerTopicSeq = innerTopicSeq;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public int getSegmentNum() {
        return segmentNum;
    }

    public void setSegmentNum(int segmentNum) {
        this.segmentNum = segmentNum;
    }

    public int getSegmentIndex() {
        return segmentIndex;
    }

    public void setSegmentIndex(int segmentIndex) {
        this.segmentIndex = segmentIndex;
    }

    @Override
    public String toString() {
        return "InternalKey{" +
                "timestamp=" + timestamp +
                ", type=" + type +
                ", expire=" + expire +
                ", times=" + times +
                ", timed=" + timed +
                ", interval=" + interval +
                ", innerTopicSeq=" + innerTopicSeq +
                ", uuid='" + uuid + '\'' +
                ", segmentNum=" + segmentNum +
                ", segmentIndex=" + segmentIndex +
                '}';
    }
}