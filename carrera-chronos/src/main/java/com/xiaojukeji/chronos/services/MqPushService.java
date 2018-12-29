package com.xiaojukeji.chronos.services;

import com.google.common.base.Charsets;
import com.xiaojukeji.chronos.autobatcher.Batcher;
import com.xiaojukeji.chronos.config.ConfigManager;
import com.xiaojukeji.chronos.config.PushConfig;
import com.xiaojukeji.chronos.db.CFManager;
import com.xiaojukeji.chronos.db.RDB;
import com.xiaojukeji.chronos.ha.MasterElection;
import com.xiaojukeji.chronos.metrics.MetricMsgAction;
import com.xiaojukeji.chronos.metrics.MetricMsgToOrFrom;
import com.xiaojukeji.chronos.metrics.MetricMsgType;
import com.xiaojukeji.chronos.metrics.MetricPushMsgResult;
import com.xiaojukeji.chronos.metrics.MetricService;
import com.xiaojukeji.chronos.model.CancelWrap;
import com.xiaojukeji.chronos.model.InternalPair;
import com.xiaojukeji.chronos.model.InternalValue;
import com.xiaojukeji.chronos.utils.Constants;
import com.xiaojukeji.chronos.utils.JsonUtils;
import com.xiaojukeji.chronos.utils.KeyUtils;
import com.xiaojukeji.chronos.utils.TsUtils;
import com.xiaojukeji.carrera.chronos.enums.Actions;
import com.xiaojukeji.carrera.chronos.enums.MsgTypes;
import com.xiaojukeji.carrera.chronos.model.InternalKey;
import com.xiaojukeji.carrera.config.CarreraConfig;
import com.xiaojukeji.carrera.producer.CarreraProducer;
import com.xiaojukeji.carrera.producer.CarreraReturnCode;
import com.xiaojukeji.carrera.producer.MessageBuilder;
import com.xiaojukeji.carrera.thrift.Result;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.rocksdb.RocksIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static com.xiaojukeji.carrera.chronos.constants.Constant.PROPERTY_KEY_FROM_CHRONOS;
import static com.xiaojukeji.carrera.thrift.producerProxyConstants.PRESSURE_TRAFFIC_KEY;


public class MqPushService {
    private static final Logger LOGGER = LoggerFactory.getLogger(MqPushService.class);

    private static final int BATCH_SEND_THREAD_NUM = ConfigManager.getConfig().getPushConfig().getBatchSendThreadNum();
    private static final int PUSH_QUEUE_SIZE = 100000;
    private static final int DEFAULT_CAPACITY = 100000;
    private static final Map<String, String> needCancelMap = new HashMap<>(DEFAULT_CAPACITY);
    private static final Map<String, String> needCancelTopicMap = new HashMap<>(DEFAULT_CAPACITY);
    private static final int INTERNAL_PAIR_COUNT = 100000;
    private static final BlockingQueue<InternalPair> blockingQueue = new ArrayBlockingQueue<>(INTERNAL_PAIR_COUNT);
    private static volatile MqPushService instance = null;
    private static final ByteArrayOutputStream OUTPUT = new ByteArrayOutputStream();

    private CarreraProducer producer = null;
    private final Batcher batcher = Batcher.getInstance();
    private long round = 0;

    /**
     * 发送线程池(从DB发送)
     */
    private final ThreadPoolExecutor pushThreadPool = new ThreadPoolExecutor(BATCH_SEND_THREAD_NUM, BATCH_SEND_THREAD_NUM,
            30L, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(PUSH_QUEUE_SIZE),
            new BasicThreadFactory.Builder().namingPattern("send-normal-to-pproxy-%d").build(),
            (r, executor) -> {
                try {
                    executor.getQueue().put(r);
                } catch (InterruptedException e) {
                    LOGGER.info("error while reject execution put to queue, err:{}", e.getMessage(), e);
                }
            });

    /**
     * 发送线程池(直接发送)
     */
    private final ThreadPoolExecutor directPushThreadPool = new ThreadPoolExecutor(BATCH_SEND_THREAD_NUM, BATCH_SEND_THREAD_NUM,
            30L, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(PUSH_QUEUE_SIZE),
            new BasicThreadFactory.Builder().namingPattern("send-direct-to-pproxy-%d").build(),
            (r, executor) -> {
                try {
                    executor.getQueue().put(r);
                } catch (InterruptedException e) {
                    LOGGER.info("error while reject execution put to queue, err:{}", e.getMessage(), e);
                }
            });

    private MqPushService() {
        initProducer();
    }

    public void pullFromDefaultCFAndPush() {
        final long seekTimestamp = MetaService.getSeekTimestamp();
        final long zkSeekTimestamp = MetaService.getZkSeekTimestamp();

        // backup的seekTimestamp不能超过master的seekTimestamp
        if (MasterElection.isBackup()) {
            if (seekTimestamp >= zkSeekTimestamp) {
                LOGGER.debug("backup's pull from db should stop for seekTimestamp > zkSeekTimestamp, seekTimestamp:{}, zkSeekTimestamp:{}, Thread:{}",
                        seekTimestamp, zkSeekTimestamp, Thread.currentThread().getName());
                try {
                    TimeUnit.SECONDS.sleep(2);
                } catch (InterruptedException e) {
                }
                return;
            }
        }

        // seekTimestamp不能超过当前时间
        final long now = TsUtils.genTS();
        if (seekTimestamp > now) {
            LOGGER.debug("pull from db should stop for seekTimestamp > now, seekTimestamp:{}, now:{}, Thread:{}",
                    seekTimestamp, now, round, Thread.currentThread().getName());
            try {
                TimeUnit.MILLISECONDS.sleep(100);
            } catch (InterruptedException e) {
            }
            return;
        }

        round++;
        final long start = System.currentTimeMillis();
        final long diff = start / 1000 - seekTimestamp;
        LOGGER.info("pull from db start, seekTimestamp:{}, currTimestamp:{}, diff:{} round:{}",
                seekTimestamp, start / 1000, diff, round);
        MetricService.putSeekLatency(MasterElection.getState().toString(), diff + 10); // 因为0上传到metric之后不显示

        // 迭代出当前 seekTimestamp 下所有数据
        int count = 0;
        try (RocksIterator it = RDB.newIterator(CFManager.CFH_DEFAULT)) {
            for (it.seek(KeyUtils.genSeekKey(seekTimestamp)); it.isValid(); it.next()) {
                final String dMsgId = new String(it.key());
                final InternalKey internalKey = new InternalKey(dMsgId);

                // 是否是墓碑
                if (internalKey.getType() == MsgTypes.TOMBSTONE.getValue()) {
//                    String uniqDelayMsgId = new String(it.value());
//                    String tombstoneKey = dMsgId;
//
//                    needCancelMap.put(uniqDelayMsgId, tombstoneKey);
//                    continue;

                    String value = new String(it.value());
                    if (value.startsWith("{")) {
                        CancelWrap cancelWrap = JsonUtils.fromJsonString(value, CancelWrap.class);
                        String uniqDelayMsgId = cancelWrap.getUniqDelayMsgId();
                        String topic = cancelWrap.getTopic();
                        String tombstoneKey = dMsgId;

                        needCancelMap.put(uniqDelayMsgId, tombstoneKey);
                        needCancelTopicMap.put(uniqDelayMsgId, topic);
                        continue;
                    } else {
                        // TODO: 2018/8/30 这段代码10月30日可以去掉
                        String uniqDelayMsgId = value;
                        String topic = "general";
                        String tombstoneKey = dMsgId;
                        needCancelMap.put(uniqDelayMsgId, tombstoneKey);
                        needCancelTopicMap.put(uniqDelayMsgId, topic);
                        continue;
                    }
                }

                // 是否是尸体
                String uniqDelayMsgId = internalKey.genUniqDelayMsgId();
                if (needCancelMap.containsKey(uniqDelayMsgId)) {
                    // split的消息在最后一个segment时, 从needCancelMap中删除dMsgId
                    if (internalKey.getSegmentNum() == (internalKey.getSegmentIndex() - Constants.SEGMENT_INDEX_BASE + 1)) {
                        needCancelMap.remove(uniqDelayMsgId);
                        needCancelTopicMap.remove(uniqDelayMsgId);
                    }

                    if (internalKey.getSegmentNum() == 0) {
                        needCancelMap.remove(uniqDelayMsgId);
                        needCancelTopicMap.remove(uniqDelayMsgId);
                    }

                    LOGGER.info("pull from db succ cancel message, dMsgId:{}", dMsgId);
                    continue;
                }

                boolean needMetricWriteQpsAfterSplit = false;

                // 循环消息需要插入一条新的消息, 如果失效, 则不再插入
                if (internalKey.getType() == MsgTypes.LOOP_DELAY.getValue()
                        || internalKey.getType() == MsgTypes.LOOP_EXPONENT_DELAY.getValue()) {
                    final InternalKey nextInternalKey = new InternalKey(internalKey).nextUniqDelayMsgId();
                    if (!KeyUtils.isInvalidMsg(nextInternalKey)) {
                        batcher.putToDefaultCF(nextInternalKey.genUniqDelayMsgIdWithSegmentInfoIfHas(), it.value(), null, nextInternalKey, Actions.ADD.getValue());
                        needMetricWriteQpsAfterSplit = true;
                    }
                }

                byte[] bytes = it.value();
                if (internalKey.getSegmentNum() > 0) {
                    try {
                        OUTPUT.write(it.value());
                        LOGGER.info("segment merge, dMsgId:{}, value.len:{}, value.acc.len:{}", internalKey.genUniqDelayMsgIdWithSegmentInfoIfHas(), it.value().length, OUTPUT.size());
                        if (internalKey.getSegmentNum() != (internalKey.getSegmentIndex() - Constants.SEGMENT_INDEX_BASE + 1)) {
                            continue;
                        }
                        bytes = OUTPUT.toByteArray();
                        OUTPUT.reset();
                    } catch (IOException e) {
                        LOGGER.error("error while output.write byte array, msg:{}", e.getMessage(), e);
                    }
                }

                // 如果解析不出来, 说明格式有问题, 抛弃掉该条消息, 不阻塞
                final InternalValue internalValue = JsonUtils.fromJsonString(bytes, InternalValue.class);
                if (internalValue == null) {
                    continue;
                }

                if (internalKey.getSegmentNum() > 0) {
                    for (int i = 0; i < internalKey.getSegmentNum(); i++) {
                        if (needMetricWriteQpsAfterSplit) {
                            if (internalKey.getType() == MsgTypes.LOOP_DELAY.getValue()) {
                                if (i == 0) {
                                    // 合并完只统计一次
                                    MetricService.incWriteQps(internalValue.getTopic(), MetricMsgAction.ADD_INNER, MetricMsgType.LOOP_DELAY, MetricMsgToOrFrom.DB);
                                }
                                MetricService.incWriteQpsAfterSplit(internalValue.getTopic(), MetricMsgAction.ADD_INNER, MetricMsgType.LOOP_DELAY);
                            } else {
                                if (i == 0) {
                                    // 合并完只统计一次
                                    MetricService.incWriteQps(internalValue.getTopic(), MetricMsgAction.ADD_INNER, MetricMsgType.LOOP_EXPONENT_DELAY, MetricMsgToOrFrom.DB);
                                }
                                MetricService.incWriteQpsAfterSplit(internalValue.getTopic(), MetricMsgAction.ADD_INNER, MetricMsgType.LOOP_EXPONENT_DELAY);
                            }
                        }
                        MetricService.incPushQpsBeforeMerge(internalValue.getTopic());
                    }
                } else {
                    if (needMetricWriteQpsAfterSplit) {
                        if (internalKey.getType() == MsgTypes.LOOP_DELAY.getValue()) {
                            MetricService.incWriteQps(internalValue.getTopic(), MetricMsgAction.ADD_INNER, MetricMsgType.LOOP_DELAY, MetricMsgToOrFrom.DB);
                            MetricService.incWriteQpsAfterSplit(internalValue.getTopic(), MetricMsgAction.ADD_INNER, MetricMsgType.LOOP_DELAY);
                        } else {
                            MetricService.incWriteQps(internalValue.getTopic(), MetricMsgAction.ADD_INNER, MetricMsgType.LOOP_EXPONENT_DELAY, MetricMsgToOrFrom.DB);
                            MetricService.incWriteQpsAfterSplit(internalValue.getTopic(), MetricMsgAction.ADD_INNER, MetricMsgType.LOOP_EXPONENT_DELAY);
                        }
                    }
                    MetricService.incPushQpsBeforeMerge(internalValue.getTopic());
                }

                count++;

                try {
                    blockingQueue.put(new InternalPair(internalKey, internalValue));
                } catch (InterruptedException e) {
                    LOGGER.error("error while put to blockingQueue, dMsgId:{}", dMsgId);
                }

                if (count % INTERNAL_PAIR_COUNT == 0) {
                    sendConcurrent(blockingQueue, round);
                }
            }

            sendConcurrent(blockingQueue, round);
        }

        needCancelMap.forEach((uniqDelayMsgId, tombstoneKey) -> {
            final InternalKey internalKey = new InternalKey(uniqDelayMsgId);
            final InternalKey tombstoneInternalKey = new InternalKey(tombstoneKey);

            // 残留的循环消息取消需要重新添加进去, 否则会删除不掉
            if (internalKey.getType() == MsgTypes.LOOP_DELAY.getValue()
                    || internalKey.getType() == MsgTypes.LOOP_EXPONENT_DELAY.getValue()) {
                final InternalKey nextTombstoneKey = tombstoneInternalKey.nextUniqDelayMsgId();
                final InternalKey nextInternalKey = internalKey.nextUniqDelayMsgId();
                if (!KeyUtils.isInvalidMsg(nextTombstoneKey)) {
                    String topic = needCancelTopicMap.get(uniqDelayMsgId);
                    batcher.putToDefaultCF(nextTombstoneKey.genUniqDelayMsgId(),
                            new CancelWrap(nextInternalKey.genUniqDelayMsgId(), topic).toJsonString(),
                            topic, nextInternalKey, Actions.CANCEL.getValue());

                } else {
                    LOGGER.info("pull from db succ cancel message of tombstone key, tombstone dMsgId:{}",
                            nextTombstoneKey.genUniqDelayMsgId());
                }
            }
        });
        batcher.flush();

        needCancelMap.clear();
        needCancelTopicMap.clear();

        // 更新offset
        MetaService.nextSeekTimestamp();

        LOGGER.info("pull from db finish push, pushCost:{}ms, count:{}, seekTimestamp:{}, round:{}",
                System.currentTimeMillis() - start, count, seekTimestamp, round);
    }

    private void sendConcurrent(final BlockingQueue<InternalPair> blockingQueue, final long round) {
        if (blockingQueue.size() == 0) {
            LOGGER.info("pull from db sendConcurrent start, return for no message to send, round:{}", round);
            return;
        }

        final long sendCount = blockingQueue.size();
        LOGGER.info("pull from db sendConcurrent start, send count:{}, round:{}", sendCount, round);
        final long start = System.currentTimeMillis();
        final CountDownLatch cdl = new CountDownLatch(blockingQueue.size());
        InternalPair internalPair;
        while ((internalPair = blockingQueue.poll()) != null) {
            final InternalPair immutableInternalPair = internalPair;
            pushThreadPool.execute(() -> {
                while (!send(
                        immutableInternalPair.getInternalValue().getTopic(),
                        immutableInternalPair.getInternalValue().getBody().getBytes(Charsets.UTF_8),
                        immutableInternalPair.getInternalKey(),
                        immutableInternalPair.getInternalValue().getTags(),
                        immutableInternalPair.getInternalValue().getProperties(),
                        false)) {
                    try {
                        TimeUnit.MILLISECONDS.sleep(100);
                    } catch (InterruptedException e) {
                    }
                }
                cdl.countDown();
            });
        }

        try {
            cdl.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        final long cost = System.currentTimeMillis() - start;
        LOGGER.info("pull from db sendConcurrent end, send count:{}, round:{}, cost:{}ms", sendCount, round, cost);
    }

    public void sendConcurrent(final BlockingQueue<InternalPair> blockingQueue, final String from, final long round) {
        if (blockingQueue.size() == 0) {
            LOGGER.info("pull from {} sendConcurrent start, return for no message to send, round:{}", from, round);
            return;
        }

        final long sendCount = blockingQueue.size();
        LOGGER.info("pull from {} sendConcurrent start, send count:{}, round:{}", from, sendCount, round);
        final long start = System.currentTimeMillis();
        final CountDownLatch cdl = new CountDownLatch(blockingQueue.size());
        InternalPair internalPair;
        while ((internalPair = blockingQueue.poll()) != null) {
            final InternalPair immutableInternalPair = internalPair;
            directPushThreadPool.execute(() -> {
                while (!send(
                        immutableInternalPair.getInternalValue().getTopic(),
                        immutableInternalPair.getInternalValue().getBody().getBytes(Charsets.UTF_8),
                        immutableInternalPair.getInternalKey(),
                        immutableInternalPair.getInternalValue().getTags(),
                        immutableInternalPair.getInternalValue().getProperties(),
                        true)) {
                    try {
                        TimeUnit.MILLISECONDS.sleep(100);
                    } catch (InterruptedException e) {
                    }
                }
                cdl.countDown();
            });
        }

        try {
            cdl.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        final long cost = System.currentTimeMillis() - start;
        LOGGER.info("pull from {} sendConcurrent end, send count:{}, round:{}, cost:{}ms", from, sendCount, round, cost);
    }

    private boolean send(final String topic, final byte[] body, final InternalKey internalKey, final String tags, final Map<String, String> properties,
                        final boolean direct) {
        final long start = System.nanoTime();
        final String key = internalKey.genUniqDelayMsgId();
        MetricMsgType metricMsgType;

        if (internalKey.getType() == MsgTypes.DELAY.getValue()) {
            metricMsgType = MetricMsgType.DELAY;
        } else if (internalKey.getType() == MsgTypes.LOOP_DELAY.getValue()) {
            metricMsgType = MetricMsgType.LOOP_DELAY;
        } else {
            metricMsgType = MetricMsgType.UNKNOWN;
        }

        int len = 0;
        if (body != null) {
            len = body.length;
        }

        if (MasterElection.isBackup()) {
            if (direct) {
                LOGGER.info("succ send message(but cancel for backup) directly, topic:{}, dMsgId:{}, len:{}", topic, key, len);
                MetricService.incPushQps(topic, metricMsgType, MetricMsgToOrFrom.SEND, MetricPushMsgResult.BACKUP);
            } else {
                LOGGER.info("succ send message(but cancel for backup) from db, topic:{}, dMsgId:{}, len:{}", topic, key, len);
                MetricService.incPushQps(topic, metricMsgType, MetricMsgToOrFrom.DB, MetricPushMsgResult.BACKUP);
            }
            return true;
        }

        if (ConfigManager.getConfig().isFakeSend()) {
            try {
                TimeUnit.MILLISECONDS.sleep(1);
            } catch (InterruptedException e) {
            }
            if (direct) {
                LOGGER.info("succ send message directly(fakeSend), topic:{}, dMsgId:{}, len:{}", topic, key, len);
            } else {
                LOGGER.info("succ send message from db(fakeSend), topic:{}, dMsgId:{}, len:{}", topic, key, len);
            }
            return true;
        }

        MessageBuilder messageBuilder = producer.messageBuilder().setTopic(topic).setBody(body).setKey(key).setTags(tags).setRandomPartition();
        if (properties != null && properties.size() > 0) {
            for (Map.Entry<String, String> entry : properties.entrySet()) {
                LOGGER.debug("properties, topic:{}, dMsgId:{}, key:{}, value:{}", topic, key, entry.getKey(), entry.getValue());
                // IMPORTANT: If use addProperty for isPressureTraffic, the property will be ignored
                if (PRESSURE_TRAFFIC_KEY.equals(entry.getKey())) {
                    messageBuilder.setPressureTraffic(Boolean.parseBoolean(entry.getValue()));
                } else {
                    messageBuilder.addProperty(entry.getKey(), entry.getValue());
                }
            }
        }
        messageBuilder.addProperty(PROPERTY_KEY_FROM_CHRONOS, PROPERTY_KEY_FROM_CHRONOS);

        final Result result = messageBuilder.send();
        final long cost = (System.nanoTime() - start) / 1000;
        MetricService.putPushLatency(topic, cost);

        if (result.getCode() == CarreraReturnCode.OK) {
            if (direct) {
                LOGGER.info("succ send message directly, topic:{}, dMsgId:{}, len:{}, result:{}, cost:{}us", topic, key, len, result, cost);
                MetricService.incPushQps(topic, metricMsgType, MetricMsgToOrFrom.SEND, MetricPushMsgResult.OK);
            } else {
                LOGGER.info("succ send message from db, topic:{}, dMsgId:{}, len:{}, result:{}, cost:{}us", topic, key, len, result, cost);
                MetricService.incPushQps(topic, metricMsgType, MetricMsgToOrFrom.DB, MetricPushMsgResult.OK);
            }
            return true;
        } else if (result.getCode() == CarreraReturnCode.FAIL_TOPIC_NOT_EXIST
                || result.getCode() == CarreraReturnCode.FAIL_TOPIC_NOT_ALLOWED
                || result.getCode() == CarreraReturnCode.FAIL_ILLEGAL_MSG
                || result.getCode() == CarreraReturnCode.MISSING_PARAMETERS) {
            if (direct) {
                LOGGER.error("fail send message directly, topic:{}, dMsgId:{}, len:{}, result:{}, cost:{}us", topic, key, len, result, cost);
                MetricService.incPushQps(topic, metricMsgType, MetricMsgToOrFrom.SEND, MetricPushMsgResult.FAIL);
            } else {
                LOGGER.error("fail send message from db, topic:{}, dMsgId:{}, len:{}, result:{}, cost:{}us", topic, key, len, result, cost);
                MetricService.incPushQps(topic, metricMsgType, MetricMsgToOrFrom.DB, MetricPushMsgResult.FAIL);
            }
            return true;
        } else {
            if (direct) {
                LOGGER.error("error while send message directly, topic:{}, dMsgId:{}, len:{}, result:{}, cost:{}us", topic, key, len, result, cost);
                MetricService.incPushQps(topic, metricMsgType, MetricMsgToOrFrom.SEND, MetricPushMsgResult.FAIL);
            } else {
                LOGGER.error("error while send message from db, topic:{}, dMsgId:{}, len:{}, result:{}, cost:{}us", topic, key, len, result, cost);
                MetricService.incPushQps(topic, metricMsgType, MetricMsgToOrFrom.DB, MetricPushMsgResult.FAIL);
            }
            return false;
        }
    }

    private void initProducer() {
        final long start = System.currentTimeMillis();
        final CarreraConfig config = new CarreraConfig();
        final PushConfig pushConfig = ConfigManager.getConfig().getPushConfig();

        config.setCarreraProxyList(pushConfig.getPproxyAddrs());
        config.setCarreraProxyTimeout(pushConfig.getProxyTimeoutMs());
        config.setCarreraClientRetry(pushConfig.getClientRetry());
        config.setCarreraClientTimeout(pushConfig.getClientTimeoutMs());
        config.setCarreraPoolSize(pushConfig.getPoolSize());
        config.setBatchSendThreadNumber(pushConfig.getBatchSendThreadNum());

        producer = new CarreraProducer(config);

        try {
            producer.start();
            LOGGER.info("succ start producer, cost:{}ms, pushConfig:{}", System.currentTimeMillis() - start, pushConfig);
        } catch (Exception e) {
            LOGGER.error("error while start producer, pushConfig:{}, err:{}", pushConfig, e.getMessage(), e);
        }
    }

    public void stop() {
        /* if master, upload seekTimestamp before stop */
        if (MasterElection.isMaster()) {
            MetaService.uploadSeekTimestampToZk();
        }

        int times = 0;
        pushThreadPool.shutdown();
        while (!pushThreadPool.isShutdown()) {
            LOGGER.info("pushThreadPool is shutting down..., times={}", times);
            times++;
            try {
                TimeUnit.MILLISECONDS.sleep(100);
            } catch (InterruptedException e) {
                LOGGER.error("error while sleep for check pushThreadPool's shutdown, err:{}", e.getMessage(), e);
            }
        }
        LOGGER.info("pushThreadPool has shut down");

        times = 0;
        directPushThreadPool.shutdown();
        while (!directPushThreadPool.isShutdown()) {
            LOGGER.info("directPushThreadPool is shutting down..., times={}", times);
            times++;
            try {
                TimeUnit.MILLISECONDS.sleep(100);
            } catch (InterruptedException e) {
                LOGGER.error("error while sleep for check directPushThreadPool's shutdown, err:{}", e.getMessage(), e);
            }
        }
        LOGGER.info("directPushThreadPool has shut down");

        if (producer != null) {
            producer.shutdown();
            LOGGER.info("succ shutdown producer");
        }
    }

    public static MqPushService getInstance() {
        if (instance == null) {
            synchronized (MqPushService.class) {
                if (instance == null) {
                    instance = new MqPushService();
                }
            }
        }
        return instance;
    }
}