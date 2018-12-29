package com.xiaojukeji.carrera.cproxy.consumer.limiter;

import com.xiaojukeji.carrera.config.v4.GroupConfig;
import com.xiaojukeji.carrera.config.v4.cproxy.UpstreamTopic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Semaphore;


public class CapacityLimiter {

    private static final Logger LOGGER = LoggerFactory.getLogger(CapacityLimiter.class);

    private String groupName;

    private ConcurrentMap<String, Semaphore> emptySlots = new ConcurrentHashMap<>();

    private GroupConfig groupConfig;

    public CapacityLimiter(GroupConfig groupConfig) {
        this.groupName = groupConfig.getGroup();
        this.groupConfig = groupConfig;

        for (UpstreamTopic upstreamTopic : groupConfig.getTopics()) {
            emptySlots.put(upstreamTopic.getTopic(), new Semaphore(upstreamTopic.getConcurrency()));
        }
    }

    /**
     * 会阻塞
     * @param topic
     * @param permits
     * @return
     * @throws InterruptedException
     */
    public void doBlockLimit(String topic, int permits) throws InterruptedException {
        if (emptySlots.get(topic) == null) {
            return;
        }
        emptySlots.get(topic).acquire(permits);
    }

    /**
     * 限量 非阻塞
     * @throws InterruptedException
     */
    public boolean doNonBlockLimit(String topic, int permits) {
        if (emptySlots.get(topic) == null) {
            return true;
        }

        if (!emptySlots.get(topic).tryAcquire(permits)) {
            LOGGER.trace("doNonBlockLimit sizeLimit is effective. group:{}.", groupName);
            return false;
        }
        return true;
    }

    public void release(String topic, int permits) {
        if (emptySlots.get(topic) == null) {
            return;
        }
        emptySlots.get(topic).release(permits);
    }

    public int availablePermits(String topic) {
        if (emptySlots.get(topic) == null) {
            return 0;
        }
        return emptySlots.get(topic).availablePermits();
    }

    public void close() {
        for (UpstreamTopic topic : groupConfig.getTopics()) {
            emptySlots.get(topic.getTopic()).release(topic.getConcurrency());
        }
    }
}