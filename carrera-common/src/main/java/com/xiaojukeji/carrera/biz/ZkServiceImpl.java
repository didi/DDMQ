package com.xiaojukeji.carrera.biz;

import com.google.common.collect.Lists;
import com.xiaojukeji.carrera.config.v4.BrokerConfig;
import com.xiaojukeji.carrera.config.v4.CProxyConfig;
import com.xiaojukeji.carrera.config.v4.GroupConfig;
import com.xiaojukeji.carrera.config.v4.HostRegionConfig;
import com.xiaojukeji.carrera.config.v4.MonitorAssignedConfig;
import com.xiaojukeji.carrera.config.v4.PProxyConfig;
import com.xiaojukeji.carrera.config.v4.TopicConfig;
import com.xiaojukeji.carrera.dynamic.ParameterDynamicConfig;
import com.xiaojukeji.carrera.dynamic.ParameterDynamicZookeeper;
import com.xiaojukeji.carrera.utils.CommonFastJsonUtils;
import org.I0Itec.zkclient.exception.ZkNoNodeException;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.List;


public class ZkServiceImpl implements ZkService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ZkServiceImpl.class);

    private ParameterDynamicZookeeper zkService;

    /**
     * @param zkHost         ip:port,ip:port
     * @param isConfigCentre
     * @throws Exception
     */
    public ZkServiceImpl(String zkHost, boolean isConfigCentre) throws Exception {
        if (!zkHost.contains(ZK_ROOT)) {
            zkHost = zkHost + ZK_ROOT;
        }
        ParameterDynamicConfig config = new ParameterDynamicConfig(zkHost);
        config.setConfigCentre(isConfigCentre);
        try {
            zkService = new ParameterDynamicZookeeper(config);
        } catch (Exception e) {
            LOGGER.error("zkService init exception, zkHost={}", zkHost, e);
            throw e;
        }
    }

    @Override
    public void shutdown() {
        zkService.shutdown();
    }

    @Override
    public void getAndWatchTopic(ParameterDynamicZookeeper.DataChangeCallback<TopicConfig> callback) throws Exception {
        zkService.recursiveWatch(CARRERA_TOPIC, callback, TopicConfig.class);
    }

    @Override
    public void getAndWatchTopic(String topic, ParameterDynamicZookeeper.DataChangeCallback<TopicConfig> callback) throws Exception {
        zkService.getAndWatch(getTopicPath(topic), callback, TopicConfig.class);
    }

    @Override
    public void removeTopicWatch(String topic, ParameterDynamicZookeeper.DataChangeCallback<TopicConfig> callback) {
        zkService.removeWatch(getTopicPath(topic), callback, TopicConfig.class);
    }

    @Override
    public void getAndWatchGroup(ParameterDynamicZookeeper.DataChangeCallback<GroupConfig> callback) throws Exception {
        zkService.recursiveWatch(CARRERA_GROUP, callback, GroupConfig.class);
    }

    @Override
    public void getAndWatchGroup(String group, ParameterDynamicZookeeper.DataChangeCallback<GroupConfig> callback) throws Exception {
        zkService.getAndWatch(getGroupPath(group), callback, GroupConfig.class);
    }

    @Override
    public void removeGroupWatch(String group, ParameterDynamicZookeeper.DataChangeCallback<GroupConfig> callback) {
        zkService.removeWatch(getGroupPath(group), callback, GroupConfig.class);
    }

    @Override
    public void getAndWatchPProxy(String instance, ParameterDynamicZookeeper.DataChangeCallback<PProxyConfig> callback) throws Exception {
        zkService.getAndWatch(getProxyPath(CARRERA_PPROXY, instance), callback, PProxyConfig.class);
    }

    @Override
    public void getAndWatchCProxy(String instance, ParameterDynamicZookeeper.DataChangeCallback<CProxyConfig> callback) throws Exception {
        zkService.getAndWatch(getProxyPath(CARRERA_CPROXY, instance), callback, CProxyConfig.class);
    }

    @Override
    public void getAndWatchBroker(String broker, ParameterDynamicZookeeper.DataChangeCallback<BrokerConfig> callback) throws Exception {
        zkService.getAndWatch(getBrokerPath(broker), callback, BrokerConfig.class);
    }

    @Override
    public void getAndWatchBroker(ParameterDynamicZookeeper.DataChangeCallback<BrokerConfig> callback) throws Exception {
        zkService.recursiveWatch(CARRERA_BROKER, callback, BrokerConfig.class);
    }

    @Override
    public List<TopicConfig> getAllTopic() {
        List<TopicConfig> allTopic = Lists.newArrayList();
        List<String> topicList = zkService.getChildren(CARRERA_TOPIC);
        if (CollectionUtils.isEmpty(topicList)) {
            return allTopic;
        }
        for (String topicName : topicList) {
            allTopic.add(getZkData(getTopicPath(topicName), TopicConfig.class));
        }

        return allTopic;
    }

    @Override
    public TopicConfig getTopic(String topic) {
        return getZkData(getTopicPath(topic), TopicConfig.class);
    }

    @Override
    public List<GroupConfig> getAllGroup() {
        List<GroupConfig> allGroup = Lists.newArrayList();
        List<String> groupList = zkService.getChildren(CARRERA_GROUP);
        if (CollectionUtils.isEmpty(groupList)) {
            return allGroup;
        }
        for (String groupName : groupList) {
            allGroup.add(getZkData(getGroupPath(groupName), GroupConfig.class));
        }

        return allGroup;
    }

    @Override
    public GroupConfig getGroup(String group) {
        return getZkData(getGroupPath(group), GroupConfig.class);
    }

    @Override
    public List<PProxyConfig> getAllPProxy() {
        List<PProxyConfig> allPProxy = Lists.newArrayList();
        List<String> childList = zkService.getChildren(CARRERA_PPROXY);
        if (CollectionUtils.isEmpty(childList)) {
            return allPProxy;
        }
        for (String childPath : childList) {
            allPProxy.add(getZkData(getProxyPath(CARRERA_PPROXY, childPath), PProxyConfig.class));
        }

        return allPProxy;
    }

    @Override
    public PProxyConfig getPProxy(String instance) {
        return getZkData(getProxyPath(CARRERA_PPROXY, instance), PProxyConfig.class);
    }

    @Override
    public List<CProxyConfig> getAllCProxy() {
        List<CProxyConfig> allCProxy = Lists.newArrayList();
        List<String> childList = zkService.getChildren(CARRERA_CPROXY);
        if (CollectionUtils.isEmpty(childList)) {
            return allCProxy;
        }
        for (String childPath : childList) {
            allCProxy.add(getZkData(getProxyPath(CARRERA_CPROXY, childPath), CProxyConfig.class));
        }

        return allCProxy;
    }

    @Override
    public CProxyConfig getCProxy(String instance) {
        return getZkData(getProxyPath(CARRERA_CPROXY, instance), CProxyConfig.class);
    }

    @Override
    public List<BrokerConfig> getAllBroker() {
        List<BrokerConfig> allBroker = Lists.newArrayList();
        List<String> childList = zkService.getChildren(CARRERA_BROKER);
        if (CollectionUtils.isEmpty(childList)) {
            return allBroker;
        }
        for (String childPath : childList) {
            allBroker.add(getZkData(getBrokerPath(childPath), BrokerConfig.class));
        }

        return allBroker;
    }

    @Override
    public BrokerConfig getBroker(String brokerCluster) {
        return getZkData(getBrokerPath(brokerCluster), BrokerConfig.class);
    }

    private String getTopicPath(String topic) {
        return CARRERA_TOPIC + "/" + topic;
    }

    private String getGroupPath(String group) {
        return CARRERA_GROUP + "/" + group;
    }

    private String getProxyPath(String path, String instance) {
        return path + "/" + instance;
    }

    private String getBrokerPath(String broker) {
        return CARRERA_BROKER + "/" + broker;
    }

    private String getMonitorHostPath(String host) {
        return CARRERA_MONITHOR_HOST + "/" + host;
    }

    private String getMonitorAssignedPath(String broken) {
        return CARRERA_MONITOR_ASSIGNED + "/" + broken;
    }

    @Override
    public boolean createOrUpdateTopic(TopicConfig config) throws Exception {
        if (!config.validate()) {
            LOGGER.error("invalid topic config, config={}", config);
            throw new InvalidParameterException("invalid topic config");
        }

        setZkData(getTopicPath(config.getTopic()), config);
        return true;
    }

    private <T> void setZkData(String path, T t) {
        zkService.setData(path, CommonFastJsonUtils.toJsonStringDefault(t));
    }

    private <T> T getZkData(String path, Class<T> t) {
        try {
            String data = zkService.getData(path);
            if (StringUtils.isEmpty(data)) {
                return null;
            }

            return CommonFastJsonUtils.toObject(data, t);
        } catch (IOException e) {
            LOGGER.error("get zk exception, path={}", path, e);
            return null;
        } catch (ZkNoNodeException e) {
            LOGGER.warn("no node, path={}", path, e);
            return null;
        }
    }

    @Override
    public boolean deleteTopic(String topic) {
        zkService.delete(getTopicPath(topic));
        return true;
    }

    @Override
    public boolean createOrUpdateGroup(GroupConfig config) throws Exception {
        if (!config.validate()) {
            LOGGER.error("invalid group config, config={}", config);
            throw new InvalidParameterException("invalid group config");
        }

        setZkData(getGroupPath(config.getGroup()), config);
        return true;
    }

    @Override
    public boolean deleteGroup(String group) {
        zkService.delete(getGroupPath(group));
        return true;
    }

    @Override
    public boolean createOrUpdatePProxy(PProxyConfig config) throws Exception {
        if (!config.validate()) {
            LOGGER.error("invalid pproxy config, config={}", config);
            throw new InvalidParameterException("invalid pproxy config");
        }
        setZkData(getProxyPath(CARRERA_PPROXY, config.getInstance()), config);
        return true;
    }

    @Override
    public boolean deletePProxy(String instance) {
        zkService.delete(getProxyPath(CARRERA_PPROXY, instance));
        return true;
    }

    @Override
    public boolean createOrUpdateCProxy(CProxyConfig config) throws Exception {
        if (!config.validate()) {
            LOGGER.error("invalid cproxy config, config={}", config);
            throw new InvalidParameterException("invalid cproxy config");
        }
        setZkData(getProxyPath(CARRERA_CPROXY, config.getInstance()), config);
        return true;
    }

    @Override
    public boolean deleteCProxy(String instance) {
        zkService.delete(getProxyPath(CARRERA_CPROXY, instance));
        return true;
    }

    @Override
    public boolean createOrUpdateBroker(BrokerConfig config) throws Exception {
        if (!config.validate()) {
            LOGGER.error("invalid broker config, config={}", config);
            throw new InvalidParameterException("invalid broker config");
        }
        setZkData(getBrokerPath(config.getBrokerCluster()), config);
        return true;
    }

    @Override
    public boolean deleteBroker(String brokerCluster) {
        zkService.delete(getBrokerPath(brokerCluster));
        return true;
    }

    @Override
    public boolean createOrUpdateMonitorHost(String host, HostRegionConfig config) {
        setZkData(getMonitorHostPath(host), config);
        return true;
    }

    @Override
    public boolean createOrUpdateMonitorAssigned(String broker, MonitorAssignedConfig config) {
        setZkData(getMonitorAssignedPath(broker), config);
        return true;
    }

    @Override
    public List<String> getChildren(String path) {
        return zkService.getChildren(path);
    }
}