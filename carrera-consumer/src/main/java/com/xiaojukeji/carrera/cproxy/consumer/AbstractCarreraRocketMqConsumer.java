package com.xiaojukeji.carrera.cproxy.consumer;

import com.xiaojukeji.carrera.config.v4.CProxyConfig;
import com.xiaojukeji.carrera.config.v4.GroupConfig;
import com.xiaojukeji.carrera.config.v4.cproxy.RocketMQBaseConfig;
import com.xiaojukeji.carrera.config.v4.cproxy.RocketmqConfiguration;
import com.xiaojukeji.carrera.config.v4.cproxy.UpstreamTopic;
import com.xiaojukeji.carrera.cproxy.consumer.handler.AsyncMessageHandler;
import com.xiaojukeji.carrera.cproxy.consumer.offset.ConsumeOffsetTracker;
import com.xiaojukeji.carrera.cproxy.consumer.offset.OffsetTrackSnapshot;
import com.xiaojukeji.carrera.cproxy.consumer.offset.OffsetTracker;
import com.xiaojukeji.carrera.cproxy.consumer.limiter.LimiterMgr;
import com.xiaojukeji.carrera.cproxy.utils.PropertyUtils;
import com.xiaojukeji.carrera.cproxy.utils.LogUtils;
import com.xiaojukeji.carrera.cproxy.utils.QidUtils;
import com.xiaojukeji.carrera.cproxy.utils.StringUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeOrderlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerOrderly;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.impl.consumer.DefaultMQPushConsumerImpl;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.common.message.MessageQueue;
import org.apache.rocketmq.common.protocol.body.ConsumerRunningInfo;
import org.apache.rocketmq.remoting.exception.RemotingException;

import java.util.*;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;


abstract class AbstractCarreraRocketMqConsumer extends BaseCarreraConsumer {

    private static volatile AtomicInteger instanceNum = new AtomicInteger(0);

    private volatile ScheduledFuture<?> rmqCommitFuture;

    private volatile DefaultMQPushConsumer rmqConsumer;

    protected RocketmqConfiguration rocketmqConfiguration;

    private int totalThreads;

    public AbstractCarreraRocketMqConsumer(String brokerCluster, String group, GroupConfig groupConfig,
                                           CProxyConfig cProxyConfig, RocketmqConfiguration rocketmqConfiguration,
                                           AsyncMessageHandler handler, Map<String, Long> maxCommitLagMap, int totalThreads) {
        super(brokerCluster, group, groupConfig, cProxyConfig, handler, maxCommitLagMap);
        this.rocketmqConfiguration = rocketmqConfiguration;
        this.totalThreads = totalThreads;
    }

    @Override
    public void doStart() throws Exception {
        LOGGER.info("startConsume for group={}, groupConfig:{}", group, groupConfig);
        if (rmqConsumer != null) {
            LogUtils.logErrorInfo("RocketMQ_already_started", "RocketMQ already started!");
            return;
        }

        tracker = new ConsumeOffsetTracker(true, ConsumeContext.MessageSource.RMQ, this);

        rmqConsumer = new DefaultMQPushConsumer(group);
        rmqConsumer.setNamesrvAddr(StringUtils.join(rocketmqConfiguration.getNamesrvAddrs().iterator(), ";"));
        setup(rmqConsumer, rocketmqConfiguration, groupConfig);
        setUpExtra(rmqConsumer);

        if (autoCommit) {
            long autoCommitInterval = rmqConsumer.getPersistConsumerOffsetInterval();
            rmqCommitFuture = autoOffsetCommitService.scheduleAtFixedRate(this::commitOffset, autoCommitInterval,
                    autoCommitInterval, TimeUnit.MILLISECONDS);
            //turn off auto commit in RMQ client.
            rmqConsumer.setPersistConsumerOffsetInterval(-1);
        }
        isRunning = true;
        rmqConsumer.start();
    }

    private void setup(DefaultMQPushConsumer rmqConsumer, RocketMQBaseConfig config, GroupConfig groupConfig) {
        rmqConsumer.setInstanceName(rmqConsumer.getInstanceName() + (instanceNum.addAndGet(1) / 3000));

        if (config == null || config.getSubscription() == null) {
            Map<String, String> subscription = new HashMap<>();
            for (UpstreamTopic topicConf : groupConfig.getTopics()) {
                String subExpression;
                if (CollectionUtils.isEmpty(topicConf.getTags()) || topicConf.getTags().contains("*")) {
                    subExpression = "*";
                } else {
                    subExpression = StringUtils.join(topicConf.getTags(), "||");
                }
                subscription.put(topicConf.getTopic(), subExpression);
            }
            rmqConsumer.setSubscription(subscription);
        }

        if (config == null || config.getConsumeThreadMin() == null) {
            rmqConsumer.setConsumeThreadMin(totalThreads);
        }

        if (config == null || config.getConsumeThreadMax() == null) {
            rmqConsumer.setConsumeThreadMax(totalThreads);
        }
        LogUtils.logMainInfo("group:{}, rmq_set_consumeThread : {}", getGroupName(), totalThreads);

        rmqConsumer.setMessageListener((MessageListenerOrderly) (msgs, context) -> {
            if (autoCommit) {
                context.setAutoCommit(false);
            }
            return consumeRocketMQMessages(msgs, context.getMessageQueue());
        });

        if (config != null) {
            config.setOrderly(null);
            PropertyUtils.copyNonNullProperties(rmqConsumer, config);
        }

        int maxConcurrency;
        if (config != null && config.getPullThresholdForTopic() != null && config.getPullThresholdForTopic() != -1) {
            maxConcurrency = config.getPullThresholdForTopic();
        } else {
            maxConcurrency = groupConfig.getTopics().stream().mapToInt(UpstreamTopic::getConcurrency).max().orElse(1000);
        }
        maxConcurrency = Math.max(1000, maxConcurrency);
        LogUtils.logMainInfo("group:{}, rmq_set_PullThresholdForTopic: {}", getGroupName(), maxConcurrency);
        rmqConsumer.setPullThresholdForTopic(maxConcurrency);

        rmqConsumer.setMessageQueueListener((topic, mqAll, mqDivided) -> {
            if (mqAll.size() <= 0 || mqDivided.size() <= 0) {
                return;
            }
            for(UpstreamTopic topicConf : groupConfig.getTopics()) {
                if (topicConf.getTopic().equals(topic)) {
                    double correctTps, httpCorrectTps;
                    double coeff = mqDivided.size() * 1.0 / mqAll.size();
                    if (topicConf.getTotalMaxTps() <= 0) {
                        correctTps = topicConf.getMaxTps();
                    } else {
                        correctTps = coeff * topicConf.getTotalMaxTps();
                    }

                    if (topicConf.getHttpMaxTps() > 0) {
                        httpCorrectTps = coeff * topicConf.getHttpMaxTps();
                    } else {
                        httpCorrectTps = correctTps;
                    }
                    LimiterMgr.getInstance().adjustThreshold(getGroupBrokerCluster(), topic, correctTps, httpCorrectTps);
                    break;
                }
            }
        });
    }

    protected abstract void setUpExtra(DefaultMQPushConsumer rmqConsumer);

    @Override
    public synchronized void shutdown() {
        if(!isRunning) {
            LOGGER.warn("already shutdown RMQConsumer for {}", group);
            return;
        }
        LOGGER.info("shutting down RMQConsumer for {}", group);

        rmqConsumer.getDefaultMQPushConsumerImpl().getConsumeMessageService().shutdown();
        if (rmqCommitFuture != null) {
            rmqCommitFuture.cancel(true);
            rmqCommitFuture = null;
            if(isRunning) {
                commitOffset();
            }
        }

        if(rmqConsumer != null) {
            rmqConsumer.shutdown();
            rmqConsumer = null;
        }

        super.shutdown();
        isRunning = false;
        LOGGER.info("consumer shutdown, group:{}, brokerCluster:{}.", group, brokerCluster);
    }

    @Override
    public Set<String> getCurrentTopicQids(String topic) {
        try {
            return fetchSubscribeQid(topic);
        } catch (MQClientException e) {
            return Collections.emptySet();
        }
    }

    @Override
    public void commitOffset() {
        String groupId = group;
        LOGGER.trace("Start commit offset for RMQ, group={}", groupId);
        long start = System.currentTimeMillis();
        int commitCnt = 0;
        for (Map.Entry<MessageQueue, OffsetTracker> entry : tracker.getRmqTrackerMap().entrySet()) {
            MessageQueue mq = entry.getKey();
            OffsetTracker mqTracker = entry.getValue();
            long committableOffset = mqTracker.getMaxCommittableFinish();
            if (committableOffset > mqTracker.getCommittedOffset()) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("commit offset: group={},mq={},offset={},maxFinish={},maxStart={},tracker={}",
                            groupId, mq, committableOffset, mqTracker.getMaxFinish(), mqTracker.getMaxStart(), mqTracker.hashCode());
                }
                rmqConsumer.getDefaultMQPushConsumerImpl().updateConsumeOffset(mq, committableOffset);
                try {
                    rmqConsumer.getDefaultMQPushConsumerImpl().getOffsetStore().updateConsumeOffsetToBroker(mq, committableOffset, true);
                    commitCnt++;
                    tracker.getRmqTrackerMap().get(entry.getKey()).setCommittedOffset(committableOffset);
                    LOGGER.debug("offset update to {}, group={}, mq={}", committableOffset, groupId, mq);
                } catch (InterruptedException e) {
                    LogUtils.logErrorInfo("commit_offset_interrupted", "commit offset interrupted. group={}", groupId);
                    return;
                } catch (RemotingException | MQClientException | MQBrokerException e) {
                    LogUtils.logErrorInfo("commit_offset_failed", "commit offset failed. group={},mq={},offset={}", groupId, entry.getKey(), entry.getValue());
                }
            }
        }
        if (commitCnt > 0) {
            LOGGER.info("finished commit offset for RMQ, group={}, commit count={}, cost={}ms", groupId,
                    commitCnt, System.currentTimeMillis() - start);
        }
    }

    @Override
    public void onConsumeFailed(CommonMessage commonMessage, ConsumeContext context) {
        if (!isRunning) {
            return;
        }
        try {
            rmqConsumer.sendMessageBack(context.getOriginMessage(), 0, context.getMessageQueue().getBrokerName());
        } catch (Exception e) {
            LogUtils.logErrorInfo("sendBack_msg_failed", String.format("sendBack message failed.msg=%s,context=%s",
                    commonMessage.info(), context.info()), e);
        }
    }

    protected abstract ConsumeOrderlyStatus consumeRocketMQMessages(List<MessageExt> msgs, MessageQueue mq);

    public List<OffsetTrackSnapshot> takeOffsetTrackSnapshot(Map<MessageQueue, Long> rmqMaxOffsetMap) {
        if (tracker == null) {
            return Collections.emptyList();
        }
        return removeUnsubscriptedQids(tracker.takeRmqSnapshot(rmqMaxOffsetMap));
    }

    private Set<String> fetchSubscribeQid(String topic) throws MQClientException {
        Optional<DefaultMQPushConsumer> consumer = Optional.ofNullable(rmqConsumer);
        if (!consumer.isPresent()) {
            return Collections.emptySet();
        }
        Set<MessageQueue> mqSet = consumer.map(DefaultMQPushConsumer::getDefaultMQPushConsumerImpl)
                .map(DefaultMQPushConsumerImpl::consumerRunningInfo)
                .map(ConsumerRunningInfo::getMqTable)
                .map(Map::keySet)
                .orElse(Collections.emptySet());

        return mqSet.stream().filter(messageQueue -> messageQueue.getTopic().equals(topic))
                .map(messageQueue -> QidUtils.rmqMakeQid(rocketmqConfiguration.getClusterName(), messageQueue.getBrokerName(), messageQueue.getQueueId()))
                .collect(Collectors.toSet());
    }
}