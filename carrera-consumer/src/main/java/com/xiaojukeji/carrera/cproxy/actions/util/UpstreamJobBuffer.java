package com.xiaojukeji.carrera.cproxy.actions.util;

import com.xiaojukeji.carrera.config.v4.cproxy.UpstreamTopic;
import com.xiaojukeji.carrera.thrift.consumer.ConsumeResult;
import com.xiaojukeji.carrera.cproxy.consumer.ConfigManager;
import com.xiaojukeji.carrera.cproxy.consumer.UpstreamJob;
import com.xiaojukeji.carrera.cproxy.utils.LogUtils;
import com.xiaojukeji.carrera.cproxy.utils.MetricUtils;
import com.xiaojukeji.carrera.cproxy.utils.TimeUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class UpstreamJobBuffer {

    private static final Logger LOGGER = LoggerFactory.getLogger(UpstreamJobBuffer.class);

    private static final Logger DROP_LOGGER = LogUtils.DROP_LOGGER;

    private Queue<UpstreamJob> queue = new ArrayDeque<>();

    private final LinkedHashMap<Long, UpstreamJob> workingJobs = new LinkedHashMap<>(1024, 0.75f, true);

    private final String groupId;
    private final String topic;
    private final String qid;
    private UpstreamTopic upstreamTopic;
    private boolean inGroupQueue = false;
    private boolean inTopicQueue = false;

    private final Queue<UpstreamJobBuffer> groupNonEmptyBufferQueue;
    private final Queue<UpstreamJobBuffer> topicNonEmptyBufferQueue;

    public String getTopic() {
        return topic;
    }

    public String getQid() {
        return qid;
    }

    private final ScheduledExecutorService scheduler;

    public UpstreamJobBuffer(String groupId, String topic, String qid,
                             Queue<UpstreamJobBuffer> groupNonEmptyBufferQueue,
                             Queue<UpstreamJobBuffer> topicNonEmptyBufferQueue,
                             ScheduledExecutorService scheduler) {
        this.groupId = groupId;
        this.topic = topic;
        this.qid = qid;
        this.groupNonEmptyBufferQueue = groupNonEmptyBufferQueue;
        this.topicNonEmptyBufferQueue = topicNonEmptyBufferQueue;
        this.scheduler = scheduler;
    }

    public synchronized void offer(UpstreamJob job) {
        if (!Objects.equals(job.getUpstreamTopic(), upstreamTopic)) {
            LOGGER.debug("upstream topic is updated! buffer={}", this);
            upstreamTopic = job.getUpstreamTopic();
        }
        if (CollectionUtils.isEmpty(queue)) {
            tryPutInNonEmptyQueue();
        }
        queue.offer(job);
    }

    private void tryPutInNonEmptyQueue() {
        if (!inGroupQueue) {
            LOGGER.debug("buffer in groupNonEmptyBufferQueue, buffer={}", this);
            inGroupQueue = true;
            groupNonEmptyBufferQueue.offer(this);
        }
        if (!inTopicQueue) {
            LOGGER.debug("buffer in topicNonEmptyBufferQueue, buffer={}", this);
            inTopicQueue = true;
            topicNonEmptyBufferQueue.offer(this);
        }
    }

    public synchronized List<UpstreamJob> poll(int maxBatchSize) {
        int maxSize = upstreamTopic.getMaxPullBatchSize();
        int maxRecvMsgsLength = ConfigManager.getInstance().getConsumeServerConfiguration().getMaxRecvMsgsLength();

        if (maxBatchSize > 0 && maxBatchSize < maxSize) {
            maxSize = maxBatchSize;
        }

        int msgsLength = 0;
        List<UpstreamJob> jobs = new ArrayList<>(maxSize);
        long curTime = TimeUtils.getCurTime();
        Iterator<Map.Entry<Long, UpstreamJob>> itr = workingJobs.entrySet().iterator();
        while (itr.hasNext()) {
            UpstreamJob job = itr.next().getValue();
            if (job.isTerminated()) {
                job.terminate();
                itr.remove();
                continue;
            }
            if (jobs.size() >= maxSize || msgsLength >= maxRecvMsgsLength)
                break;
            if (curTime - job.getPullTimestamp() < upstreamTopic.getTimeout())
                break;

            if (job.canDoErrorRetry()) {  //exception
                MetricUtils.qpsAndFilterMetric(job, MetricUtils.ConsumeResult.EXCEPTION);
                job.incErrorRetryCnt();
                job.setState("PullSvr.RePulled#" + job.getErrorRetryCnt());
                jobs.add(job);
                msgsLength += job.getCommonMessage().getValue().length;
            } else {
                itr.remove();
                dropJob(job); //failure
            }
        }

        for (UpstreamJob job : jobs) {
            //update timestamp and maintain the order in LinkedHashMap by calling 'get'
            workingJobs.get(job.getOffset()).setPullTimestamp(curTime);
        }

        while (jobs.size() < maxSize && msgsLength < maxRecvMsgsLength) {
            UpstreamJob job = queue.poll();
            if (job == null)
                break;
            if (job.isTerminated()) {
                job.terminate();
                continue;
            }
            job.setPullTimestamp(curTime);
            job.setState("PullSvr.Pulled");
            workingJobs.put(job.getOffset(), job);
            jobs.add(job);
            msgsLength += job.getCommonMessage().getValue().length;
        }
        if (CollectionUtils.isNotEmpty(jobs)) {
            tryPutInNonEmptyQueue();
        }
        return jobs;
    }

    private void dropJob(UpstreamJob job) {
        MetricUtils.qpsAndFilterMetric(job, MetricUtils.ConsumeResult.FAILURE);
        LOGGER.warn("drop Job:{},errRetry={},retryIdx={}", job, job.getErrorRetryCnt(), job.getRetryIdx());
        DROP_LOGGER.info("beyondErrorRetry,job={},errRetry={},retryIdx={}",
            job, job.getErrorRetryCnt(), job.getRetryIdx());
        job.onFinished(true); //do not sendBack to rmq.
    }

    public synchronized boolean isEmpty() {
        while (!queue.isEmpty()) {
            if (queue.peek().isTerminated()) {
                queue.poll().terminate();
            } else {
                break;
            }
        }
        if (!queue.isEmpty())
            return false;
        for (UpstreamJob job : workingJobs.values()) {
            return TimeUtils.getElapseTime(job.getPullTimestamp()) < upstreamTopic.getTimeout();
        }
        return true;
    }

    public synchronized List<Long> processResult(ConsumeResult result) {
        List<Long> nonExistsOffset = null;
        if (CollectionUtils.isNotEmpty(result.getSuccessOffsets())) {
            for (Long offset : result.getSuccessOffsets()) {
                UpstreamJob job = workingJobs.remove(offset);
                if (job != null) {
                    MetricUtils.qpsAndFilterMetric(job, MetricUtils.ConsumeResult.SUCCESS);
                    MetricUtils.pullAckLatencyMetric(job, TimeUtils.getElapseTime(job.getPullTimestamp()));
                    job.onFinished(true);  //success
                } else {
                    if (nonExistsOffset == null) {
                        nonExistsOffset = new ArrayList<>();
                    }
                    nonExistsOffset.add(offset);
                }
            }
        }
        if (CollectionUtils.isNotEmpty(result.getFailOffsets())) {
            for (Long offset : result.getFailOffsets()) {
                UpstreamJob job = workingJobs.remove(offset);
                if (job != null) {
                    MetricUtils.pullAckLatencyMetric(job, TimeUtils.getElapseTime(job.getPullTimestamp()));
                    int delay = job.nextRetryDelay();
                    if (delay >= 0) {
                        scheduler.schedule(() -> this.offer(job), delay, TimeUnit.MILLISECONDS);
                    } else {
                        dropJob(job);
                    }
                } else {
                    if (nonExistsOffset == null) {
                        nonExistsOffset = new ArrayList<>();
                    }
                    nonExistsOffset.add(offset);
                }
            }
        }
        return nonExistsOffset == null ? Collections.emptyList() : nonExistsOffset;
    }

    public synchronized void recoverTimeoutMessage() {
        if (MapUtils.isEmpty(workingJobs)) {
            return;
        }

        LOGGER.trace("recoverTimeoutMessage,group:{},topic:{},qid:{},workingJobs.size={}", groupId, topic, qid, workingJobs.size());

        long curTime = TimeUtils.getCurTime();
        Iterator<Map.Entry<Long, UpstreamJob>> itr = workingJobs.entrySet().iterator();
        while (itr.hasNext()) {
            UpstreamJob job = itr.next().getValue();
            if (curTime - job.getPullTimestamp() >= upstreamTopic.getTimeout()) {
                if (job.canDoErrorRetry()) {
                    tryPutInNonEmptyQueue();
                    job.setState("PullSvr.Timeout#" + job.getErrorRetryCnt());
                } else {
                    itr.remove();
                    dropJob(job);  //failure
                }
            } else {
                break;
            }
        }
    }

    @Override
    public String toString() {
        return "UpstreamJobBuffer{" +
            "queue.size=" + queue.size() +
            ", workingJobs.size=" + workingJobs.size() +
            ", groupId='" + groupId + '\'' +
            ", topic='" + topic + '\'' +
            ", qid='" + qid + '\'' +
            ", inGroupQueue=" + inGroupQueue +
            ", inTopicQueue=" + inTopicQueue +
            '}';
    }

    public synchronized void markDequeue(boolean groupDequque) {
        if (groupDequque) {
            if (!inGroupQueue) {
                LogUtils.logErrorInfo("buffer_not_in_queue", "error state, buffer is not in the queue, buffer={}", this);
            }
            inGroupQueue = false;
        } else {
            if (!inTopicQueue) {
                LogUtils.logErrorInfo("buffer_not_in_queue", "error state, buffer is not in the queue, buffer={}", this);
            }
            inTopicQueue = false;
        }
    }

    public synchronized void clearTerminatedJobs() {
        Iterator<Map.Entry<Long, UpstreamJob>> itr = workingJobs.entrySet().iterator();
        while (itr.hasNext()) {
            UpstreamJob job = itr.next().getValue();
            if (job.isTerminated()) {
                job.terminate();
                itr.remove();
            }
        }
        Queue<UpstreamJob> oldQueue = queue;
        queue = new ArrayDeque<>();
        oldQueue.forEach(job -> {
            if (!job.isTerminated()) {
                queue.add(job);
            }
        });
    }
}