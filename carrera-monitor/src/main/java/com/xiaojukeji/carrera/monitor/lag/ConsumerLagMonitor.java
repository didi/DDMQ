package com.xiaojukeji.carrera.monitor.lag;

import com.alibaba.fastjson.JSON;
import com.xiaojukeji.carrera.monitor.BaseConfigMonitor;
import com.xiaojukeji.carrera.monitor.config.MonitorConfig;
import com.xiaojukeji.carrera.monitor.lag.offset.OffsetManager;
import com.xiaojukeji.carrera.monitor.lag.offset.RocketMQProduceOffsetFetcher;
import com.xiaojukeji.carrera.monitor.utils.ExecutorUtils;
import com.google.common.collect.Maps;
import com.xiaojukeji.carrera.config.v4.BrokerConfig;
import com.xiaojukeji.carrera.config.v4.GroupConfig;
import com.xiaojukeji.carrera.config.v4.cproxy.UpstreamTopic;
import com.xiaojukeji.carrera.dynamic.ParameterDynamicZookeeper;
import com.xiaojukeji.carrera.utils.TimeUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.rocketmq.client.consumer.PullResult;
import org.apache.rocketmq.client.consumer.PullStatus;
import org.apache.rocketmq.common.message.MessageQueue;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;



public class ConsumerLagMonitor extends BaseConfigMonitor {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConsumerLagMonitor.class);

    private ConcurrentMap<String/* broker */, RocketMQProduceOffsetFetcher> rmqFetcherMap = new ConcurrentHashMap<>();

    private ConcurrentMap<String/* group */, MonitorInfo> groupMonitorMap = Maps.newConcurrentMap();

    private ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(100);

    private OffsetManager offsetManager = new OffsetManager();

    public static final long MSG_NOT_FOUND = -2;

    public ConsumerLagMonitor(MonitorConfig monitorConfig) {
        super("lag", monitorConfig);
    }

    @Override
    protected void initMonitor(String broker, BrokerConfig brokerConfig) throws Exception {
        long startTs = TimeUtils.getCurTime();

        // start watch group config.
        zkService.getAndWatchGroup(new ParameterDynamicZookeeper.DataChangeCallback<GroupConfig>() {
            @Override
            public void handleDataChange(String dataPath, GroupConfig data, Stat stat) throws Exception {
                // update monitor this group.
                if (groupMonitorMap.containsKey(data.getGroup())) { // in monitor, need restart.
                    MonitorInfo monitorInfo = groupMonitorMap.get(data.getGroup());
                    monitorInfo.scheduledFuture.cancel(false);
                    groupMonitorMap.remove(data.getGroup());
                    LOGGER.info("[zk group updated]stop monitor consumerLag for group: {}", data.getGroup());

                    if (validateGroupConfig(data)) {
                        ScheduledFuture<?> scheduledFuture = scheduledExecutorService.scheduleAtFixedRate(() -> doMonitor(data), 0, 60, TimeUnit.SECONDS);
                        groupMonitorMap.put(data.getGroup(), new MonitorInfo(data, scheduledFuture));
                        LOGGER.info("[zk group updated]start monitor consumerLag for group: {}", data.getGroup());
                    }
                } else {
                    // new Group, start monitor.
                    if (validateGroupConfig(data)) {
                        ScheduledFuture<?> scheduledFuture = scheduledExecutorService.scheduleAtFixedRate(() -> doMonitor(data), 0, 60, TimeUnit.SECONDS);
                        groupMonitorMap.put(data.getGroup(), new MonitorInfo(data, scheduledFuture));
                        LOGGER.info("[zk new group]start monitor consumerLag for group: {}", data.getGroup());
                    }
                }
            }

            @Override
            public void handleDataDeleted(String dataPath) throws Exception {
                // stop monitor this group.
                String[] split = dataPath.split("/");
                String group = split[split.length - 1];
                if (groupMonitorMap.containsKey(group)) {
                    MonitorInfo monitorInfo = groupMonitorMap.get(group);
                    monitorInfo.scheduledFuture.cancel(false);
                    groupMonitorMap.remove(group);
                    LOGGER.info("[zk group deleted]stop monitor consumerLag for group: {}", group);
                } else {
                    LOGGER.warn("[zk group deleted]delete group already not monitored for group: {}", group);
                }
            }
        });
        LOGGER.info("init ConsumerLagMonitor cost {} ms", TimeUtils.getElapseTime(startTs));
    }

    private boolean validateGroupConfig(GroupConfig groupConfig) {
        if (CollectionUtils.isEmpty(groupConfig.getTopics())) {
            LOGGER.info("no upstreamTopic, group:{}", groupConfig.getGroup());
            return false;
        }

        long enabledCount = groupConfig.getTopics().stream().filter(upstreamTopic -> upstreamTopic.isEnabled()).count();
        if (enabledCount == 0) {
            LOGGER.info("upstreamTopic is not enabled, group:{}", groupConfig.getGroup());
            return false;
        }

        return true;
    }

    private void doMonitor(GroupConfig groupConfig) {
        LOGGER.info("consumerLag monitor start, group:{}", groupConfig.getGroup());
        long startTs = TimeUtils.getCurTime();

        String group = groupConfig.getGroup();
        List<UpstreamTopic> upstreamTopics = groupConfig.getTopics();

        Map<String, Long> produceOffsetMap = new HashMap();
        Map<String, Long> consumeOffsetMap = new HashMap();
        Map<String /*qid*/, MessageQueue> qid2mq = new HashMap<>();

        List<Object> alarmMsgs = new ArrayList();

        for (UpstreamTopic upstreamTopic : upstreamTopics) {
            try {
                String topic = upstreamTopic.getTopic();
                String broker = upstreamTopic.getBrokerCluster();

                if (!upstreamTopic.isEnabled()) {
                    LOGGER.debug("subscription is not enabled for topic:{}, group:{}, broker:{}", topic, group, broker);
                    continue;
                }

                long committedLagThreshold = upstreamTopic.getCommittedLagThreshold();
                long delayTimeThreshold = upstreamTopic.getDelayTimeThreshold();

                produceOffsetMap.clear();
                consumeOffsetMap.clear();
                qid2mq.clear();

                getRmqConsumeStats(group, topic, brokerConfig, produceOffsetMap, consumeOffsetMap, qid2mq);
                RocketMQProduceOffsetFetcher rmqFetcher = getRmqFetcher(brokerConfig);


                consumeOffsetMap.forEach((qid, consumeOffset) -> {
                    long produceOffset = produceOffsetMap.containsKey(qid) ? produceOffsetMap.remove(qid) : -1;
                    long lag = produceOffset > consumeOffset ? produceOffset - consumeOffset : 0;
                    long delay = getRmqConsumeDelay(rmqFetcher, qid2mq.get(qid), produceOffset, consumeOffset, group);

                    offsetManager.markConsume(broker, group, topic, qid, consumeOffset, lag);

                    List<String> msgs = new ArrayList();
                    if (committedLagThreshold != -1L && lag > committedLagThreshold) {
                        msgs.add("消费积压（条数）：" + lag + " > " + committedLagThreshold);
                    }

                    if (delay > delayTimeThreshold) {
                        msgs.add("消费延迟（分钟）：" + delay / 1000 / 60 + " > " + delayTimeThreshold / 1000 / 60);
                    } else if (delay == MSG_NOT_FOUND) { // msg not found.
                        msgs.add("消费延迟 : 当前消费 offset 的消息不存在");
                    }

                    if (CollectionUtils.isNotEmpty(msgs)) {
                        Map<String, Object> alarmMsg = new HashMap();
                        alarmMsg.put("type", "carrera_monitor");
                        alarmMsg.put("cluster", upstreamTopic.getBrokerCluster());
                        alarmMsg.put("group", group);
                        alarmMsg.put("topic", topic);
                        alarmMsg.put("qid", qid);
                        alarmMsg.put("committedLag", lag);
                        alarmMsg.put("alarmMsg", String.join(", ", msgs));
                        alarmMsg.put("committedLagRecent", offsetManager.getConsumeLagRecord(broker, group, topic, qid, 10));
                        alarmMsg.put("committedOffsetRecent", offsetManager.getConsumeOffsetRecord(broker, group, topic, qid, 10));
                        alarmMsg.put("consumeDelay", delay == MSG_NOT_FOUND ? "-1" : delay / 1000 / 60);
                        alarmMsg.put("consumeTime", offsetManager.getConsumeTime(broker, group, topic) / 1000 / 60);
                        alarmMsgs.add(alarmMsg);
                    }

                    LOGGER.info("broker:{}, broker:{}, group:{}, topic:{}, qid:{}, produceOffset:{}, consumeOffset:{}, lag:{}, delay:{}, LagThreshold:{}, delayThreshold:{}",
                            upstreamTopic.getBrokerCluster(), broker, group, topic, qid, produceOffset, consumeOffset, lag, delay, committedLagThreshold,
                            delayTimeThreshold);
                });

                produceOffsetMap.forEach((qid, offset) -> {
                    if (offset > 0) {
                        Map<String, Object> alarmMsg = new HashMap();
                        alarmMsg.put("type", "carrera_monitor");
                        alarmMsg.put("cluster", upstreamTopic.getBrokerCluster());
                        alarmMsg.put("group", group);
                        alarmMsg.put("topic", topic);
                        alarmMsg.put("qid", qid);
                        alarmMsg.put("committedLag", -1);
                        alarmMsg.put("alarmMsg", "qid 未被消费");
                        alarmMsg.put("committedLagRecent", Collections.EMPTY_LIST);
                        alarmMsg.put("committedOffsetRecent", Collections.EMPTY_LIST);
                        alarmMsg.put("consumeDelay", -1);
                        alarmMsg.put("consumeTime", -1);
                        alarmMsgs.add(alarmMsg);
                    }

                    LOGGER.info("broker:{}, broker:{}, group:{}, topic:{}, qid:{}, lag:{}, delay:{}",
                            upstreamTopic.getBrokerCluster(), broker, group, topic, qid, -1, -1);
                });
            } catch (Exception e) {
                LOGGER.error("getConsumeStats error", e);
            }
        }

        if (CollectionUtils.isNotEmpty(alarmMsgs)) {
            Map<String, Object> alarmTable = new HashMap<>();
            alarmTable.put("alarms", alarmMsgs);
            String alarmJson = JSON.toJSONString(alarmTable);
            // 可以通过配置日志采集收集报警信息并配置报警策略.
            LOGGER.info("[CONSUME_LAG_ALARM_MSG] alarm msg={}", alarmJson);
        }

        LOGGER.debug("monitor end, group:{}, costtime:{}ms", groupConfig.getGroup(), TimeUtils.getElapseTime(startTs));
    }

    private long getRmqConsumeDelay(RocketMQProduceOffsetFetcher fetcher, MessageQueue mq, long produceOffset, long consumeOffset, String group) {
        try {
            if (produceOffset == 0L || produceOffset <= consumeOffset) {
                // 未生产 or 消费完毕.
                return 0L;
            }

            PullResult consumePullResult = fetcher.queryMsgByOffset(mq, consumeOffset);

            if (consumePullResult != null && consumePullResult.getPullStatus() == PullStatus.FOUND) {
                return TimeUtils.getElapseTime(consumePullResult.getMsgFoundList().get(0).getStoreTimestamp());
            } else if (consumePullResult.getPullStatus() == PullStatus.NO_MATCHED_MSG) {
                LOGGER.error("failed to getRmqConsumeDelay. mq: {}, namesvr: {}, group: {}, produceOffset: {}, consumeOffset: {}, consumePullResult: {}", mq, fetcher.getNamesrvAddr(), group, produceOffset, consumeOffset, consumePullResult);
                return 0; // 拿不到消息，认为没有延迟.
            } else if (consumePullResult.getPullStatus() == PullStatus.OFFSET_ILLEGAL) {
                LOGGER.error("failed to getRmqConsumeDelay. mq: {}, namesvr: {}, group: {}, produceOffset: {}, consumeOffset: {}, consumePullResult: {}", mq, fetcher.getNamesrvAddr(), group, produceOffset, consumeOffset, consumePullResult);

                PullResult pullResult = fetcher.queryMsgByOffset(mq, consumePullResult.getMinOffset());
                if (pullResult != null && pullResult.getPullStatus() == PullStatus.FOUND) {
                    return TimeUtils.getElapseTime(pullResult.getMsgFoundList().get(0).getStoreTimestamp());
                }

                return MSG_NOT_FOUND;
            } else {
                return 0;
            }
        } catch (Exception e) {
            LOGGER.error("queryMsgByOffset failed,", e);
            return -1L;
        }
    }

    private String buildQid(String mqServer, MessageQueue mq) {
        return mqServer + "_" + mq.getBrokerName() + "_" + mq.getQueueId();
    }

    RocketMQProduceOffsetFetcher getRmqFetcher(BrokerConfig brokerConfig) {
        return rmqFetcherMap.computeIfAbsent(brokerConfig.getBrokerCluster(), r -> {
            RocketMQProduceOffsetFetcher fetcher = new RocketMQProduceOffsetFetcher(brokerConfig.getBrokerClusterAddrs());
            try {
                fetcher.start();
                return fetcher;
            } catch (Exception e) {
                LOGGER.error("start rmq fetcher error, broker: " + brokerConfig.getBrokerCluster(), e);
                return null;
            }
        });
    }

    private void getRmqConsumeStats(String group, String topic, BrokerConfig rmqConfig,
                                    Map<String, Long> produceOffset, Map<String, Long> consumeOffset, Map<String /*qid*/, MessageQueue> qid2mq) throws Exception {
        if (topic.startsWith("%RETRY%")) {
            return;
        }

        RocketMQProduceOffsetFetcher fetcher = getRmqFetcher(rmqConfig);
        if (fetcher == null) throw new Exception("get rmq fetcher error");

        fetcher.getConsumeStats(group, topic).getOffsetTable().forEach((mq, offset) -> {
            consumeOffset.put(buildQid(rmqConfig.getBrokerCluster(), mq), offset.getConsumerOffset());
            produceOffset.put(buildQid(rmqConfig.getBrokerCluster(), mq), offset.getBrokerOffset());
            qid2mq.put(buildQid(rmqConfig.getBrokerCluster(), mq), mq);
        });
    }

    class MonitorInfo {
        private GroupConfig groupConfig;
        private ScheduledFuture scheduledFuture;

        public MonitorInfo(GroupConfig groupConfig, ScheduledFuture scheduledFuture) {
            this.groupConfig = groupConfig;
            this.scheduledFuture = scheduledFuture;
        }

        @Override
        public String toString() {
            return "MonitorInfo{" +
                    "groupConfig=" + groupConfig +
                    ", scheduledFuture=" + scheduledFuture +
                    '}';
        }
    }

    @Override
    public void shutdown() {
        rmqFetcherMap.values().forEach(fetcher -> fetcher.shutdown());
        ExecutorUtils.shutdown(scheduledExecutorService);
        super.shutdown();
    }
}
