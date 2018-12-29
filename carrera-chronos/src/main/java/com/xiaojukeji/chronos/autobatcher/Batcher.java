package com.xiaojukeji.chronos.autobatcher;

import com.google.common.base.Charsets;
import com.xiaojukeji.chronos.config.ConfigManager;
import com.xiaojukeji.chronos.db.CFManager;
import com.xiaojukeji.chronos.db.RDB;
import com.xiaojukeji.chronos.metrics.MetricMsgAction;
import com.xiaojukeji.chronos.metrics.MetricMsgToOrFrom;
import com.xiaojukeji.chronos.metrics.MetricMsgType;
import com.xiaojukeji.chronos.metrics.MetricService;
import com.xiaojukeji.chronos.model.CancelWrap;
import com.xiaojukeji.chronos.utils.ByteUtils;
import com.xiaojukeji.chronos.utils.Constants;
import com.xiaojukeji.chronos.utils.KeyUtils;
import com.xiaojukeji.carrera.chronos.enums.Actions;
import com.xiaojukeji.carrera.chronos.enums.MsgTypes;
import com.xiaojukeji.carrera.chronos.model.InternalKey;
import org.rocksdb.ColumnFamilyHandle;
import org.rocksdb.WriteBatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;


public class Batcher {
    private static final Logger LOGGER = LoggerFactory.getLogger(Batcher.class);

    private static final int PULL_BATCH_ITEM_NUM = ConfigManager.getConfig().getPullConfig().getPullBatchItemNum();
    private static final int MSG_BYTE_BASE_LEN = ConfigManager.getConfig().getPullConfig().getMsgByteBaseLen();

    private WriteBatch wb = new WriteBatch();
    private volatile int itemNum = 0;
    private static volatile Batcher instance = null;

    public static volatile ReentrantLock lock = new ReentrantLock();

    public static Batcher getInstance() {
        if (instance == null) {
            synchronized (Batcher.class) {
                instance = new Batcher();
            }
        }
        return instance;
    }

    private void checkFrequency() {
        if (itemNum >= PULL_BATCH_ITEM_NUM) {
            flush();
        }
    }

     public void flush() {
        lock.lock();
        try {
            if (itemNum > 0) {
                // make sure write succ
                while (!RDB.writeSync(wb)) {
                    LOGGER.error("error while flush to db!");
                    try {
                        TimeUnit.MILLISECONDS.sleep(200);
                    } catch (InterruptedException e) {
                    }
                }
                wb.clear();
                itemNum = 0;
            }
        } finally {
            lock.unlock();
        }
    }

    private void put(final ColumnFamilyHandle cfh, final byte[] key, final byte[] value, final String topic, final InternalKey internalKey, final int action) {
        lock.lock();
        try {
            int len = 0;
            if (value != null) {
                len = value.length;
            }
            wb.put(cfh, key, value);

            LOGGER.info("put to cf, dMsgId:{}, len:{}", new String(key), len);

            itemNum++;
            checkFrequency();

            if (action == Actions.ADD.getValue()) {
                if (internalKey.getType() == MsgTypes.DELAY.getValue()) {
                    MetricService.incWriteQpsAfterSplit(topic, MetricMsgAction.ADD, MetricMsgType.DELAY);
                } else if (internalKey.getType() == MsgTypes.LOOP_DELAY.getValue()) {
                    MetricService.incWriteQpsAfterSplit(topic, MetricMsgAction.ADD, MetricMsgType.LOOP_DELAY);
                } else if (internalKey.getType() == MsgTypes.LOOP_EXPONENT_DELAY.getValue()) {
                    MetricService.incWriteQpsAfterSplit(topic, MetricMsgAction.ADD, MetricMsgType.LOOP_EXPONENT_DELAY);
                }
            } else {
                if (internalKey.getType() == MsgTypes.DELAY.getValue()) {
                    MetricService.incWriteQps(topic, MetricMsgAction.CANCEL, MetricMsgType.DELAY, MetricMsgToOrFrom.DB);
                    MetricService.incWriteQpsAfterSplit(topic, MetricMsgAction.CANCEL, MetricMsgType.DELAY);
                } else if (internalKey.getType() == MsgTypes.LOOP_DELAY.getValue()) {
                    MetricService.incWriteQps(topic, MetricMsgAction.CANCEL, MetricMsgType.LOOP_DELAY, MetricMsgToOrFrom.DB);
                    MetricService.incWriteQpsAfterSplit(topic, MetricMsgAction.CANCEL, MetricMsgType.LOOP_DELAY);
                } else if (internalKey.getType() == MsgTypes.LOOP_EXPONENT_DELAY.getValue()) {
                    MetricService.incWriteQps(topic, MetricMsgAction.CANCEL, MetricMsgType.LOOP_EXPONENT_DELAY, MetricMsgToOrFrom.DB);
                    MetricService.incWriteQpsAfterSplit(topic, MetricMsgAction.CANCEL, MetricMsgType.LOOP_EXPONENT_DELAY);
                }
            }
        } finally {
            lock.unlock();
        }
    }

    public void putToDefaultCF(final String key, final String val, final String topic, final InternalKey internalKey, final int action) {
        put(CFManager.CFH_DEFAULT, key.getBytes(Charsets.UTF_8), val.getBytes(Charsets.UTF_8), topic, internalKey, action);
    }

    public void putToDefaultCF(final String key, final byte[] value, final String topic, final InternalKey internalKey, final int action) {
        put(CFManager.CFH_DEFAULT, key.getBytes(Charsets.UTF_8), value, topic, internalKey, action);
    }

    public boolean checkAndPutToDefaultCF(final InternalKey internalKey, final String strVal, final String topic, final int action) {
        lock.lock();
        try {
            if (KeyUtils.afterSeekTimestamp(internalKey.getTimestamp())) {
                byte[] bytes = strVal.getBytes(Charsets.UTF_8);
                MetricService.putMsgSizePercent(topic, bytes.length);
                if (bytes.length <= MSG_BYTE_BASE_LEN) {
                    putToDefaultCF(internalKey.genUniqDelayMsgId(), bytes, topic, internalKey, action);
                } else {
                    // 如果字节数据超过一定长度, 则进行字节数组切分, 以便降低io.util
                    List<byte[]> list = ByteUtils.divideArray(bytes, MSG_BYTE_BASE_LEN);
                    final int segmentNum = list.size();
                    for (int segmentIndex = 0; segmentIndex < segmentNum; segmentIndex++) {
                        internalKey.setSegmentNum(segmentNum);
                        internalKey.setSegmentIndex(Constants.SEGMENT_INDEX_BASE + segmentIndex);
                        putToDefaultCF(internalKey.genUniqDelayMsgIdWithSegmentInfoIfHas(), list.get(segmentIndex), topic, internalKey, action);
                        LOGGER.info("segment split, dMsgId:{}, len:{}, value.totalLen:{}", internalKey.genUniqDelayMsgIdWithSegmentInfoIfHas(), list.get(segmentIndex).length, bytes.length);
                    }
                }
                return true;
            }
            return false;
        } finally {
            lock.unlock();
        }
    }

    public void putLoopTombstoneKey(final InternalKey tombstoneInternalKey, InternalKey internalKey, final String topic, final int action) {
        lock.lock();
        try {
            // 指数循环
            // 1536811267-4-1536911267-3-0-300-0-9e7952e0-b709-11e8-a709-aafb4bfc0bc5
            // 1536811567-4-1536911267-3-1-300-0-9e7952e0-b709-11e8-a709-aafb4bfc0bc5
            // 1536897967-4-1536911267-3-2-300-0-9e7952e0-b709-11e8-a709-aafb4bfc0bc5

            // 普通循环
            // 1536811267-3-1536911267-3-0-10-0-9e7952e0-b709-11e8-a709-aafb4bfc0bc5
            while (!KeyUtils.afterSeekTimestamp(internalKey.getTimestamp())) {
                internalKey = internalKey.nextUniqDelayMsgId();
            }

            tombstoneInternalKey.setTimestamp(internalKey.getTimestamp());
            tombstoneInternalKey.setTimes(internalKey.getTimed() + 2);
            tombstoneInternalKey.setTimed(internalKey.getTimed());

            if (!KeyUtils.isInvalidMsg(tombstoneInternalKey)) {
                putToDefaultCF(tombstoneInternalKey.genUniqDelayMsgId(),
                        new CancelWrap(internalKey.genUniqDelayMsgId(), topic).toJsonString(), topic, internalKey, action);
            }
        } finally {
            lock.unlock();
        }
    }

    public void close() {
        wb.close();
    }
}