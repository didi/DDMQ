package com.xiaojukeji.carrera.cproxy.actions.util;

import com.xiaojukeji.carrera.thrift.consumer.ConsumeResult;
import com.xiaojukeji.carrera.thrift.consumer.Context;
import com.xiaojukeji.carrera.thrift.consumer.Message;
import com.xiaojukeji.carrera.thrift.consumer.PullRequest;
import com.xiaojukeji.carrera.cproxy.config.ConsumerGroupConfig;
import com.xiaojukeji.carrera.cproxy.consumer.UpstreamJob;
import com.xiaojukeji.carrera.cproxy.server.DelayRequest;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Collectors;


public class PullBuffer {

    public static final Logger LOGGER = LoggerFactory.getLogger(PullBuffer.class);

    private static final int MAX_WAIT_REQUEST_QUEUE_SIZE = 1000;

    private static String EMPTY_TOPIC = "#EMPTY_TOPIC#";

    private UpstreamGroupJobBuffer buffer;

    private final String group;

    private Map<String/* cluster */, ConsumerGroupConfig> clusterConfigs = new ConcurrentHashMap<>();

    private Map<String/* topic */, Deque<DelayRequest>> waitQueueMap = new ConcurrentHashMap<>();

    public PullBuffer(String group, ScheduledExecutorService scheduledExecutorService) {
        this.group = group;
        buffer = new UpstreamGroupJobBuffer(group, scheduledExecutorService);
    }

    public List<Message> pull(Context context, int maxBatchSize) {
        List<UpstreamJob> jobs = buffer.pull(context, maxBatchSize);
        if (CollectionUtils.isEmpty(jobs)) {
            return Collections.emptyList();
        } else {
            return jobs.stream().map(UpstreamJob::getPullMessage).collect(Collectors.toList());
        }
    }

    public void processResult(ConsumeResult result) {
        buffer.processResult(result);
    }

    public void offer(UpstreamJob job) {
        buffer.offer(job);

        if (!job.canProcessDelayRequest()) { // current thread do not process delay request.
            return;
        }

        String topic = job.getTopic();
        Deque<DelayRequest> waitQueue = waitQueueMap.get(topic);
        if (waitQueue == null) {
            waitQueue = waitQueueMap.get(EMPTY_TOPIC);
        }
        if (waitQueue == null) return;

        while (true) {
            DelayRequest request = waitQueue.poll();
            if (request == null) break;
            if (request.isFinished()) continue;
            synchronized (request) { // lock this request so that timeout-checker won't process it.
                if (request.isFinished()) continue;
                List<Message> messages = pull(request.getContext(), request.getRequest().getMaxBatchSize());
                if (CollectionUtils.isNotEmpty(messages)) {
                    request.response(messages);
                } else {
                    waitQueue.addFirst(request);
                }
            }
            break;
        }
    }

    public void recoverTimeoutMessage() {
        try {
            if (clusterConfigs.size() > 0) {
                buffer.recoverTimeoutMessage();
            }
        } catch (Exception e) {
            LOGGER.error("exception in recoverTimeoutMessage, group=" + group, e);
        }
    }

    public void cleanWaitQueue() {
        try {
            if (MapUtils.isNotEmpty(waitQueueMap)) {
                waitQueueMap.values().forEach(this::doCleanWaitQueue);
            }
        } catch (Exception e) {
            LOGGER.error("exception in cleanWaitQueue, group=" + group, e);
        }
    }

    public void doCleanWaitQueue(Deque<DelayRequest> waitQueue) {
        while (!waitQueue.isEmpty()) {
            DelayRequest r = waitQueue.peek();
            if (r == null) break;
            if (r.isExpired()) {
                r.timeout();
            }
            if (r.isFinished()) {
                r = waitQueue.poll();
                if (r != null && !r.isFinished()) {
                    waitQueue.addFirst(r);
                }
            } else {
                break;
            }
        }
    }

    public void addClusterConfig(ConsumerGroupConfig config) {
        clusterConfigs.put(config.getBrokerCluster(), config);
    }

    public void removeClusterConfig(ConsumerGroupConfig config) {
        clusterConfigs.remove(config.getBrokerCluster());
        buffer.clearTerminatedJobs();
    }

    public boolean addDelayRequest(DelayRequest delayRequest) {
        Deque<DelayRequest> waitQueue = waitQueueMap.computeIfAbsent(
                getRequestTopic(delayRequest.getRequest()), topic -> new LinkedBlockingDeque<>(MAX_WAIT_REQUEST_QUEUE_SIZE));

        if (!waitQueue.offer(delayRequest)) {
            doCleanWaitQueue(waitQueue);
            return waitQueue.offer(delayRequest);
        } else {
            return true;
        }
    }

    private String getRequestTopic(PullRequest request) {
        return request.getTopic() == null ? EMPTY_TOPIC : request.getTopic();
    }
}