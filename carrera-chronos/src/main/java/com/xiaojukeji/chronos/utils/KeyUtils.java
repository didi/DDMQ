package com.xiaojukeji.chronos.utils;

import com.google.common.base.Charsets;
import com.xiaojukeji.chronos.services.MetaService;
import com.xiaojukeji.carrera.chronos.enums.MsgTypes;
import com.xiaojukeji.carrera.chronos.model.InternalKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class KeyUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(KeyUtils.class);

    public static byte[] genSeekKey(final long seekOffset) {
        return String.valueOf(seekOffset).getBytes(Charsets.UTF_8);
    }

    /**
     * 循环消息需要检查过期时间和回调次数
     */
    public static boolean isInvalidMsg(final InternalKey internalKey) {
        if (internalKey.getType() == MsgTypes.LOOP_DELAY.getValue()
                || internalKey.getType() == MsgTypes.LOOP_EXPONENT_DELAY.getValue()
                || internalKey.getType() == MsgTypes.TOMBSTONE.getValue()) {
            if (internalKey.getExpire() < internalKey.getTimestamp()) {
                LOGGER.info("delay msg is invalid for expire, delayMsgId:{}, expire:{}, seekTimestamp:{}",
                        internalKey.genUniqDelayMsgIdWithSegmentInfoIfHas(), internalKey.getExpire(), MetaService.getSeekTimestamp());
                return true;
            }

            if (internalKey.getTimes() <= internalKey.getTimed()) {
                LOGGER.info("delay msg is invalid for exceed times, delayMsgId:{}, times:{}, timed:{}",
                        internalKey.genUniqDelayMsgIdWithSegmentInfoIfHas(), internalKey.getTimes(), internalKey.getTimed());
                return true;
            }
        }
        return false;
    }

    public static boolean afterSeekTimestamp(final long timestamp) {
        return timestamp > MetaService.getSeekTimestamp();
    }
}