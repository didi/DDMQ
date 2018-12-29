package com.xiaojukeji.carrera.monitor.lag.offset;

import com.xiaojukeji.carrera.utils.TimeUtils;
import org.apache.commons.collections4.CollectionUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;



public class ProduceOffsetTracker {
    private String mqserver;
    private String topic;
    private Map<String/* qid */, List<Offset>> offsetMap = new ConcurrentHashMap();

    ProduceOffsetTracker(String mqserver, String topic) {
        this.mqserver = mqserver;
        this.topic = topic;
    }

    void mark(String qid, long maxOffset) {
        List<Offset> offsets = offsetMap.computeIfAbsent(qid, r -> new CopyOnWriteArrayList());

        if (offsets.size() == 0) {
            offsets.add(new Offset(TimeUtils.getCurTime(), maxOffset));
            return;
        }

        Offset offset = offsets.get(offsets.size() - 1);
        if (offset.getOffset() == maxOffset) {
            return;
        }

        offsets.add(new Offset(TimeUtils.getCurTime(), maxOffset));

        if (offsets.size() > 30) {
            offsets.remove(0);
        }
    }

    public long getConsumeDelayTime(String qid, long offset) {
        List<Offset> offsetsRecord = offsetMap.get(qid);
        if (CollectionUtils.isEmpty(offsetsRecord)) return 0;

        if (offsetsRecord.size() == 1 || offsetsRecord.get(offsetsRecord.size() - 1).getOffset() <= offset) return 0;

        for (int i = offsetsRecord.size() - 2; i >= 0; i--) {
            if (offsetsRecord.get(i).getOffset() <= offset) {
                return TimeUtils.getElapseTime(offsetsRecord.get(i + 1).getProduceTs());
            }
        }

        return TimeUtils.getElapseTime(offsetsRecord.get(0).getProduceTs());
    }

    public Map<String, Long> getProduceOffset() {
        Map<String, Long> ret = new HashMap<>();
        offsetMap.forEach((qid, offsets) -> {
            if (offsets.size() == 0) {
                ret.put(qid, 0L);
            } else {
                ret.put(qid, offsets.get(offsets.size() - 1).getOffset());
            }
        });

        return ret;
    }

    @Override
    public String toString() {
        return "ProduceOffsetTracker{" +
                ", mqserver='" + mqserver + '\'' +
                ", topic='" + topic + '\'' +
                ", offsetMap=" + offsetMap +
                '}';
    }

    class Offset {
        private long produceTs;
        private long offset;

        public Offset(long produceTs, long offset) {
            this.produceTs = produceTs;
            this.offset = offset;
        }

        public long getProduceTs() {
            return produceTs;
        }

        public void setProduceTs(long produceTs) {
            this.produceTs = produceTs;
        }

        public long getOffset() {
            return offset;
        }

        public void setOffset(long offset) {
            this.offset = offset;
        }
    }
}
