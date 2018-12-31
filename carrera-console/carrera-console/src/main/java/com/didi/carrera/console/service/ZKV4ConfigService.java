package com.didi.carrera.console.service;

import java.util.Set;

import com.didi.carrera.console.dao.model.ConsumeSubscription;
import com.didi.carrera.console.service.exception.ZkConfigException;
import com.didi.carrera.console.web.ConsoleBaseResponse;
import com.xiaojukeji.carrera.config.v4.GroupConfig;
import com.xiaojukeji.carrera.config.v4.cproxy.UpstreamTopic;


public interface ZKV4ConfigService {
    void updateTopicConfig(Long topicId, Set<Long> clusterIdSet) throws Exception;

    void updateSubConfig(Long groupId, Set<Long> clusterIdSet) throws Exception;

    void onlyUpdateGroupConfig(Long groupId) throws Exception;

    void updatePProxyConfig(Long nodeId) throws Exception;

    void updateCProxyConfig(Long nodeId) throws Exception;

    void updateBrokerConfig(Long mqServerId) throws Exception;

    void initAllZk() throws Exception;

    void initZkPath() throws Exception;

    ConsoleBaseResponse<?> pushCproxyConfig(String host) throws Exception;

    ConsoleBaseResponse<?> pushPproxyConfig(String host) throws Exception;

    ConsoleBaseResponse<?> pushTopicConfig(String topicName) throws Exception;

    ConsoleBaseResponse<?> pushGroupConfig(String groupName) throws Exception;

    ConsoleBaseResponse<?> pushTopicByCluster(String clusterName) throws Exception;

    ConsoleBaseResponse<?> pushGroupByCluster(String clusterName) throws Exception;

    ConsoleBaseResponse<?> pushPProxyByCluster(String clusterName) throws Exception;

    ConsoleBaseResponse<?> pushCProxyByCluster(String clusterName) throws Exception;

    UpstreamTopic buildUpstreamTopic(GroupConfig groupConfig, ConsumeSubscription subscription, String brokerCluster) throws ZkConfigException;

    void updatePProxyConfigByClusterId(String topic, Set<Long> clusterIdSet) throws Exception;
    void updateCProxyConfigByClusterId(String group, Set<Long> clusterIdSet) throws Exception;
}