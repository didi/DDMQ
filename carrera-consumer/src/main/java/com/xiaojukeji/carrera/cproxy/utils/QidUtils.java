package com.xiaojukeji.carrera.cproxy.utils;


public class QidUtils {

    public static int getKafkaQid(String clusterName, String carreraQid) {
        String prefix = clusterName + "_";
        return Integer.parseInt(carreraQid.substring(prefix.length(), carreraQid.length()));
    }

    public static String kafkaMakeQid(String clusterName, int partition) {
        return String.format("%s_%d", clusterName, partition);
    }

    public static String kafkaMakeQid(String clusterName, String partition) {
        return String.format("%s_%s", clusterName, partition);
    }

    public static String rmqMakeQid(String clusterName, String brokerName, int queueId) {
        return String.format("%s_%s_%d", clusterName, brokerName, queueId);
    }
}