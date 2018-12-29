package com.didi.carrera.console.service;

import com.didi.carrera.console.data.Message;
import org.apache.rocketmq.common.admin.TopicStatsTable;
import org.apache.rocketmq.common.protocol.body.ClusterInfo;

import java.util.Date;


public interface RmqAdminService {

    void createTopic(Long clusterId, String topic) throws Exception;

    Message queryLatestMessage(String nameServer, String topic) throws Exception;

    TopicStatsTable queryTopicConsumeState(String nameServer, String topic) throws Exception;

    void resetOffsetToLatest(String nameServer, String group, String topic) throws Exception;

    void resetOffsetByTime(String nameServer, String group, String topic, Date date) throws Exception;

    ClusterInfo examineBrokerClusterInfo(String nameServer) throws Exception;

    String getClusterName(String nameServer) throws Exception;
}