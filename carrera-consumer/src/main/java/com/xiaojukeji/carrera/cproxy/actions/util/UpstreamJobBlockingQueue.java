package com.xiaojukeji.carrera.cproxy.actions.util;

import com.google.common.collect.Sets;
import com.xiaojukeji.carrera.config.v4.cproxy.UpstreamTopic;
import com.xiaojukeji.carrera.cproxy.consumer.limiter.LimiterMgr;
import com.xiaojukeji.carrera.cproxy.config.ConsumerGroupConfig;
import com.xiaojukeji.carrera.cproxy.consumer.UpstreamJob;
import com.xiaojukeji.carrera.cproxy.utils.ConfigUtils;
import com.xiaojukeji.carrera.cproxy.utils.LogUtils;
import org.apache.commons.lang3.RandomUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;


public class UpstreamJobBlockingQueue {
    private static final Logger LOGGER = LoggerFactory.getLogger(UpstreamJobBlockingQueue.class);

    private static final int LOCK_NUMBER_BITS = 7;
    private static final int LOCK_NUMBER_MASK = (1 << LOCK_NUMBER_BITS) - 1;

    private volatile ConsumerGroupConfig consumerGroupConfig;

    private volatile Queue<UpstreamJob> mainQueue = new ConcurrentLinkedQueue<>(); // save submit jobs.
    private ReentrantLock mainQueueLock = new ReentrantLock();
    private final Queue<UpstreamJob> reActivationQueue = new ConcurrentLinkedQueue<>(); //排队链表的表头, 且还未被执行, 每次poll优先取这个队列.
    private final Map<Integer /*orderId*/, UpstreamJob /*tail*/> jobOrderMap = new ConcurrentHashMap<>();
    private final ReentrantLock[] orderLocks; //orderId的分段锁.

    /**
     * 只用于日志, 用于追查shutdown失败的问题.
     */
    private final Set<UpstreamJob> workingJobs = Sets.newConcurrentHashSet();
    private final Semaphore readyJobs;
    private final AtomicInteger jobSize;

    public UpstreamJobBlockingQueue(ConsumerGroupConfig consumerGroupConfig) {
        this.jobSize = new AtomicInteger(0);
        readyJobs = new Semaphore(0);
        this.consumerGroupConfig = consumerGroupConfig;

        orderLocks = new ReentrantLock[1 << LOCK_NUMBER_BITS];
        for (int i = 0; i < orderLocks.length; i++) {
            orderLocks[i] = new ReentrantLock();
        }
    }

    /**
     * save to mainQueue.
     *
     * @param job
     */
    public void submit(UpstreamJob job) throws InterruptedException {
        job.setState("Async.InMainQueue");
        jobSize.incrementAndGet();
        mainQueue.add(job);
        readyJobs.release();
    }

    public UpstreamJob poll() throws InterruptedException {
        while (true) {
            readyJobs.acquire();
            UpstreamJob job = fetchJob();
            if (job != null) {
                return job;
            }
        }
    }

    public UpstreamJob pollWithoutBlock() {
        while(true) {
            if (!readyJobs.tryAcquire()) {
                return null;
            }
            UpstreamJob job = fetchJob();
            if (job != null) {
                return job;
            }
        }
    }

    /**
     * 从队列中获取一个job.
     * 优先从reActivationQueue取,
     * 如果没有, 则从mainQueue中取, 取出来的job如果需要排队(orderId != null)则放入相应的排队队列中, 重新取.
     *
     * @return maybe null indicate that job is not ready
     */
    private UpstreamJob fetchJob() {
        UpstreamJob job = reActivationQueue.poll();
        if (job != null) {
            putInWorkingQueue(job);
            return job;
        }
        ReentrantLock orderLock;
        Integer orderId;
        mainQueueLock.lock();
        try {
            job = mainQueue.poll();
            orderId = job.getOrderId();

            if (orderId == null) {
                if (LOGGER.isTraceEnabled()) {
                    LOGGER.trace("job is out of mainQueue: job={}, no orderId", job.info());
                }
                putInWorkingQueue(job);
                return job;
            }

            orderLock = getLocks(orderId);
            orderLock.lock();
        } finally {
            mainQueueLock.unlock();
        }

        try {
            UpstreamJob dependentJob = jobOrderMap.putIfAbsent(orderId, job);
            if (dependentJob == null) {
                if (LOGGER.isTraceEnabled()) {
                    LOGGER.trace("job is out of mainQueue: job={}, no dependent job, orderId={}", job.info(), orderId);
                }
                putInWorkingQueue(job);
                return job;
            }

            assert dependentJob.getNext() == null;
            dependentJob.setNextIfNull(job);

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("job is out of mainQueue: job={}, enter jobOrderMap, orderId={}, dependent job={}", job.info(), orderId, dependentJob.info());
            }
            jobOrderMap.put(orderId, job);
        } finally {
            orderLock.unlock();
        }

        return null;
    }

    private void putInWorkingQueue(UpstreamJob job) {
        workingJobs.add(job);
    }

    private boolean removeWorkingQueue(UpstreamJob job) {
        return workingJobs.remove(job);
    }

    private ReentrantLock getLocks(Integer orderId) {
        return orderId == null ? null : orderLocks[orderId & LOCK_NUMBER_MASK];
    }

    /** two things:
     *      1. put job.next reActivationQueue
     *      2. update JobSize.
     * @param job
     */
    public void onJobFinished(UpstreamJob job) {
        if (job == null) {
            LogUtils.logErrorInfo("JobFinished_error","onJobFinished error: job is null");
            return;
        }
        try {
            if (!removeWorkingQueue(job)) {
                LogUtils.logErrorInfo("remove_job_from_workingJobs","remove job from workingJobs failed, job={}", job.info());
            }
            Integer orderId = job.getOrderId();
            if (orderId != null) {
                ReentrantLock orderLock = getLocks(orderId);
                orderLock.lock();
                try {
                    UpstreamJob nextJob = job.getNext();
                    if (nextJob == null) {
                        if (!jobOrderMap.remove(orderId, job)) {
                            UpstreamJob newJob = jobOrderMap.get(orderId);
                            LogUtils.logErrorInfo("remove_failed","onJobFinished: remove failed.job={}, orderId={}, size={}, jobOrderMap[orderId]={}",
                                    job.info(), orderId, getSize(), newJob == null ? null : newJob.info());
                        } else {
                            if (LOGGER.isTraceEnabled()) {
                                LOGGER.trace("onJobFinished: no next.job={}, jobOrderMap[{}]={}, size={}.",
                                        job.info(), orderId, jobOrderMap.get(orderId), getSize());
                            }
                        }
                    } else {
                        if (LOGGER.isTraceEnabled()) {
                            LOGGER.trace("onJobFinished: job={}, orderId={}, size={}, has next:{}",
                                    job.info(), orderId, getSize(), nextJob.info());
                        }
                        nextJob.setState("Async.InReActivationQueue");
                        reActivationQueue.offer(nextJob);
                        readyJobs.release();
                    }
                } finally {
                    orderLock.unlock();
                }
            } else {
                if (LOGGER.isTraceEnabled()) {
                    LOGGER.trace("onJobFinished: job={}, size={}, no orderId.", job.info(), getSize());
                }
            }
        } finally {
            jobSize.decrementAndGet();
        }
    }

    public void processNextMessage() {
        for(UpstreamJob job = pollWithoutBlock(); job != null; job = pollWithoutBlock()) {
            // for DelayRequest
            if (consumerGroupConfig.getDelayRequestHandlerThreads() > 0) {
                job.setWorkerId(RandomUtils.nextInt(0, consumerGroupConfig.getDelayRequestHandlerThreads() * 2));
            }
            job.execute();
        }
    }

    public int getSize() {
        return jobSize.get();
    }

    public int getReadyJobNumber() {
        return readyJobs.availablePermits();
    }

    public int getWorkingJobNumber() {
        return workingJobs.size();
    }

    public String info() {
        StringBuilder sb = new StringBuilder(2048);
        sb.append("queue.size=").append(getSize());
        sb.append("readyJobs.size=").append(getReadyJobNumber());
        for (UpstreamTopic upstreamTopic : consumerGroupConfig.getGroupConfig().getTopics()) {
            String topic = upstreamTopic.getTopic();
            int processingJobs = LimiterMgr.getInstance().availablePermits(
                    ConfigUtils.genGroupBrokerCluster(consumerGroupConfig.getGroup(), upstreamTopic.getBrokerCluster()), topic);
            int unfinishedJobs = upstreamTopic.getConcurrency() - processingJobs;
            if (unfinishedJobs > 0) {
                sb.append(", topic[").append(topic).append("].unfinished:").append(unfinishedJobs);
            }
        }
        sb.append("; mainQueue:size=").append(mainQueue.size());
        if (mainQueue.size() > 0) {
            UpstreamJob job = mainQueue.peek();
            sb.append(",peek=").append(job == null ? null : job.getMsgKey());
        }
        sb.append("; reActivationQueue:size=").append(reActivationQueue.size());
        if (reActivationQueue.size() > 0) {
            UpstreamJob job = reActivationQueue.peek();
            sb.append(",peek=").append(job == null ? null : job.getMsgKey());
        }
        sb.append("; jobOrderMap:size=").append(jobOrderMap.size());
        if (jobOrderMap.size() > 0) {
            try {
                sb.append(",oid->msgKey:");
                jobOrderMap.forEach((k, v) -> sb.append(k).append("=").append(v.getMsgKey()).append(","));
            } catch (Exception e) {
                LogUtils.logErrorInfo("info_error", "get info of jobOrderMap failed", e);
            }
        }

        sb.append("; workingJobs:size=").append(workingJobs.size());
        if (workingJobs.size() > 0) {
            try {
                sb.append(",keys:");
                workingJobs.forEach(job -> sb.append(job.getMsgKey()).append(","));
            } catch (Exception e) {
                LogUtils.logErrorInfo("info_error", "get info of workingJobs failed", e);
            }
        }
        return sb.toString();
    }
}