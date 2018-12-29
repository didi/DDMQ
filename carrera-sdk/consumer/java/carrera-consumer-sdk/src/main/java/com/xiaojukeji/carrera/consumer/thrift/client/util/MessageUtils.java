package com.xiaojukeji.carrera.consumer.thrift.client.util;

import com.xiaojukeji.carrera.consumer.thrift.Message;
import com.xiaojukeji.carrera.consumer.thrift.consumerProxyConstants;


public class MessageUtils {

    public static boolean isPressureTraffic(Message message) {
        if (message == null || message.getProperties() == null) {
            return false;
        }

        if (message.properties.containsKey(consumerProxyConstants.PRESSURE_TRAFFIC_KEY)) {
            return Boolean.valueOf(message.properties.get(consumerProxyConstants.PRESSURE_TRAFFIC_KEY));
        }

        return false;
    }

}