package com.xiaojukeji.carrera.cproxy.consumer.offset;

import com.xiaojukeji.carrera.cproxy.consumer.BaseCarreraConsumer;
import com.xiaojukeji.carrera.cproxy.consumer.CommonMessage;
import com.xiaojukeji.carrera.cproxy.consumer.ConsumeContext;
import com.xiaojukeji.carrera.cproxy.utils.QidUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.rocketmq.common.message.MessageQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.xiaojukeji.carrera.cproxy.consumer.ConsumeContext.MessageSource.KAFKA;


public class ConsumeOffsetTracker {
    private static Logger LOGGER = LoggerFactory.getLogger(ConsumeOffsetTracker.class);

    private ConcurrentHashMap<String/*Topic*/, ConcurrentHashMap<Integer/*PartitionId*/, OffsetTracker>> kafkaTrackerMap;

    private ConcurrentHashMap<MessageQueue, OffsetTracker> rmqTrackerMap;

    private boolean async;

    private BaseCarreraConsumer consumer;

    public ConsumeOffsetTracker(boolean async, ConsumeContext.MessageSource source, BaseCarreraConsumer consumer) {
        this.async = async;
        if (source == KAFKA) {
            kafkaTrackerMap = new ConcurrentHashMap<>();
        } else {
            rmqTrackerMap = new ConcurrentHashMap<>();
        }
        this.consumer = consumer;
    }

    public void trackStart(CommonMessage commonMessage, ConsumeContext context) {
        OffsetTracker tracker = getTracker(commonMessage.getTopic(), context);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("trackStart, msg={},context={},tracker={}", commonMessage.info(), context.info(), tracker.hashCode());
        }
        if (!tracker.markStart(context.getOffset())) {
            LOGGER.warn("duplicate start offset; commonMessage={}, context={}", commonMessage.info(), context.info());
        }
    }

    public void trackFinish(CommonMessage commonMessage, ConsumeContext context) {
        OffsetTracker tracker = getTracker(commonMessage.getTopic(), context);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("trackFinish, msg={},context={},tracker={}", commonMessage.info(), context.info(), tracker.hashCode());
        }
        if (!tracker.markFinish(context.getOffset())) {
            LOGGER.warn("unknown finish offset; commonMessage={}, context={}", commonMessage.info(), context.info());
        }
    }

    public void trackFinish(String topic, ConsumeContext context) {
        OffsetTracker tracker = getTracker(topic, context);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("trackFinish, topic={},context={},tracker={}", topic, context.info(), tracker.hashCode());
        }
        if (!tracker.markFinish(context.getOffset())) {
            LOGGER.warn("unknown finish offset; topic={}, commit offset", topic, context.info());
        }
    }

    private OffsetTracker getTracker(String topic, ConsumeContext context) {
        if (context.getSource() == KAFKA) {
            return getKafkaTracker(topic, context.getPartitionId());
        } else {
            return getRmqTracker(context.getMessageQueue());
        }
    }


    private OffsetTracker getKafkaTracker(String topic, int partitionId) {
        ConcurrentHashMap<Integer, OffsetTracker> topicOffsetTracker = kafkaTrackerMap.get(topic);
        if (topicOffsetTracker == null) {
            topicOffsetTracker = new ConcurrentHashMap<>();
            ConcurrentHashMap<Integer, OffsetTracker> oldTracker = kafkaTrackerMap.putIfAbsent(topic, topicOffsetTracker);
            if (oldTracker != null) {
                topicOffsetTracker = oldTracker;
            }
        }
        OffsetTracker offsetTracker = topicOffsetTracker.get(partitionId);
        if (offsetTracker == null) {
            offsetTracker = new OffsetTracker(async);
            LOGGER.debug("new tracker={},  topic={}, partition={}", offsetTracker.hashCode(), topic, partitionId);
            OffsetTracker oldTracker = topicOffsetTracker.putIfAbsent(partitionId, offsetTracker);
            if (oldTracker != null) {
                offsetTracker = oldTracker;
            }
        }
        return offsetTracker;
    }

    private OffsetTracker getRmqTracker(MessageQueue mq) {
        OffsetTracker offsetTracker = rmqTrackerMap.get(mq);
        if (offsetTracker == null) {
            offsetTracker = new OffsetTracker(async);
            OffsetTracker oldTracker = rmqTrackerMap.putIfAbsent(mq, offsetTracker);
            if (oldTracker != null) {
                offsetTracker = oldTracker;
            }
        }
        return offsetTracker;
    }

    public List<OffsetTrackSnapshot> takeKafkaSnapshot(Map<String, Map<Integer, Long>> kafkaMaxOffsetMap) {
        List<OffsetTrackSnapshot> ret = new ArrayList<>();
        kafkaTrackerMap.forEach((topic, offsetMap) -> offsetMap.forEach((partition, tracker) -> {
            try {
                long finishOffset = tracker.getMaxFinish();
                long startOffset = tracker.getMaxStart();
                long maxCommitableOffset = tracker.getMaxCommittableFinish();
                String qid = QidUtils.kafkaMakeQid(consumer.getBrokerCluster(), partition);
                long maxOffset = MapUtils.getLong(kafkaMaxOffsetMap.get(topic), partition, -1L);
                long committedOffset = tracker.getCommittedOffset();
                ret.add(new OffsetTrackSnapshot(consumer.getGroupName(), consumer.getBrokerCluster(), topic, qid, maxOffset, startOffset, finishOffset, maxCommitableOffset, committedOffset));
            } catch (Exception e) {
                LOGGER.error(String.format("make snapshot failed!topic=%s,pid=%s, tracker=%s", topic, partition, tracker), e);
            }
        }));
        return ret;
    }

    public List<OffsetTrackSnapshot> takeRmqSnapshot(Map<MessageQueue, Long> rmqMaxOffsetMap) {
        List<OffsetTrackSnapshot> ret = new ArrayList<>();
        rmqTrackerMap.forEach((mq, tracker) -> {
            try {
                long finishOffset = tracker.getMaxFinish();
                long startOffset = tracker.getMaxStart();
                long maxCommitableOffset = tracker.getMaxCommittableFinish();
                String qid = QidUtils.rmqMakeQid(consumer.getBrokerCluster(), mq.getBrokerName(), mq.getQueueId());
                long maxOffset = MapUtils.getLong(rmqMaxOffsetMap, mq, -1L);
                long committedOffset = tracker.getCommittedOffset();
                ret.add(new OffsetTrackSnapshot(consumer.getGroupName(), consumer.getBrokerCluster(), mq.getTopic(), qid, maxOffset, startOffset, finishOffset, maxCommitableOffset, committedOffset));
            } catch (Exception e) {
                LOGGER.error(String.format("make snapshot failed!mq=%s,tracker=%s", mq, tracker), e);
            }
        });
        return ret;
    }

    public long getCommitLag(String topic, ConsumeContext context) {
        OffsetTracker tracker = getTracker(topic, context);
        return tracker.getMaxStart() - tracker.getMaxCommittableFinish();
    }

    public ConcurrentHashMap<String, ConcurrentHashMap<Integer, OffsetTracker>> getKafkaTrackerMap() {
        return kafkaTrackerMap;
    }

    public ConcurrentHashMap<MessageQueue, OffsetTracker> getRmqTrackerMap() {
        return rmqTrackerMap;
    }
}