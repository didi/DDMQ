package com.xiaojukeji.carrera.pproxy.utils;

import org.apache.commons.collections.MapUtils;
import org.apache.rocketmq.common.message.MessageConst;

import java.util.Map;


public class MsgCheckUtils {

    public static String checkProperties(Map<String, String> properties) {
        if (MapUtils.isNotEmpty(properties)) {
            for (Map.Entry<String, String> entry : properties.entrySet()) {
                if (MessageConst.STRING_HASH_SET.contains(entry.getKey()) || entry.getKey().trim().isEmpty()) {
                    return entry.getKey();
                }
                if (entry.getValue() == null || entry.getValue().trim().isEmpty()) {
                    return entry.getKey();
                }
            }
        }

        return null;
    }
}