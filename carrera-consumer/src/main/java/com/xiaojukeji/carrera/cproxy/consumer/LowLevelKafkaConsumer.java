package com.xiaojukeji.carrera.cproxy.consumer;

import com.xiaojukeji.carrera.cproxy.consumer.handler.AsyncMessageHandler;
import com.xiaojukeji.carrera.cproxy.consumer.offset.ConsumeOffsetTracker;
import com.xiaojukeji.carrera.cproxy.consumer.ConsumeContext.MessageSource;
import com.xiaojukeji.carrera.cproxy.config.ConsumerGroupConfig;


public class LowLevelKafkaConsumer extends CarreraKafkaConsumer {

    public LowLevelKafkaConsumer(ConsumerGroupConfig config, AsyncMessageHandler handler) {
        super(config.getBrokerCluster(), config.getGroup(), config.getGroupConfig(),
                config.getcProxyConfig(), config.getcProxyConfig().getKafkaConfigs().get(config.getBrokerCluster()),
                handler, config.getMaxConsumeLagMap(), config.getTopicCount(), config.getTopicMap());
        commitLagLimiter = null;
    }

    @Override
    public void doStart() throws Exception {
        super.doStart();
        this.tracker = new LowLevelConsumeOffsetTracker(MessageSource.KAFKA, this);
    }

    public void setCommitOffset(String topic, int qid, long offset) {
        ConsumeContext context = new ConsumeContext(MessageSource.KAFKA, getGroupName());
        context.setPartitionId(qid);
        //修bug sdk offset已经+1 cproxy不需要加
        context.setOffset(offset - 1);
        this.tracker.trackFinish(topic, context);
    }


    private final static class LowLevelConsumeOffsetTracker extends ConsumeOffsetTracker {
        String groupId;

        public LowLevelConsumeOffsetTracker(MessageSource source, BaseCarreraConsumer consumer) {
            super(false, source, consumer);
            groupId = consumer.getGroupName();
        }

        @Override
        public void trackFinish(CommonMessage commonMessage, ConsumeContext context) {
        }
    }
}