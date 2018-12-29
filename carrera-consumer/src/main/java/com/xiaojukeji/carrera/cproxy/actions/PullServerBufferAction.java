package com.xiaojukeji.carrera.cproxy.actions;

import com.xiaojukeji.carrera.cproxy.config.ConsumerGroupConfig;
import com.xiaojukeji.carrera.cproxy.consumer.UpstreamJob;
import com.xiaojukeji.carrera.cproxy.actions.util.PullBuffer;
import com.xiaojukeji.carrera.cproxy.server.ConsumerServiceImpl;
import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;


public class PullServerBufferAction implements Action {
    public static final Logger LOGGER = getLogger(PullServerBufferAction.class);

    private final PullBuffer buffer;

    private final ConsumerGroupConfig config;

    public PullServerBufferAction(ConsumerGroupConfig config) {
        this.config = config;
        buffer = ConsumerServiceImpl.getInstance().register(config);
    }

    @Override
    public Status act(UpstreamJob job) {
        job.setState("PullSvr.InBuffer");
        buffer.offer(job);
        return Status.ASYNCHRONIZED;
    }

    @Override
    public void shutdown() {
        LOGGER.info("shutdown PullServerBufferAction for group={}", config.getGroupBrokerCluster());
        ConsumerServiceImpl.getInstance().unRegister(config);
    }
}