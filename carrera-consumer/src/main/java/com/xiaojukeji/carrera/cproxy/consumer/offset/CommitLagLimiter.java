package com.xiaojukeji.carrera.cproxy.consumer.offset;

import com.xiaojukeji.carrera.cproxy.consumer.ConsumeContext;
import org.apache.commons.collections4.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;


public class CommitLagLimiter {
    protected static final Logger LOGGER = LoggerFactory.getLogger(CommitLagLimiter.class);
    private final Map<String/*Topic*/, Long> maxCommitLagMap;
    private ReentrantLock commitLagLock;
    private Map<String, Condition> conditionMap;

    public CommitLagLimiter(Map<String, Long> maxCommitLagMap) {
        this.maxCommitLagMap = maxCommitLagMap;
        commitLagLock = new ReentrantLock();
        conditionMap = new HashMap<>();
    }

    public void acquire(ConsumeOffsetTracker tracker, String topic, ConsumeContext context) throws InterruptedException {
        long maxCommitLag = MapUtils.getLong(maxCommitLagMap, topic, -1L);
        if (maxCommitLag < 0) {
            return;
        }
        long lag = tracker.getCommitLag(topic, context);
        if (lag < maxCommitLag) {
            return;
        }

        commitLagLock.lock();
        try {
            while ((lag = tracker.getCommitLag(topic, context)) >= maxCommitLag) {
                LOGGER.warn("commit lag is over maxLag, block consuming...group={},topic={},qid={},lag={}",
                        context.getGroupId(), topic, context.getQid(), lag);
                getCondition(topic, context).await();
            }
        } finally {
            commitLagLock.unlock();
        }
    }

    public boolean tryAcquire(ConsumeOffsetTracker tracker, String topic, ConsumeContext context)  {
        long maxCommitLag = MapUtils.getLong(maxCommitLagMap, topic, -1L);
        if (maxCommitLag < 0) {
            return true;
        }
        long lag = tracker.getCommitLag(topic, context);

        return lag < maxCommitLag;
    }

    private Condition getCondition(String topic, ConsumeContext context) {
        String tqId = topic + "-" + context.getQid();
        return conditionMap.computeIfAbsent(tqId, id -> commitLagLock.newCondition());
    }

    public void release(ConsumeOffsetTracker tracker, String topic, ConsumeContext context) {
        long maxCommitLag = MapUtils.getLong(maxCommitLagMap, topic, -1L);
        if (maxCommitLag < 0) {
            return;
        }
        if (tracker.getCommitLag(topic, context) >= maxCommitLag) {
            return;
        }
        commitLagLock.lock();
        try {
            getCondition(topic, context).signal();
        } finally {
            commitLagLock.unlock();
        }
    }

    public void shutdown() {
        commitLagLock.lock();
        try {
            conditionMap.values().forEach(Condition::signalAll);
        } finally {
            commitLagLock.unlock();
        }
    }
}