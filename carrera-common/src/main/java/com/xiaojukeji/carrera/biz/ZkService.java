package com.xiaojukeji.carrera.biz;

import com.xiaojukeji.carrera.config.v4.*;
import com.xiaojukeji.carrera.dynamic.ParameterDynamicZookeeper;

import java.util.List;


public interface ZkService {

    String ZK_ROOT = "/carrera/v4/config";
    String CARRERA_TOPIC = "/topic";
    String CARRERA_GROUP = "/group";
    String CARRERA_PPROXY = "/pproxy";
    String CARRERA_CPROXY = "/cproxy";
    String CARRERA_BROKER = "/broker";
    String CARRERA_MONITHOR_HOST = "/monitor/host";
    String CARRERA_MONITOR_ASSIGNED = "/monitor/assigned";

    void shutdown();

    void getAndWatchTopic(ParameterDynamicZookeeper.DataChangeCallback<TopicConfig> callback) throws Exception;

    void getAndWatchTopic(String topic, ParameterDynamicZookeeper.DataChangeCallback<TopicConfig> callback) throws Exception;

    void removeTopicWatch(String topic, ParameterDynamicZookeeper.DataChangeCallback<TopicConfig> callback);

    void getAndWatchGroup(ParameterDynamicZookeeper.DataChangeCallback<GroupConfig> callback) throws Exception;

    void getAndWatchGroup(String group, ParameterDynamicZookeeper.DataChangeCallback<GroupConfig> callback) throws Exception;

    void removeGroupWatch(String group, ParameterDynamicZookeeper.DataChangeCallback<GroupConfig> callback);

    void getAndWatchPProxy(String instance, ParameterDynamicZookeeper.DataChangeCallback<PProxyConfig> callback) throws Exception;

    void getAndWatchCProxy(String instance, ParameterDynamicZookeeper.DataChangeCallback<CProxyConfig> callback) throws Exception;

    void getAndWatchBroker(String broker, ParameterDynamicZookeeper.DataChangeCallback<BrokerConfig> callback) throws Exception;

    void getAndWatchBroker(ParameterDynamicZookeeper.DataChangeCallback<BrokerConfig> callback) throws Exception;

    TopicConfig getTopic(String topic);

    GroupConfig getGroup(String group);

    PProxyConfig getPProxy(String instance);

    CProxyConfig getCProxy(String instance);

    boolean createOrUpdateTopic(TopicConfig config) throws Exception;

    boolean deleteTopic(String topic);

    boolean createOrUpdateGroup(GroupConfig config) throws Exception;

    boolean deleteGroup(String group);

    boolean createOrUpdatePProxy(PProxyConfig config) throws Exception;

    boolean deletePProxy(String instance);

    boolean createOrUpdateCProxy(CProxyConfig config) throws Exception;

    boolean deleteCProxy(String instance);

    boolean createOrUpdateBroker(BrokerConfig config) throws Exception;

    boolean deleteBroker(String brokerCluster);

    List<String> getChildren(String path);

}