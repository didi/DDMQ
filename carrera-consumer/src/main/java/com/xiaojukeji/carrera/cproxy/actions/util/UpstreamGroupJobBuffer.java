package com.xiaojukeji.carrera.cproxy.actions.util;

import com.xiaojukeji.carrera.thrift.consumer.ConsumeResult;
import com.xiaojukeji.carrera.thrift.consumer.Context;
import com.xiaojukeji.carrera.cproxy.consumer.UpstreamJob;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ScheduledExecutorService;


public class UpstreamGroupJobBuffer {

    public static final Logger LOGGER = LoggerFactory.getLogger(UpstreamGroupJobBuffer.class);

    private String groupId;

    private ConcurrentMap<String/*Topic*/, ConcurrentMap<String/*QID*/, UpstreamJobBuffer>> bufferIndex = new ConcurrentHashMap<>();

    private Queue<UpstreamJobBuffer> groupNonEmptyBufferQueue = new ConcurrentLinkedQueue<>();

    private Map<String/* topic */, Queue<UpstreamJobBuffer>> topicNonEmptyBufferQueueMap = new ConcurrentHashMap<>();

    private final ScheduledExecutorService scheduler;

    public UpstreamGroupJobBuffer(String groupId, ScheduledExecutorService scheduler) {
        this.groupId = groupId;
        this.scheduler = scheduler;
    }

    public void processResult(ConsumeResult result) {
        Context context = result.getContext();
        assert groupId.equals(context.getGroupId());

        ConcurrentMap<String, UpstreamJobBuffer> qidToJobBuffer = bufferIndex.get(context.getTopic());
        if (qidToJobBuffer == null) {
            LOGGER.warn("topic not exists. context={}", context);
            return;
        }
        UpstreamJobBuffer jobBuffer = qidToJobBuffer.get(context.getQid());
        if (jobBuffer == null) {
            LOGGER.warn("qid not exists. context={}", context);
            return;
        }
        List<Long> nonExistsOffset = jobBuffer.processResult(result);
        if (CollectionUtils.isNotEmpty(nonExistsOffset)) {
            LOGGER.warn("offset not exists. context={}, nonExistsOffsets: {}",
                    context, nonExistsOffset);
        }
    }

    public List<UpstreamJob> pull(Context context, int maxBatchSize) {
        UpstreamJobBuffer buffer = pickNonEmptyBuffer(context.getTopic());
        if (buffer == null) {
            return Collections.emptyList();
        }
        context.setTopic(buffer.getTopic());
        context.setQid(buffer.getQid());
        return buffer.poll(maxBatchSize);
    }

    private UpstreamJobBuffer pickNonEmptyBuffer(String topic) {
        UpstreamJobBuffer buffer = null;
        Queue<UpstreamJobBuffer> bufferQueue;
        if (topic == null) {
            bufferQueue = groupNonEmptyBufferQueue;
        } else {
            bufferQueue = topicNonEmptyBufferQueueMap.get(topic);
        }

        if (bufferQueue == null) {
            return null;
        }

        while (buffer == null || buffer.isEmpty()) {
            buffer = bufferQueue.poll();
            if (buffer != null) {
                buffer.markDequeue(topic == null);
                LOGGER.trace("pick buffer={}, groupId={}", buffer, groupId);
            } else {
                LOGGER.trace("groupNonEmptyBufferQueue is empty, groupId={}", groupId);
                break;
            }
        }
        return buffer;
    }

    public void offer(UpstreamJob job) {
        ConcurrentMap<String/* qid */, UpstreamJobBuffer> topicMap = bufferIndex.computeIfAbsent(job.getTopic(), topic -> new ConcurrentHashMap<>());
        assert topicMap != null;

        UpstreamJobBuffer jobBuffer = topicMap.computeIfAbsent(job.getQid(), qid -> {
            Queue<UpstreamJobBuffer> topicNonEmptyBufferQueue = topicNonEmptyBufferQueueMap.computeIfAbsent(job.getTopic(), topic -> new ConcurrentLinkedQueue<>());
            return new UpstreamJobBuffer(job.getGroupId(), job.getTopic(), job.getQid(), groupNonEmptyBufferQueue, topicNonEmptyBufferQueue, scheduler);
        });

        assert jobBuffer != null;
        jobBuffer.offer(job);
    }

    public void recoverTimeoutMessage() {
        try {
            bufferIndex.values().forEach(topicMap -> topicMap.values().forEach(UpstreamJobBuffer::recoverTimeoutMessage));
        } catch (Exception e) {
            LOGGER.error("exception in recoverTimeoutMessage. group=" + groupId, e);
        }
    }

    public void clearTerminatedJobs() {
        bufferIndex.values().forEach(topicMap -> topicMap.values().forEach(UpstreamJobBuffer::clearTerminatedJobs));
    }
}