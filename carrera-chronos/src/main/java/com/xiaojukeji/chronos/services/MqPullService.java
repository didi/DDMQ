package com.xiaojukeji.chronos.services;

import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.xiaojukeji.chronos.autobatcher.Batcher;
import com.xiaojukeji.chronos.config.ConfigManager;
import com.xiaojukeji.chronos.config.PullConfig;
import com.xiaojukeji.chronos.ha.MasterElection;
import com.xiaojukeji.chronos.metrics.MetricMsgAction;
import com.xiaojukeji.chronos.metrics.MetricMsgToOrFrom;
import com.xiaojukeji.chronos.metrics.MetricMsgType;
import com.xiaojukeji.chronos.metrics.MetricPullMsgResult;
import com.xiaojukeji.chronos.metrics.MetricService;
import com.xiaojukeji.chronos.metrics.MetricWriteMsgResult;
import com.xiaojukeji.chronos.model.CancelWrap;
import com.xiaojukeji.chronos.model.InternalPair;
import com.xiaojukeji.chronos.model.InternalValue;
import com.xiaojukeji.chronos.utils.JsonUtils;
import com.xiaojukeji.chronos.utils.KeyUtils;
import com.xiaojukeji.carrera.chronos.enums.Actions;
import com.xiaojukeji.carrera.chronos.enums.MsgTypes;
import com.xiaojukeji.carrera.chronos.model.BodyExt;
import com.xiaojukeji.carrera.chronos.model.InternalKey;
import com.xiaojukeji.carrera.consumer.thrift.Message;
import com.xiaojukeji.carrera.consumer.thrift.PullResponse;
import com.xiaojukeji.carrera.consumer.thrift.client.CarreraConfig;
import com.xiaojukeji.carrera.consumer.thrift.client.SimpleCarreraConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;


public class MqPullService implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(MqPullService.class);

    private static final PullConfig PULL_CONFIG = ConfigManager.getConfig().getPullConfig();
    private static final Batcher BATCHER = Batcher.getInstance();
    private volatile boolean shouldStop = false;
    private CountDownLatch cdl;

    private final List<Long> succOffsets = new ArrayList<>();
    private final List<Long> failOffsets = new ArrayList<>();
    private SimpleCarreraConsumer carreraConsumer;
    private String mqPullServiceName;

    private final int INTERNAL_PAIR_COUNT = 5000;
    private final BlockingQueue<InternalPair> blockingQueue = new ArrayBlockingQueue<>(INTERNAL_PAIR_COUNT);

    public MqPullService(final String server, final int index) {
        this.mqPullServiceName = Joiner.on("-").join("mqPullServiceName", index);

        CarreraConfig carreraConfig = new CarreraConfig();
        carreraConfig.setServers(server);
        carreraConfig.setGroupId(PULL_CONFIG.getInnerGroup());
        carreraConfig.setRetryInterval(PULL_CONFIG.getRetryIntervalMs());
        carreraConfig.setTimeout(PULL_CONFIG.getTimeoutMs());
        carreraConfig.setMaxBatchSize(PULL_CONFIG.getMaxBatchSize());

        carreraConsumer = new SimpleCarreraConsumer(carreraConfig);
        LOGGER.info("{} init carrera consumer, carreraConfig:{}", mqPullServiceName, carreraConfig);
    }

    @Override
    public void run() {
        long round = 0L;
        while (!shouldStop) {
            round++;

            final PullResponse response = carreraConsumer.pullMessage();
            if (response == null || response.getContext() == null || response.getMessagesSize() == 0) {
                try {
                    TimeUnit.MILLISECONDS.sleep(PULL_CONFIG.getRetryIntervalMs());
                } catch (InterruptedException e) {}
                continue;
            }

            addOrCancelMessages(response, succOffsets, failOffsets, round);

            for (Long offset : succOffsets) {
                carreraConsumer.ack(response.getContext(), offset);
            }

            for (Long offset : failOffsets) {
                carreraConsumer.fail(response.getContext(), offset);
            }
        }
        carreraConsumer.stop();
        cdl.countDown();
    }

    public void start() {
        new Thread(this).start();
    }

    private void addOrCancelMessages(PullResponse response, final List<Long> succOffsets, final List<Long> failOffsets, final long round) {
        succOffsets.clear();
        failOffsets.clear();

        for (Message msg : response.getMessages()) {
            if (MasterElection.isBackup()) {
                final long zkQidOffset = MetaService.getZkQidOffsets().getOrDefault(response.getContext().getQid(), 0L);
                if (msg.getOffset() > zkQidOffset) {
                    failOffsets.add(msg.getOffset());
                    continue;
                }
            }

            String jsonString = null;
            BodyExt bodyExt = null;
            try {
                jsonString = new String(msg.getValue());

                // 如果json解析不出来, 说明格式有问题
                bodyExt = JsonUtils.fromJsonString(jsonString, BodyExt.class);
                if (bodyExt == null) {
                    LOGGER.error("error while process message, msg.key:{}, msg.value:{}", msg.getKey(), jsonString);
                    succOffsets.add(msg.getOffset());

                    // 上报拉取到的非法消息
                    MetricService.incPullQps("unknown", MetricMsgAction.UNKNOWN, MetricMsgType.UNKNOWN, MetricPullMsgResult.INVALID);

                    continue;
                }

                final InternalKey internalKey = new InternalKey(bodyExt.getUniqDelayMsgId());
                final InternalValue internalValue = new InternalValue(bodyExt);

                if (bodyExt.getAction() == Actions.ADD.getValue()) {
                    addMessage(internalKey, internalValue, bodyExt.getAction());
                }

                if (bodyExt.getAction() == Actions.CANCEL.getValue()) {
                    cancelMessage(internalKey, bodyExt.getTopic(), bodyExt.getAction());
                }

                if (blockingQueue.size() != 0 && blockingQueue.size() % INTERNAL_PAIR_COUNT == 0) {
                    MqPushService.getInstance().sendConcurrent(blockingQueue, this.mqPullServiceName, round);
                }

                succOffsets.add(msg.getOffset());
            } catch (Exception e) {
                LOGGER.error("error while addOrCancelMessages, jsonString:{}, err:{}", jsonString, e.getMessage(), e);
                succOffsets.add(msg.getOffset());

                MetricService.incWriteQps(bodyExt.getTopic(), MetricMsgAction.UNKNOWN, MetricMsgType.UNKNOWN,
                        MetricMsgToOrFrom.UNKNOWN, MetricWriteMsgResult.FAIL);
            }
        }
        BATCHER.flush();

        if (blockingQueue.size() != 0) {
            MqPushService.getInstance().sendConcurrent(blockingQueue, this.mqPullServiceName, round);
        }
    }

    /**
     * 添加消息
     *
     * @param internalKey
     * @param internalValue
     */
    private void addMessage(final InternalKey internalKey, final InternalValue internalValue, final int action) {
        if (internalKey.getType() == MsgTypes.DELAY.getValue()) {
            MetricService.incPullQps(internalValue.getTopic(), MetricMsgAction.ADD, MetricMsgType.DELAY);

            if (BATCHER.checkAndPutToDefaultCF(internalKey, internalValue.toJsonString(), internalValue.getTopic(), action)) {
                MetricService.incWriteQps(internalValue.getTopic(), MetricMsgAction.ADD, MetricMsgType.DELAY, MetricMsgToOrFrom.DB);

                return;
            }

            MetricService.putMsgSizePercent(internalValue.getTopic(), internalValue.toJsonString().getBytes(Charsets.UTF_8).length);
            MetricService.incWriteQps(internalValue.getTopic(), MetricMsgAction.ADD, MetricMsgType.DELAY, MetricMsgToOrFrom.SEND);

            putToBlockingQueue(internalKey, internalValue);
        } else if (internalKey.getType() == MsgTypes.LOOP_DELAY.getValue()
                || internalKey.getType() == MsgTypes.LOOP_EXPONENT_DELAY.getValue()) {

            MetricMsgType msgType = MetricMsgType.LOOP_DELAY;
            if (internalKey.getType() == MsgTypes.LOOP_EXPONENT_DELAY.getValue()) {
                msgType = MetricMsgType.LOOP_EXPONENT_DELAY;
            }

            MetricService.incPullQps(internalValue.getTopic(), MetricMsgAction.ADD, msgType);

            while (true) {
                if (KeyUtils.isInvalidMsg(internalKey)) {
                    return;
                }

                // 循环消息只写入rocksdb一次, seek到的时候再进行添加
                if (BATCHER.checkAndPutToDefaultCF(internalKey, internalValue.toJsonString(), internalValue.getTopic(), action)) {
                    MetricService.incWriteQps(internalValue.getTopic(), MetricMsgAction.ADD, msgType, MetricMsgToOrFrom.DB);
                    return;
                }

                MetricService.incWriteQps(internalValue.getTopic(), MetricMsgAction.ADD, msgType, MetricMsgToOrFrom.SEND);

                putToBlockingQueue(new InternalKey(internalKey), internalValue);
                internalKey.nextUniqDelayMsgId();
            }
        } else {
            MetricService.incPullQps(internalValue.getTopic(), MetricMsgAction.ADD, MetricMsgType.UNKNOWN);

            LOGGER.error("should not go here, invalid message type:{}, internalKey:{}", internalKey.getType(),
                    internalKey.genUniqDelayMsgId());
        }
    }

    /**
     * 取消消息
     *
     * @param internalKey
     */
    private void cancelMessage(final InternalKey internalKey, final String topic, final int action) {
        InternalKey tombStoneInternalKey = internalKey.cloneTombstoneInternalKey();

        if (internalKey.getType() == MsgTypes.DELAY.getValue()) {
            MetricService.incPullQps(topic, MetricMsgAction.CANCEL, MetricMsgType.DELAY);

            BATCHER.putToDefaultCF(tombStoneInternalKey.genUniqDelayMsgId(),
                    new CancelWrap(internalKey.genUniqDelayMsgId(), topic).toJsonString(), topic, tombStoneInternalKey, action);
        } else if (internalKey.getType() == MsgTypes.LOOP_DELAY.getValue()) {
            MetricService.incPullQps(topic, MetricMsgAction.CANCEL, MetricMsgType.LOOP_DELAY);

            BATCHER.putLoopTombstoneKey(tombStoneInternalKey, internalKey, topic, action);
        } else if (internalKey.getType() == MsgTypes.LOOP_EXPONENT_DELAY.getValue()) {
            MetricService.incPullQps(topic, MetricMsgAction.CANCEL, MetricMsgType.LOOP_EXPONENT_DELAY);

            BATCHER.putLoopTombstoneKey(tombStoneInternalKey, internalKey, topic, action);
        } else {
            MetricService.incPullQps(topic, MetricMsgAction.CANCEL, MetricMsgType.UNKNOWN);

            LOGGER.error("should not go here, invalid message type: {}, internalKey: {}", internalKey.getType(),
                    internalKey.genUniqDelayMsgId());
        }
    }

    private void putToBlockingQueue(InternalKey internalKey, InternalValue internalValue) {
        try {
            blockingQueue.put(new InternalPair(internalKey, internalValue));
        } catch (InterruptedException e) {
            LOGGER.error("error while put to blockingQueue, dMsgId:{}", internalKey.genUniqDelayMsgId());
        }
    }

    public void stop() {
        final long start = System.currentTimeMillis();
        LOGGER.info("carrera consumer will stop ...");
        cdl = new CountDownLatch(1);
        shouldStop = true;
        while (!carreraConsumer.isStopped()) {
            try {
                TimeUnit.MILLISECONDS.sleep(10);
            } catch (InterruptedException e) {
            }
        }
        try {
            cdl.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        LOGGER.info("{} carrera consumer has stopped, cost:{}ms", mqPullServiceName, System.currentTimeMillis() - start);
    }
}