package com.xiaojukeji.carrera.cproxy.consumer;

import com.xiaojukeji.carrera.config.v4.CProxyConfig;
import com.xiaojukeji.carrera.config.v4.GroupConfig;
import com.xiaojukeji.carrera.config.v4.cproxy.RocketmqConfiguration;
import com.xiaojukeji.carrera.cproxy.consumer.handler.AsyncMessageHandler;
import com.xiaojukeji.carrera.cproxy.consumer.limiter.LimiterMgr;
import com.xiaojukeji.carrera.cproxy.utils.CommonUtils;
import com.xiaojukeji.carrera.cproxy.utils.QidUtils;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeOrderlyStatus;
import org.apache.rocketmq.common.message.MessageConst;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.common.message.MessageQueue;

import java.util.List;
import java.util.Map;

import static com.xiaojukeji.carrera.cproxy.consumer.ConsumeContext.MessageSource.RMQ;


public class CarreraNewRocketMqConsumer extends AbstractCarreraRocketMqConsumer {

    public CarreraNewRocketMqConsumer(String brokerCluster, String group, GroupConfig groupConfig,
                                      CProxyConfig cProxyConfig, RocketmqConfiguration rocketmqConfiguration,
                                      AsyncMessageHandler handler, Map<String, Long> maxCommitLagMap, int totalThreads) {
        super(brokerCluster, group, groupConfig, cProxyConfig, rocketmqConfiguration, handler, maxCommitLagMap, totalThreads);
    }

    @Override
    protected void setUpExtra(DefaultMQPushConsumer rmqConsumer) {
        rmqConsumer.setSharedThreadPoolExecutor(SharedThreadPool.getExecutor());
        rmqConsumer.setSharedScheduledExecutorService(SharedThreadPool.getScheduler());
    }

    @Override
    protected ConsumeOrderlyStatus consumeRocketMQMessages(List<MessageExt> msgs, MessageQueue mq) {
        if (!isRunning) {
            return ConsumeOrderlyStatus.SUSPEND_CURRENT_QUEUE_A_MOMENT;
        }

        if (msgs.size() <= 0) {
            return ConsumeOrderlyStatus.SUCCESS;
        }

        assert msgs.size() == 1;

        String topic = msgs.get(0).getTopic();

        if (!LimiterMgr.getInstance().doNonBlockLimit(getGroupBrokerCluster(), topic, msgs.size())) {
            return ConsumeOrderlyStatus.SUSPEND_CURRENT_QUEUE_A_MOMENT_FOR_BLOCK;
        }

        for (final MessageExt msg : msgs) {
            final CommonMessage commonMessage = new CommonMessage(msg.getTopic(), msg.getKeys(), msg.getBody());
            final ConsumeContext context = new ConsumeContext(RMQ, group);
            context.setStartTime(System.currentTimeMillis());
            context.setMessageQueue(mq);
            context.setOffset(msg.getQueueOffset());
            context.setOriginMessage(msg);
            context.setQid(QidUtils.rmqMakeQid(rocketmqConfiguration.getClusterName(), mq.getBrokerName(), mq.getQueueId()));
            context.setProperties(CommonUtils.mapRemoveKeys(msg.getProperties(), MessageConst.STRING_HASH_SET));
            try {
                if (commitLagLimiter != null && !commitLagLimiter.tryAcquire(tracker, topic, context)) {
                    return ConsumeOrderlyStatus.SUSPEND_CURRENT_QUEUE_A_MOMENT_FOR_BLOCK;
                }
                handleMessage(commonMessage, context);
            } catch (Exception e) {
                logConsumeException(commonMessage, context, e);
                return ConsumeOrderlyStatus.SUSPEND_CURRENT_QUEUE_A_MOMENT;
            }
        }
        return ConsumeOrderlyStatus.SUCCESS;
    }
}