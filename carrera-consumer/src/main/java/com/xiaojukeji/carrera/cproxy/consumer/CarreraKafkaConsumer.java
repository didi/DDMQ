package com.xiaojukeji.carrera.cproxy.consumer;

import com.xiaojukeji.carrera.config.v4.CProxyConfig;
import com.xiaojukeji.carrera.config.v4.GroupConfig;
import com.xiaojukeji.carrera.config.v4.cproxy.KafkaConfiguration;
import com.xiaojukeji.carrera.config.v4.cproxy.UpstreamTopic;
import com.xiaojukeji.carrera.cproxy.consumer.handler.AsyncMessageHandler;
import com.xiaojukeji.carrera.cproxy.consumer.offset.ConsumeOffsetTracker;
import com.xiaojukeji.carrera.cproxy.consumer.offset.OffsetTrackSnapshot;
import com.xiaojukeji.carrera.cproxy.consumer.limiter.LimiterMgr;
import com.xiaojukeji.carrera.cproxy.utils.LogUtils;
import com.xiaojukeji.carrera.cproxy.utils.QidUtils;
import kafka.common.TopicAndPartition;
import kafka.consumer.*;
import kafka.javaapi.consumer.ConsumerConnector;
import kafka.message.MessageAndMetadata;
import kafka.serializer.DefaultDecoder;
import kafka.serializer.StringDecoder;
import kafka.utils.Pool;
import kafka.utils.ZKStringSerializer$;
import kafka.utils.ZkUtils$;
import org.I0Itec.zkclient.ZkClient;
import org.apache.commons.collections.CollectionUtils;
import scala.Tuple2;
import scala.collection.Iterator;
import scala.collection.JavaConversions;
import scala.collection.JavaConverters;
import scala.collection.Seq;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.*;


public class CarreraKafkaConsumer extends BaseCarreraConsumer {
    private volatile ExecutorService executorService;
    private volatile ConsumerConnector kafkaConnector;
    private volatile ZookeeperConsumerConnector underlyingKafkaConnector;
    private volatile ScheduledFuture<?> kafkaAutoCommitFuture;

    private ZkClient zkClient = null;
    private final Map<String, List<Object>> topicsAllPartition = new ConcurrentHashMap<>();
    private final Map<String, List<Object>> topicsAssignedPartition = new ConcurrentHashMap<>();
    private final Map<String, Integer> oldAssignedPartitionNum = new HashMap<>();
    private final Map<String, Integer> oldAllPatititonNum = new HashMap<>();
    private ScheduledFuture scheduledFuture = null;
    private ScheduledExecutorService schedule = SharedThreadPool.getScheduler();

    private KafkaConfiguration kafkaConfiguration;

    private Map<String, Integer> topicCountMap;
    private Map<String, UpstreamTopic> topicMap;

    public CarreraKafkaConsumer(String brokerCluster, String group, GroupConfig groupConfig,
                                CProxyConfig cProxyConfig, KafkaConfiguration kafkaConfiguration, AsyncMessageHandler handler,
                                Map<String, Long> maxCommitLagMap, Map<String, Integer> topicCountMap, Map<String, UpstreamTopic> topicMap) {
        super(brokerCluster, group, groupConfig, cProxyConfig, handler, maxCommitLagMap);
        this.kafkaConfiguration = kafkaConfiguration;

        this.topicCountMap = topicCountMap;
        this.topicMap = topicMap;

        assert !CollectionUtils.isEmpty(groupConfig.getTopics());
        assert kafkaConfiguration != null;
    }

    public void doStart() throws Exception {
        String groupId = group;
        LOGGER.info("startConsumeKafka for group={}, groupConfig={}, topicCountMap:{}.", groupId, groupConfig, topicCountMap);

        if (kafkaConnector != null) {
            LOGGER.error("kafka consumer connector is already connected.");
            return;
        }
        tracker = new ConsumeOffsetTracker(true, ConsumeContext.MessageSource.KAFKA, this);

        Properties properties = new Properties();
        properties.putAll(kafkaConfiguration.toProperties());
        properties.put("group.id", groupId);
        LOGGER.info("properties=" + properties);

        if (autoCommit) {
            properties.put("auto.commit.enable", "false");
        }
        ConsumerConfig config = new ConsumerConfig(properties);
        zkClient = new ZkClient(config.zkConnect(), config.zkSessionTimeoutMs(), config.zkConnectionTimeoutMs(), ZKStringSerializer$.MODULE$);
        kafkaConnector = Consumer.createJavaConsumerConnector(config);
        if (autoCommit) {
            initOffsetCommit();
            long autoCommitInterval = config.autoCommitIntervalMs();
            kafkaAutoCommitFuture = autoOffsetCommitService.scheduleAtFixedRate(this::commitOffset, autoCommitInterval,
                    autoCommitInterval, TimeUnit.MILLISECONDS);
        }

        Map<String, List<KafkaStream<String, byte[]>>> streamsMap;
        streamsMap = kafkaConnector.createMessageStreams(topicCountMap,
                new StringDecoder(null), new DefaultDecoder(null));
        executorService = Executors.newFixedThreadPool(streamsMap.values().stream().mapToInt(List::size).sum(),
                r -> new Thread(r, "Kafka-Stream-[" + groupId + "]"));
        isRunning = true;
        streamsMap.forEach((topic, streams) -> {
            int streamCnt = 0;
            for (final KafkaStream<String, byte[]> stream : streams) {
                String streamId = String.format("[%s][%s]#%d", groupId, topic, streamCnt++);
                executorService.execute(() -> {
                    LOGGER.info("Kafka Consume Thread for Stream {} Started!", streamId);
                    ConsumerIterator<String, byte[]> iterator = stream.iterator();
                    try {
                        while (isRunning && iterator.hasNext()) {

                            LimiterMgr.getInstance().doBlockLimit(getGroupBrokerCluster(), topic, 1);

                            MessageAndMetadata<String, byte[]> msg = iterator.next();
                            final CommonMessage commonMessage = new CommonMessage(msg.topic(), msg.key(), msg.message());
                            final ConsumeContext context = new ConsumeContext(ConsumeContext.MessageSource.KAFKA, groupId);
                            context.setStartTime(System.currentTimeMillis());
                            context.setPartitionId(msg.partition());
                            context.setOffset(msg.offset());
                            context.setQid(QidUtils.kafkaMakeQid(kafkaConfiguration.getClusterName(), msg.partition()));
                            try {
                                if (commitLagLimiter != null) {
                                    commitLagLimiter.acquire(tracker, topic, context);
                                }
                                handleMessage(commonMessage, context);
                            } catch (InterruptedException e) {
                                throw e;
                            } catch (Exception e) {
                                logConsumeException(commonMessage, context, e);
                            }
                        }
                    } catch (Exception e) {
                        //noinspection ConstantConditions
                        if (e instanceof InterruptedException) {
                            LOGGER.warn("consume thread is interrupted. stream:{}", streamId);
                            Thread.currentThread().interrupt();
                        } else {
                            LOGGER.error("Exception on kafka consume thread stream: " + streamId, e);
                        }
                    }
                    LOGGER.info("Kafka Consume Thread for stream {} Finished!", streamId);
                });
            }
        });
        LogUtils.logMainInfo("group:{}, kafka_set_consumeThread : {}", getGroupName(), streamsMap.values().stream().mapToInt(List::size).sum());
        scheduledFuture = schedule.scheduleWithFixedDelay(this::adjustRateLimitThreshold, 1000, 5000, TimeUnit.MILLISECONDS);
    }

    private void initOffsetCommit() {
        try {
            Field field = kafka.javaapi.consumer.ZookeeperConsumerConnector.class.getDeclaredField("underlying");
            field.setAccessible(true);
            underlyingKafkaConnector = (ZookeeperConsumerConnector) field.get(kafkaConnector);
        } catch (Exception e) {
            LOGGER.error("get underlying failed.", e);
        }
    }

    @Override
    public synchronized void shutdown() {
        if (kafkaConnector == null && !isRunning) {
            LOGGER.warn("already shut down KafkaCarreraConsumer for {}", group);
            return;
        }

        if (kafkaConnector != null) {
            try {
                LOGGER.info("shutting down KafkaCarreraConsumer for {}", group);

                boolean isStart = isRunning;
                isRunning = false;
                if (executorService != null) {
                    executorService.shutdownNow();
                    executorService = null;
                }

                if (autoCommit && kafkaAutoCommitFuture != null) {
                    kafkaAutoCommitFuture.cancel(true);
                    kafkaAutoCommitFuture = null;
                    if (isStart) {
                        commitOffset();
                    }
                }

                if (kafkaConnector != null) {
                    kafkaConnector.shutdown();
                    underlyingKafkaConnector = null;
                    kafkaConnector = null;
                }

                if (zkClient != null) {
                    zkClient.close();
                }
            } catch (Exception e) {
                LOGGER.error("shutdown Kafka Consumer error", e);
            }
        }
        scheduledFuture.cancel(true);
        super.shutdown();
    }

    @Override
    public void commitOffset() {
        String groupId = group;
        LOGGER.trace("start commit offset for Kafka, group={}", groupId);
        long start = System.currentTimeMillis();
        final int[] commitCount = {0};
        try {
            if (underlyingKafkaConnector == null) {
                initOffsetCommit();
            }
            tracker.getKafkaTrackerMap().forEach((topic, offsetMap) -> offsetMap.forEach((partition, tracker) -> {
                long committableOffset = tracker.getMaxCommittableFinish();
                if (committableOffset > tracker.getCommittedOffset()) {
                    try {
                        if (LOGGER.isDebugEnabled()) {
                            LOGGER.debug("commit offset: group={},topic={},partition={},offset={},maxFinish={},maxStart={},tracker={}",
                                    groupId, topic, partition, committableOffset, tracker.getMaxFinish(), tracker.getMaxStart(), tracker.hashCode());
                        }
                        underlyingKafkaConnector.commitOffsetToZooKeeper(new TopicAndPartition(topic, partition), committableOffset);
                        tracker.setCommittedOffset(committableOffset);
                        commitCount[0]++;
                    } catch (Exception e) {
                        if (Thread.currentThread().isInterrupted()) {
                            LOGGER.warn("commit Kafka offset interrupted!");
                            throw new RuntimeException("this thread is interrupted!");
                        }
                        LOGGER.error(String.format("commit offset for Kafka failed;group=%s, topic=%s, partition=%d, offset=%d",
                                groupId, topic, partition, committableOffset), e);
                    }
                }
            }));
        } catch (Exception e) {
            LOGGER.error("commitKafkaOffset failed!, group=" + groupId, e);
        }
        if (commitCount[0] > 0) {
            LOGGER.info("finished commit offset for Kafka, group={}, commit count={}, cost={}ms", groupId,
                    commitCount[0], System.currentTimeMillis() - start);
        }
    }

    @Override
    public void onConsumeFailed(CommonMessage commonMessage, ConsumeContext context) {
        if (!isRunning) return;
    }

    public List<OffsetTrackSnapshot> takeOffsetTrackSnapshot(Map<String, Map<Integer, Long>> kafkaMaxOffsetMap) {
        if (tracker == null) return Collections.emptyList();
        return removeUnsubscriptedQids(tracker.takeKafkaSnapshot(kafkaMaxOffsetMap));
    }

    @Override
    public Set<String> getCurrentTopicQids(String topic) {
        Set<String> qids = new HashSet<>();
        if (underlyingKafkaConnector == null) {
            initOffsetCommit();
        }
        if (underlyingKafkaConnector.getTopicRegistry() == null || underlyingKafkaConnector.getTopicRegistry().get(topic) == null) {
            return qids;
        }
        scala.collection.mutable.Set<Object> partitionSet = underlyingKafkaConnector.getTopicRegistry().get(topic).keys();
        Object[] partitionArray = new Object[partitionSet.size()];
        partitionSet.copyToArray(partitionArray);
        for (Object partition : partitionArray) {
            if (partition instanceof Integer) {
                qids.add(QidUtils.kafkaMakeQid(kafkaConfiguration.getClusterName(), (Integer) partition));
            } else {
                LOGGER.error("partition is not Integer!");
            }
        }
        return qids;
    }

    private void updateTopicsPartitionInfo() {
        topicsAllPartition.clear();
        topicsAssignedPartition.clear();
        try {
            Set<String> topics = topicMap.keySet();
            Iterator<Tuple2<String, Seq<Object>>> iterator = ZkUtils$.MODULE$.getPartitionsForTopics(
                    zkClient, JavaConverters.asScalaIteratorConverter(topics.iterator()).asScala().toSeq()
            ).iterator();
            while (iterator.hasNext()) {
                Tuple2<String, Seq<Object>> tp2 = iterator.next();
                topicsAllPartition.put(tp2._1, JavaConversions.asJavaList(tp2._2));
            }
            LOGGER.info("updateTopicsAllPartition: " + topicsAllPartition.toString());

            if (underlyingKafkaConnector == null) {
                initOffsetCommit();
            }
            Iterator<Tuple2<String, Pool<Object, PartitionTopicInfo>>> iterator2 = underlyingKafkaConnector.getTopicRegistry().toIterator();
            while (iterator2.hasNext()) {
                Tuple2<String, Pool<Object, PartitionTopicInfo>> tp2 = iterator2.next();
                Iterator<Tuple2<Object, PartitionTopicInfo>> pttIterator = tp2._2.toIterator();
                while (pttIterator.hasNext()) {
                    Object ob = pttIterator.next()._1;
                    topicsAssignedPartition.computeIfAbsent(tp2._1, k -> new ArrayList<>()).add(ob);
                }
            }
            LOGGER.info("updateTopicsAssignedPartition: " + topicsAssignedPartition);
        } catch (Exception e) {
            LOGGER.error("updateTopicsPartitionInfo error!", e);
        }
    }

    private void adjustRateLimitThreshold() {
        if (topicMap == null || CollectionUtils.isEmpty(topicMap.keySet())) {
            return;
        }
        try {
            updateTopicsPartitionInfo();
            for (UpstreamTopic topicConf : groupConfig.getTopics()) {
                String topic = topicConf.getTopic();
                int oldAllNum = oldAllPatititonNum.getOrDefault(topic, 0);
                int oldAssignedNum = oldAssignedPartitionNum.getOrDefault(topic, 0);
                int newAllNum = topicsAllPartition.getOrDefault(topic, Collections.emptyList()).size();
                int newAssignedNum = topicsAssignedPartition.getOrDefault(topic, Collections.emptyList()).size();

                if (newAllNum <= 0 || newAssignedNum <= 0) {
                    continue;
                }

                if (oldAllNum != newAllNum || oldAssignedNum != newAssignedNum) {
                    double coeff = newAssignedNum * 1.0 / newAllNum;
                    double correctTps, httpCorrectTps;
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

                    LimiterMgr.getInstance().adjustThreshold(
                            getGroupBrokerCluster(),
                            topic,
                            correctTps,
                            httpCorrectTps
                    );
                    oldAllPatititonNum.put(topic, newAllNum);
                    oldAssignedPartitionNum.put(topic, newAssignedNum);
                }
            }
        } catch (Throwable t) {
            LOGGER.error("Got a throwable in adjustRateLimitThreshold for kafka. group:{}", group, t);
        }
    }
}