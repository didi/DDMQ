package com.xiaojukeji.carrera.cproxy.consumer.offset;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.xiaojukeji.carrera.config.v4.CProxyConfig;
import com.xiaojukeji.carrera.config.v4.cproxy.KafkaConfiguration;
import com.xiaojukeji.carrera.config.v4.cproxy.RocketmqConfiguration;
import com.xiaojukeji.carrera.config.v4.cproxy.UpstreamTopic;
import com.xiaojukeji.carrera.cproxy.consumer.CarreraNewRocketMqConsumer;
import com.xiaojukeji.carrera.cproxy.consumer.CarreraRocketMqConsumer;
import com.xiaojukeji.carrera.cproxy.consumer.CarreraKafkaConsumer;
import com.xiaojukeji.carrera.thrift.consumer.ConsumeStats;
import com.xiaojukeji.carrera.cproxy.concurrent.CarreraExecutors;
import com.xiaojukeji.carrera.cproxy.consumer.CarreraConsumer;
import com.xiaojukeji.carrera.cproxy.consumer.LowLevelCarreraConsumer;
import com.xiaojukeji.carrera.cproxy.utils.LogUtils;
import com.xiaojukeji.carrera.cproxy.utils.MetricUtils;
import com.xiaojukeji.carrera.cproxy.utils.QidUtils;
import com.xiaojukeji.carrera.cproxy.utils.TimeUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.message.MessageQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class CarreraOffsetManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(CarreraOffsetManager.class);

    private static final Logger OFFSET_LOGGER = LogUtils.OFFSET_LOGGER;

    private volatile List<CarreraConsumer> consumers = Lists.newArrayList();

    private ScheduledExecutorService scheduledExecutorService = CarreraExecutors.newScheduledThreadPool(10, "OffsetManagerPoolScheduler");

    private volatile Map<String, RocketMQProduceOffsetFetcher> rmqMaxOffsetFetcherMap = new ConcurrentHashMap<>();

    private volatile Map<String, KafkaProduceOffsetFetcher> kafkaMaxOffsetFetcherMap = new ConcurrentHashMap<>();

    public CarreraOffsetManager() {
        scheduledExecutorService.scheduleAtFixedRate(this::logConsumeOffset, 5, 60, TimeUnit.SECONDS);
    }

    public void updateConsumers(List<CarreraConsumer> consumers) {
        this.consumers = consumers;
    }

    synchronized public void update(CProxyConfig cProxyConfig) {

        for (RocketMQProduceOffsetFetcher rocketMQProduceOffsetFetcher : rmqMaxOffsetFetcherMap.values()) {
            rocketMQProduceOffsetFetcher.shutdown();
        }
        rmqMaxOffsetFetcherMap.clear();
        for (KafkaProduceOffsetFetcher kafkaProduceOffsetFetcher : kafkaMaxOffsetFetcherMap.values()) {
            kafkaProduceOffsetFetcher.shutdown();
        }
        kafkaMaxOffsetFetcherMap.clear();

        for (Map.Entry<String, KafkaConfiguration> entry : cProxyConfig.getKafkaConfigs().entrySet()) {
            String brokerCluster = entry.getKey();
            KafkaConfiguration kafkaConf = entry.getValue();
            KafkaProduceOffsetFetcher fetcher = newKafkaMaxOffsetFetcher(kafkaConf);
            if (fetcher != null) {
                kafkaMaxOffsetFetcherMap.put(brokerCluster, fetcher);
            }
        }
        for (Map.Entry<String, RocketmqConfiguration> entry : cProxyConfig.getRocketmqConfigs().entrySet()) {
            String brokerCluster = entry.getKey();
            RocketmqConfiguration rmqConf = entry.getValue();
            RocketMQProduceOffsetFetcher fetcher = newRmqProduceOffsetFetcher(rmqConf);
            if (fetcher != null) {
                rmqMaxOffsetFetcherMap.put(brokerCluster, fetcher);
            }
        }
    }

    private RocketMQProduceOffsetFetcher newRmqProduceOffsetFetcher(RocketmqConfiguration config) {
        String nameServers = String.join(";", config.getNamesrvAddrs());
        RocketMQProduceOffsetFetcher rmqMaxOffsetFetcher = new RocketMQProduceOffsetFetcher(nameServers);
        try {
            rmqMaxOffsetFetcher.start();
            return rmqMaxOffsetFetcher;
        } catch (MQClientException e) {
            LogUtils.logErrorInfo("new_rmqMaxOffsetFetcher_error","start rmqMaxOffsetFetcher failed, name server = " + nameServers, e);
            return null;
        }
    }

    private KafkaProduceOffsetFetcher newKafkaMaxOffsetFetcher(KafkaConfiguration config) {
        try {
            return new KafkaProduceOffsetFetcher(config.getZookeeperAddr());
        } catch (Exception e) {
            LogUtils.logErrorInfo("new_Kafka" +
                    "MaxOffsetFetcher_error","start kafkaMaxOffsetFetcher failed, zk_addr = " + config.getZookeeperAddr(), e);
            return null;
        }
    }

    private Map<String, Map<Integer, Long>> getKafkaMaxOffsets(KafkaProduceOffsetFetcher fetcher, Set<String> topics) {
        if (fetcher == null) {
            return Collections.emptyMap();
        }
        try {
            return fetcher.getMaxOffset(fetcher.getMetadata(topics));
        } catch (Exception e) {
            LOGGER.error("get kafka max offset error", e);
            return Collections.emptyMap();
        }
    }

    private Map<MessageQueue, Long> getRmqMaxOffsets(RocketMQProduceOffsetFetcher fetcher) {
        if (fetcher == null) {
            return Collections.emptyMap();
        }
        try {
            return fetcher.getMaxOffset(fetcher.getBrokers());
        } catch (Exception e) {
            LOGGER.error("get Rocketmq max offset failed!!!", e);
            return Collections.emptyMap();
        }
    }

    private void logConsumeOffset() {
        long startTime = TimeUtils.getCurTime();
        if (CollectionUtils.isEmpty(consumers)) return;

        List<OffsetTrackSnapshot> offsetTrackSnapshots = buildOffsetTrackSnapshotTable();

        for (OffsetTrackSnapshot snapshot : offsetTrackSnapshots) {
            long maxOffset = snapshot.getMaxOffset();
            long start = snapshot.getStartConsumeOffset();
            long finish = snapshot.getFinishConsumeOffset();
            long commitable = snapshot.getMaxCommittableOffset();
            long committed = snapshot.getCommittedOffset();

            long fetchLag = (maxOffset > 0 && start > 0) ? Math.max(0, maxOffset - start) : 0;
            long commitableLag = finish > 0 ? (commitable > 0 ? Math.max(0, finish - commitable) : Math.max(0, finish - committed)) : 0;
            long committedLag = (maxOffset > 0 && committed > 0) ? Math.max(0, maxOffset - committed) : 0;
            long consumeLag = (finish > 0 && maxOffset > 0) ? Math.max(0, maxOffset - finish) : committedLag;

            OFFSET_LOGGER.info("group={},snapshot={}", snapshot.getGroup(), snapshot);
            String groupTopic = snapshot.getGroup() + "-" + snapshot.getTopic();
            MetricUtils.maxOffsetCount(snapshot.getGroup(), snapshot.getTopic(), snapshot.getQid(), "committed", snapshot.getCommittedOffset());
            MetricUtils.maxOffsetCount(snapshot.getGroup(), snapshot.getTopic(), snapshot.getQid(), "consume", snapshot.getFinishConsumeOffset());
            MetricUtils.maxOffsetCount(snapshot.getGroup(), snapshot.getTopic(), snapshot.getQid(), "produce", snapshot.getMaxOffset());
            MetricUtils.maxOffsetCount(snapshot.getGroup(), snapshot.getTopic(), snapshot.getQid(), "commitable", snapshot.getMaxCommittableOffset());

            MetricUtils.maxOffsetLagCount(snapshot.getGroup(), snapshot.getTopic(), snapshot.getQid(), "fetchLag", fetchLag);
            OFFSET_LOGGER.info("groupId-topic={},qid={},fetchLag={}", groupTopic, snapshot.getQid(), fetchLag);

            MetricUtils.maxOffsetLagCount(snapshot.getGroup(), snapshot.getTopic(), snapshot.getQid(), "commitableLag", commitableLag);
            OFFSET_LOGGER.info("groupId-topic={},qid={},commitableLag={}", groupTopic, snapshot.getQid(), commitableLag);

            MetricUtils.maxOffsetLagCount(snapshot.getGroup(), snapshot.getTopic(), snapshot.getQid(), "commitLag", committedLag);
            OFFSET_LOGGER.info("groupId-topic={},qid={},commitLag={}", groupTopic, snapshot.getQid(), committedLag);

            MetricUtils.maxOffsetLagCount(snapshot.getGroup(), snapshot.getTopic(), snapshot.getQid(), "consumeLag", consumeLag);
            OFFSET_LOGGER.info("groupId-topic={},qid={},consumeLag={}", groupTopic, snapshot.getQid(), consumeLag);
        }

        LOGGER.info("logConsumeOffset finished, cost={}ms", TimeUtils.getElapseTime(startTime));
    }

    private List<OffsetTrackSnapshot> buildOffsetTrackSnapshotTable() {
        if (CollectionUtils.isEmpty(consumers)) return Collections.emptyList();
        List<OffsetTrackSnapshot> ret = new ArrayList<>();

        consumers.forEach(consumer -> {
            if (consumer instanceof LowLevelCarreraConsumer) {
                return;
            }
            String brokerCluster = consumer.getBrokerCluster();
            if (consumer.getConsumer() instanceof CarreraRocketMqConsumer) {
                RocketMQProduceOffsetFetcher fetcher = rmqMaxOffsetFetcherMap.get(brokerCluster);
                Map<MessageQueue, Long> rmqMaxOffsetMap = getRmqMaxOffsets(fetcher);
                List<OffsetTrackSnapshot> offsetTrackers = ((CarreraRocketMqConsumer) consumer.getConsumer()).takeOffsetTrackSnapshot(rmqMaxOffsetMap);
                ret.addAll(offsetTrackers);
            } else if (consumer.getConsumer() instanceof CarreraKafkaConsumer) {
                KafkaProduceOffsetFetcher fetcher = kafkaMaxOffsetFetcherMap.get(brokerCluster);

                Set<String/*topic名字*/> topics = Sets.newHashSet();
                for (UpstreamTopic topicConf : consumer.getConfig().getGroupConfig().getTopics()) {
                    topics.add(topicConf.getTopic());
                }
                Map<String, Map<Integer, Long>> kafkaMaxOffsetMap = getKafkaMaxOffsets(fetcher, topics);
                List<OffsetTrackSnapshot> offsetTrackers = ((CarreraKafkaConsumer) consumer.getConsumer()).takeOffsetTrackSnapshot(kafkaMaxOffsetMap);
                ret.addAll(offsetTrackers);
            } else if (consumer.getConsumer() instanceof CarreraNewRocketMqConsumer) {
                RocketMQProduceOffsetFetcher fetcher = rmqMaxOffsetFetcherMap.get(brokerCluster);
                Map<MessageQueue, Long> rmqMaxOffsetMap = getRmqMaxOffsets(fetcher);
                List<OffsetTrackSnapshot> offsetTrackers = ((CarreraNewRocketMqConsumer) consumer.getConsumer()).takeOffsetTrackSnapshot(rmqMaxOffsetMap);
                ret.addAll(offsetTrackers);
            } else {
                LOGGER.error("buildOffsetTrackSnapshotTable, unknown consumer type, {}", consumer.getConsumer().getClass());
            }
        });

        return ret;
    }

    public List<ConsumeStats> getKafkaConsumeStats(String cluster, String group, List<String> topics) {
        KafkaProduceOffsetFetcher fetcher = kafkaMaxOffsetFetcherMap.get(cluster);
        if (fetcher == null) return Collections.emptyList();

        List<ConsumeStats> rets = new ArrayList<>();

        topics.forEach(topic -> {
            try {
                Map<String, Long> consumeOffsets = new HashMap<>();
                Map<String, Long> produceOffsets = new HashMap<>();
                ConsumeStats consumeStats = new ConsumeStats(group, topic, consumeOffsets, produceOffsets);
                Map<String, Map<Integer, Long>> maxOffsets = getKafkaMaxOffsets(fetcher, Sets.newHashSet(topics));
                if (maxOffsets.containsKey(topic)) {
                    fetcher.getConsumeOffset(group, topic).forEach((k, v) -> consumeOffsets.put(QidUtils.kafkaMakeQid(cluster, k), v));
                    maxOffsets.get(topic).forEach((k, v) -> produceOffsets.put(cluster +"_" + k, v));
                }
                rets.add(consumeStats);
            } catch (Exception e) {
                LOGGER.error("getKafkaConsumeStats error, " + group + "-" + topic, e);
            }
        });

        return rets;
    }

    public List<ConsumeStats> getRmqConsumeStats(String cluster, String group, List<String> topics) {
        RocketMQProduceOffsetFetcher fetcher = rmqMaxOffsetFetcherMap.get(cluster);
        if (fetcher == null) return Collections.emptyList();

        List<ConsumeStats> rets = new ArrayList<>();

        topics.forEach(topic -> {
            try {
                Map<String, Long> consumeOffsets = new HashMap<>();
                Map<String, Long> produceOffsets = new HashMap<>();
                ConsumeStats consumeStats = new ConsumeStats(group, topic, consumeOffsets, produceOffsets);
                fetcher.getConsumeStats(group, topic).getOffsetTable().forEach((mq, offset) -> {
                    String qid = QidUtils.rmqMakeQid(cluster, mq.getBrokerName(), mq.getQueueId());
                    consumeOffsets.put(qid, offset.getConsumerOffset());
                    produceOffsets.put(qid, offset.getBrokerOffset());
                });
                rets.add(consumeStats);
            } catch (Exception e) {
                LOGGER.error("getRmqConsumeStats error, " + group + "-" + topic, e);
            }
        });

        return rets;
    }

    public ScheduledExecutorService getScheduler() {
        return scheduledExecutorService;
    }

    private static class Singleton {
        private static CarreraOffsetManager INSTANCE = new CarreraOffsetManager();
    }

    public static CarreraOffsetManager getInstance() {
        return CarreraOffsetManager.Singleton.INSTANCE;
    }

    public void shutdown() {
        rmqMaxOffsetFetcherMap.values().parallelStream().forEach(RocketMQProduceOffsetFetcher::shutdown);
        kafkaMaxOffsetFetcherMap.values().parallelStream().forEach(KafkaProduceOffsetFetcher::shutdown);
        rmqMaxOffsetFetcherMap.clear();
        kafkaMaxOffsetFetcherMap.clear();
    }
}