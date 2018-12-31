package com.didi.carrera.console.service.impl;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.TypeReference;
import com.didi.carrera.console.common.util.FastJsonUtils;
import com.didi.carrera.console.common.util.HostUtils;
import com.didi.carrera.console.config.ConsoleConfig;
import com.didi.carrera.console.dao.dict.*;
import com.didi.carrera.console.dao.model.*;
import com.didi.carrera.console.dao.model.custom.ConsumeGroupConfig;
import com.didi.carrera.console.service.*;
import com.didi.carrera.console.service.exception.ZkConfigException;
import com.didi.carrera.console.web.ConsoleBaseResponse;
import com.didi.carrera.console.web.controller.bo.ConsumeSubscriptionOrderBo;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.xiaojukeji.carrera.biz.ZkService;
import com.xiaojukeji.carrera.config.Actions;
import com.xiaojukeji.carrera.config.CompressType;
import com.xiaojukeji.carrera.config.ConfigurationValidator;
import com.xiaojukeji.carrera.config.v4.*;
import com.xiaojukeji.carrera.config.v4.cproxy.*;
import com.xiaojukeji.carrera.config.v4.pproxy.CarreraConfiguration;
import com.xiaojukeji.carrera.config.v4.pproxy.RocketmqConfiguration;
import com.xiaojukeji.carrera.config.v4.pproxy.TopicConfiguration;
import com.xiaojukeji.carrera.dynamic.ParameterDynamicConfig;
import com.xiaojukeji.carrera.dynamic.ParameterDynamicZookeeper;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;


@Service
public class ZKV4ConfigServiceImpl implements ZKV4ConfigService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ZKV4ConfigServiceImpl.class);

    public static final int DEFAULT_BROKER_PORT = 10911;
    public static final int DEFAULT_PPROXY_PORT = 9613;
    public static final int DEFAULT_CPROXY_PORT = 9713;

    private static final int DEFAULT_MIN_CONCURRENCY = 1000;

    @Resource(name = "didiClusterServiceImpl")
    private ClusterService clusterService;

    @Resource(name = "didiTopicServiceImpl")
    private TopicService topicService;

    @Resource(name = "didiTopicConfServiceImpl")
    private TopicConfService topicConfService;

    @Resource(name = "didiConsumeSubscriptionServiceImpl")
    private ConsumeSubscriptionService consumeSubscriptionService;

    @Resource(name = "didiConsumeGroupServiceImpl")
    private ConsumeGroupService consumeGroupService;

    @Resource(name = "didiNodeServiceImpl")
    private NodeService nodeService;

    @Resource(name = "didiMqServerServiceImpl")
    private MqServerService mqServerService;

    @Autowired
    private ClusterMqserverRelationService mqserverRelationService;

    @Autowired
    private ZkService zkService;

    private Map<Long, MqServer> getMqServerMap() {
        Map<Long, MqServer> mqServerMap = Maps.newHashMap();
        mqServerService.findAll().forEach(mqServer -> mqServerMap.put(mqServer.getId(), mqServer));
        return mqServerMap;
    }

    private Map<String, MqServer> getMqServerNameMap() {
        Map<String, MqServer> mqServerMap = Maps.newHashMap();
        mqServerService.findAll().forEach(mqServer -> mqServerMap.put(mqServer.getName(), mqServer));
        return mqServerMap;
    }

    private Map<Long, Cluster> getClusterMap() {
        Map<Long, Cluster> clusterMap = Maps.newHashMap();
        clusterService.findAll().forEach(cluster -> clusterMap.put(cluster.getId(), cluster));
        return clusterMap;
    }

    @Override
    public void initZkPath() throws Exception {
        ParameterDynamicZookeeper parameterDynamic = null;
        try {
            ParameterDynamicConfig config = new ParameterDynamicConfig(ConsoleConfig.instance().getZookeeper());
            config.setConfigCentre(true);
            parameterDynamic = new ParameterDynamicZookeeper(config);
            parameterDynamic.setData("/carrera/v4/config/topic", "default");
            parameterDynamic.setData("/carrera/v4/config/group", "default");
            parameterDynamic.setData("/carrera/v4/config/pproxy", "default");
            parameterDynamic.setData("/carrera/v4/config/cproxy", "default");
            parameterDynamic.setData("/carrera/v4/config/broker", "default");
            parameterDynamic.setData("/carrera/v4/config/monitor/host", "default");
            parameterDynamic.setData("/carrera/v4/config/monitor/assigned", "default");

            LOGGER.info("init all zk path success");
        } finally {
            if (parameterDynamic != null) {
                parameterDynamic.shutdown();
            }
        }
    }

    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public ConsoleBaseResponse<?> pushCproxyConfig(String host) throws Exception {
        List<Node> nodeList = nodeService.findByHostNodeType(host, NodeType.CONSUMER_PROXY);
        if (CollectionUtils.isEmpty(nodeList)) {
            nodeList = nodeService.findByHostNodeType(HostUtils.getIp(host), NodeType.CONSUMER_PROXY);
        }

        if (CollectionUtils.isEmpty(nodeList)) {
            LOGGER.warn("[ZK_V4] not found cproxy node, host={}", host);
            return ConsoleBaseResponse.error(ConsoleBaseResponse.Status.INVALID_PARAM, "host不存在");
        }
        updateCProxyConfig(nodeList.get(0).getId());

        return ConsoleBaseResponse.success();
    }

    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public ConsoleBaseResponse<?> pushPproxyConfig(String host) throws Exception {
        List<Node> nodeList = nodeService.findByHostNodeType(host, NodeType.PRODUCER_PROXY);
        if (CollectionUtils.isEmpty(nodeList)) {
            nodeList = nodeService.findByHostNodeType(HostUtils.getIp(host), NodeType.PRODUCER_PROXY);
        }

        if (CollectionUtils.isEmpty(nodeList)) {
            LOGGER.warn("[ZK_V4] not found pproxy node, host={}", host);
            return ConsoleBaseResponse.error(ConsoleBaseResponse.Status.INVALID_PARAM, "host不存在");
        }
        updatePProxyConfig(nodeList.get(0).getId());

        return ConsoleBaseResponse.success();
    }

    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public ConsoleBaseResponse<?> pushTopicConfig(String topicName) throws Exception {
        Topic topic = topicService.findByTopicName(topicName);
        if (topic == null) {
            LOGGER.warn("[ZK_V4] not found topic, topic={}", topicName);
            return ConsoleBaseResponse.error(ConsoleBaseResponse.Status.INVALID_PARAM, "topic不存在");
        }
        return pushTopicConfig(topic);
    }

    private ConsoleBaseResponse<?> pushTopicConfig(Topic topic) throws Exception {
        List<TopicConf> confList = topicConfService.findByTopicId(topic.getId());
        if (CollectionUtils.isEmpty(confList)) {
            LOGGER.warn("[ZK_V4] not found topic conf, topic={}", topic.getTopicName());
            return ConsoleBaseResponse.error(ConsoleBaseResponse.Status.INVALID_PARAM, "topic conf不存在");
        }
        updateTopicConfig(topic.getId(), confList.stream().map(TopicConf::getClusterId).collect(Collectors.toSet()));

        return ConsoleBaseResponse.success();
    }

    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public ConsoleBaseResponse<?> pushGroupConfig(String groupName) throws Exception {
        ConsumeGroup group = consumeGroupService.findByGroupName(groupName);
        if (group == null) {
            LOGGER.warn("[ZK_V4] not found group, group={}", groupName);
            return ConsoleBaseResponse.error(ConsoleBaseResponse.Status.INVALID_PARAM, "group不存在");
        }
        return pushGroupConfig(group);
    }

    private ConsoleBaseResponse<?> pushGroupConfig(ConsumeGroup group) throws Exception {
        List<ConsumeSubscription> subList = consumeSubscriptionService.findByGroupId(group.getId());
        updateSubConfig(group.getId(), subList.stream().map(ConsumeSubscription::getClusterId).collect(Collectors.toSet()));

        return ConsoleBaseResponse.success();
    }

    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public ConsoleBaseResponse<?> pushTopicByCluster(String clusterName) throws Exception {
        Cluster cluster = clusterService.findByClusterName(clusterName);
        if (cluster == null) {
            return ConsoleBaseResponse.error(ConsoleBaseResponse.Status.INVALID_PARAM, "集群不存在");
        }
        List<Long> list = topicConfService.findTopicByClusterIdWithDeleted(cluster.getId());
        if (CollectionUtils.isEmpty(list)) {
            return ConsoleBaseResponse.error(ConsoleBaseResponse.Status.INVALID_PARAM, "集群不存在topic");
        }
        Set<Long> clusters = Sets.newHashSet();
        for (Long topicId : list) {
            List<TopicConf> confList = topicConfService.findByTopicId(topicId);
            clusters.addAll(confList.stream().map(TopicConf::getClusterId).collect(Collectors.toSet()));
            updateTopicConfig(topicId, null);
        }
        updatePProxyConfigByClusterId("pushTopicByCluster", clusters);
        return ConsoleBaseResponse.success();
    }

    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public ConsoleBaseResponse<?> pushGroupByCluster(String clusterName) throws Exception {
        Cluster cluster = clusterService.findByClusterName(clusterName);
        if (cluster == null) {
            return ConsoleBaseResponse.error(ConsoleBaseResponse.Status.INVALID_PARAM, "集群不存在");
        }
        List<ConsumeGroup> list = consumeGroupService.findByClusterId(cluster.getId());
        if (CollectionUtils.isEmpty(list)) {
            return ConsoleBaseResponse.error(ConsoleBaseResponse.Status.INVALID_PARAM, "集群暂无订阅");
        }

        Set<Long> clusters = Sets.newHashSet();
        for (ConsumeGroup group : list) {
            List<ConsumeSubscription> subList = consumeSubscriptionService.findByGroupId(group.getId());

            clusters.addAll(subList.stream().map(ConsumeSubscription::getClusterId).collect(Collectors.toSet()));
            updateSubConfig(group.getId(), null);
        }

        updateCProxyConfigByClusterId("pushGroupByCluster", clusters);
        return ConsoleBaseResponse.success();
    }


    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public ConsoleBaseResponse<?> pushPProxyByCluster(String clusterName) throws Exception {
        Cluster cluster = clusterService.findByClusterName(clusterName);
        if (cluster == null) {
            return ConsoleBaseResponse.error(ConsoleBaseResponse.Status.INVALID_PARAM, "集群不存在");
        }
        List<Node> list = nodeService.findByClusterIdNodeType(cluster.getId(), NodeType.PRODUCER_PROXY);
        if (CollectionUtils.isEmpty(list)) {
            return ConsoleBaseResponse.error(ConsoleBaseResponse.Status.INVALID_PARAM, "集群暂无PProxy");
        }

        for (Node node : list) {
            updatePProxyConfig(node.getId());
        }
        return ConsoleBaseResponse.success();
    }

    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public ConsoleBaseResponse<?> pushCProxyByCluster(String clusterName) throws Exception {
        Cluster cluster = clusterService.findByClusterName(clusterName);
        if (cluster == null) {
            return ConsoleBaseResponse.error(ConsoleBaseResponse.Status.INVALID_PARAM, "集群不存在");
        }
        List<Node> list = nodeService.findByClusterIdNodeType(cluster.getId(), NodeType.CONSUMER_PROXY);
        if (CollectionUtils.isEmpty(list)) {
            return ConsoleBaseResponse.error(ConsoleBaseResponse.Status.INVALID_PARAM, "集群暂无CProxy");
        }

        for (Node node : list) {
            updateCProxyConfig(node.getId());
        }
        return ConsoleBaseResponse.success();
    }

    @Override
    public void initAllZk() throws Exception {
        List<Topic> topicList = topicService.findAllWithoutPage();
        for (Topic topic : topicList) {
            updateTopicConfig(topic.getId(), null);
        }

        List<ConsumeGroup> groupList = consumeGroupService.findAll();
        for (ConsumeGroup consumeGroup : groupList) {
            updateSubConfig(consumeGroup.getId(), null);
        }

        List<Node> nodeList = nodeService.findAll();
        for (Node node : nodeList) {
            NodeType nodeType = NodeType.getByIndex(node.getNodeType());
            if (nodeType == null) {
                continue;
            }
            switch (nodeType) {
                case PRODUCER_PROXY:
                    updatePProxyConfig(node.getId());
                    break;
                case CONSUMER_PROXY:
                    updateCProxyConfig(node.getId());
                    break;
                default:
                    break;
            }
        }

        List<MqServer> mqServerList = mqServerService.findAll();
        for (MqServer mqServer : mqServerList) {
            if (mqServer.getType() == MqServerType.ROCKETMQ.getIndex()) {
                updateBrokerConfig(mqServer.getId());
            }
        }
    }

    @Override
    public void updateTopicConfig(Long topicId, Set<Long> clusterIdSet) throws Exception {
        Topic topic = topicService.findById(topicId);
        if (topic == null) {
            LOGGER.warn("[ZK_V4_Topic] topic not found, topicId={}", topicId);
            throw new ZkConfigException(String.format("[Topic] topic not found, topicId=%s", topicId));
        }

        TopicConfig zkTopicConfig = zkService.getTopic(topic.getTopicName());
        if (topic.getIsDelete() == IsDelete.YES.getIndex()) {
            LOGGER.warn("[ZK_V4_Topic] topic is deleted, delete it from zk, topicId={}, topic={}", topicId, topic.getTopicName());
            if (zkTopicConfig != null) {
                zkService.deleteTopic(topic.getTopicName());
                updatePProxyConfigByClusterId(topic.getTopicName(), clusterIdSet);
            }
            return;
        }
        List<TopicConf> topicConfList = topicConfService.findByTopicId(topicId);
        if (CollectionUtils.isEmpty(topicConfList)) {
            LOGGER.warn("[ZK_V4_Topic] topic conf not found, delete topic from zk, topicId={}, topic={}", topicId, topic.getTopicName());
            if (zkTopicConfig != null) {
                zkService.deleteTopic(topic.getTopicName());
                updatePProxyConfigByClusterId(topic.getTopicName(), clusterIdSet);
            }
            return;
        }

        TopicConfig topicConfig = buildTopicConfig(topic, topicConfList);
        zkService.createOrUpdateTopic(topicConfig);
        updatePProxyConfigByClusterId(topic.getTopicName(), clusterIdSet);
        LOGGER.debug("[ZK_V4_Topic] topic update success, topicId={}, topicConfig={}", topicId, topicConfig);
        LOGGER.info("[ZK_V4_Topic] topic update success, topicId={}, topicName={}, topicConf size={}", topicId, topic.getTopicName(), topicConfList.size());
    }

    @Override
    public void updatePProxyConfigByClusterId(String topic, Set<Long> clusterIdSet) throws Exception {
        if (CollectionUtils.isEmpty(clusterIdSet)) {
            LOGGER.warn("[ZK_V4_PProxy] clusterIdSet is empty, skip update, topic={}", topic);
            return;
        }
        for (Long clusterId : clusterIdSet) {
            List<Node> nodeList = nodeService.findByClusterIdNodeType(clusterId, NodeType.PRODUCER_PROXY);
            if (CollectionUtils.isNotEmpty(nodeList)) {
                for (Node node : nodeList) {
                    updatePProxyConfig(node.getId());
                }
            }
        }
    }

    @Override
    public void updateCProxyConfigByClusterId(String group, Set<Long> clusterIdSet) throws Exception {
        if (CollectionUtils.isEmpty(clusterIdSet)) {
            LOGGER.warn("[ZK_V4_CProxy] clusterIdSet is empty, skip update, group={}", group);
            return;
        }
        for (Long clusterId : clusterIdSet) {
            List<Node> nodeList = nodeService.findByClusterIdNodeType(clusterId, NodeType.CONSUMER_PROXY);
            if (CollectionUtils.isNotEmpty(nodeList)) {
                for (Node node : nodeList) {
                    updateCProxyConfig(node.getId());
                }
            }
        }
    }

    private TopicConfig buildTopicConfig(Topic topic, List<TopicConf> topicConfList) throws ZkConfigException {
        TopicConfig topicConfig = new TopicConfig();
        topicConfig.setTopic(topic.getTopicName());
        topicConfig.setAlarmGroup(topic.getTopicAlarmGroup());
        topicConfig.setDelayTopic(topic.getDelayTopic() == TopicDelayTopic.DELAY_TOPIC.getIndex());
        topicConfig.setAutoBatch(topic.getTopicConfig().isAutoBatch());

        if (topic.getTopicConfig().getCompressionType() == TopicCompressionType.RMQ_COMPRESSION.getIndex()) {
            topicConfig.setCompressType(CompressType.PRIMORDIAL);
        } else if (topic.getTopicConfig().getCompressionType() == TopicCompressionType.SNAPPY_COMPRESSION.getIndex()) {
            topicConfig.setCompressType(CompressType.SNAPPY);
        }

        List<TopicConfiguration> confList = Lists.newArrayList();
        topicConfig.setTopicUnits(confList);

        Map<Long, MqServer> mqServerTypeTable = getMqServerMap();
        Map<Long, Cluster> clusterTable = getClusterMap();

        for (TopicConf conf : topicConfList) {
            if (!mqServerTypeTable.containsKey(conf.getMqServerId())) {
                throw new ZkConfigException(String.format("[Topic] topicConfId(%s) not found mqserver(%s)", conf.getId(), conf.getMqServerId()));
            }

            if (!clusterTable.containsKey(conf.getClusterId())) {
                throw new ZkConfigException(String.format("[Topic] topicConfId(%s) not found cluster(%s)", conf.getId(), conf.getClusterId()));
            }

            TopicConfiguration topicConfiguration = new TopicConfiguration();
            topicConfiguration.setBrokerCluster(mqServerTypeTable.get(conf.getMqServerId()).getName());

            if (conf.getTopicConfConfig() != null) {
                if (MapUtils.isNotEmpty(conf.getTopicConfConfig().getProxies())) {
                    topicConfiguration.setProxies(conf.getTopicConfConfig().getProxies());
                }
            }

            topicConfiguration.setTotalMaxTps(conf.getProduceTps());

            //同一个broker下如果绑定多个proxy，分摊总tps
            if (MapUtils.isNotEmpty(topicConfiguration.getProxies())) {
                int totalProxies = 0;
                for (Set<String> proxySet : topicConfiguration.getProxies().values()) {
                    totalProxies += proxySet.size();
                }
                topicConfiguration.setMaxTps((int) (Math.ceil((double) topicConfiguration.getTotalMaxTps() / (double) totalProxies)));
            }
            confList.add(topicConfiguration);
        }

        return topicConfig;
    }

    @Override
    public void onlyUpdateGroupConfig(Long groupId) throws Exception {
        ConsumeGroup group = consumeGroupService.findById(groupId);
        if (group == null) {
            LOGGER.warn("[ZK_V4_Group] group not found, groupId={}", groupId);
            throw new ZkConfigException(String.format("[Group] group not found, groupId=%s", groupId));
        }

        if (group.getIsDelete() == IsDelete.YES.getIndex()) {
            LOGGER.warn("[ZK_V4_Group] group is deleted, skip update zk, groupId={}, group={}", groupId, group.getGroupName());
            return;
        }

        List<ConsumeSubscription> subList = consumeSubscriptionService.findByGroupId(groupId);
        if (CollectionUtils.isEmpty(subList)) {
            LOGGER.warn("[ZK_V4_Group] sub is empty, skip update zk, groupId={}, group={}", groupId, group.getGroupName());
            return;
        }

        GroupConfig groupConfig = buildGroupConfig(group, subList);
        zkService.createOrUpdateGroup(groupConfig);
        LOGGER.debug("[ZK_V4_Group] update group success, groupId={}, groupConfig={}", groupId, groupConfig);
        LOGGER.info("[ZK_V4_Group] update group success, groupId={}, group={}, sub size={}", groupId, group.getGroupName(), subList.size());
    }

    @Override
    public void updateSubConfig(Long groupId, Set<Long> clusterIdSet) throws Exception {
        ConsumeGroup group = consumeGroupService.findById(groupId);
        if (group == null) {
            LOGGER.warn("[ZK_V4_Sub] group not found, groupId={}", groupId);
            throw new ZkConfigException(String.format("[Group] group not found, groupId=%s", groupId));
        }

        if (group.getIsDelete() == IsDelete.YES.getIndex()) {
            LOGGER.warn("[ZK_V4_Sub] group is deleted, delete it from zk, groupId={}, group={}", groupId, group.getGroupName());
            deleteGroupConfig(clusterIdSet, group);
            return;
        }

        List<ConsumeSubscription> subList = consumeSubscriptionService.findByGroupId(groupId);
        if (CollectionUtils.isEmpty(subList)) {
            LOGGER.warn("[ZK_V4_Sub] sub not found, delete group from zk, groupId={}, group={}", groupId, group.getGroupName());
            deleteGroupConfig(clusterIdSet, group);
            return;
        }

        GroupConfig groupConfig = buildGroupConfig(group, subList);
        zkService.createOrUpdateGroup(groupConfig);
        updateCProxyConfigByClusterId(group.getGroupName(), clusterIdSet);

        LOGGER.debug("[ZK_V4_Sub] update group success, groupId={}, groupConfig={}", groupId, groupConfig);
        LOGGER.info("[ZK_V4_Sub] update group success, groupId={}, group={}, sub size={}", groupId, group.getGroupName(), subList.size());
    }

    private void deleteGroupConfig(Set<Long> clusterIdSet, ConsumeGroup group) throws Exception {
        GroupConfig zkGroupConfig = zkService.getGroup(group.getGroupName());
        if (zkGroupConfig != null) {
            zkService.deleteGroup(group.getGroupName());
            updateCProxyConfigByClusterId(group.getGroupName(), clusterIdSet);
        }
    }

    private GroupConfig buildGroupConfig(ConsumeGroup group, List<ConsumeSubscription> subList) throws ZkConfigException {
        GroupConfig groupConfig = new GroupConfig();
        groupConfig.setGroup(group.getGroupName());

        if (group.getConsumeGroupConfig() != null) {
            ConsumeGroupConfig config = group.getConsumeGroupConfig();
            groupConfig.setAsyncThreads(config.getAsyncThreads());
            groupConfig.setRedisConfig(config.getRedisConfig());
        }
        groupConfig.setAlarmGroup(group.getGroupAlarmGroup());
        groupConfig.setEnableAlarm(IsEnable.isEnable(group.getAlarmIsEnable()));
        groupConfig.setDelayTimeThreshold(group.getAlarmDelayTime());
        groupConfig.setCommittedLagThreshold(group.getAlarmMsgLag());

        List<UpstreamTopic> upstreamTopics = Lists.newArrayList();
        groupConfig.setTopics(upstreamTopics);

        Map<Long, MqServer> mqServerTypeTable = getMqServerMap();
        Map<Long, Cluster> clusterTable = getClusterMap();
        Map<String, List<TopicConf>> topicConfMap = findTopicConfMap(subList);
        for (ConsumeSubscription sub : subList) {
            if (!clusterTable.containsKey(sub.getClusterId())) {
                throw new ZkConfigException(String.format("[Group] subId(%s) not found cluster(%s)", sub.getId(), sub.getClusterId()));
            }

            String topicConfMapKey = getTopicConfMapKey(sub.getTopicId(), sub.getClusterId());
            if (!topicConfMap.containsKey(topicConfMapKey) || CollectionUtils.isEmpty(topicConfMap.get(topicConfMapKey))) {
                throw new ZkConfigException(String.format("[Group] subId(%s) not found topicConf, topicId=%s, clusterId=%s", sub.getId(), sub.getTopicId(), sub.getClusterId()));
            }

            String mqServer = getSubExtraParamsMqServer(sub);
            if (StringUtils.isNotEmpty(mqServer)) {
                UpstreamTopic upstreamTopic = buildUpstreamTopic(groupConfig, sub, mqServer);
                upstreamTopics.add(upstreamTopic);
                continue;
            }

            for (TopicConf topicConf : topicConfMap.get(topicConfMapKey)) {
                if (!mqServerTypeTable.containsKey(topicConf.getMqServerId())) {
                    throw new ZkConfigException(String.format("[Group] subId(%s) not found mqserverId, topicConfId=%s, mqServerId=%s", sub.getId(), topicConf.getId(), topicConf.getMqServerId()));
                }

                UpstreamTopic upstreamTopic = buildUpstreamTopic(groupConfig, sub, topicConf.getMqServerName());
                upstreamTopics.add(upstreamTopic);
            }
        }
        return groupConfig;
    }

    private Map<String, List<TopicConf>> findTopicConfMap(List<ConsumeSubscription> subList) {
        Set<Long> topicIdSet = Sets.newHashSet();
        Set<Long> clusterIdSet = Sets.newHashSet();
        subList.forEach(sub -> {
            topicIdSet.add(sub.getTopicId());
            clusterIdSet.add(sub.getClusterId());
        });

        List<TopicConf> topicConfList = topicConfService.findByTopicClusterIds(Lists.newArrayList(topicIdSet), Lists.newArrayList(clusterIdSet));
        Map<String, List<TopicConf>> topicConfMap = Maps.newHashMap();
        if (CollectionUtils.isNotEmpty(topicConfList)) {
            topicConfList.forEach(conf -> topicConfMap.computeIfAbsent(getTopicConfMapKey(conf.getTopicId(), conf.getClusterId()), s -> Lists.newArrayList()).add(conf));
        }
        return topicConfMap;
    }

    private String getTopicConfMapKey(Long topicId, Long clusterId) {
        return topicId + "_" + clusterId;
    }

    private String getSubExtraParamsMqServer(ConsumeSubscription sub) throws ZkConfigException {
        if (MapUtils.isNotEmpty(sub.getSubExtraParams()) && sub.getSubExtraParams().containsKey(ConsumeSubscriptionOrderBo.SUB_FLAG_EXTREA_PARAMS_MQ_CLUSTER)) {
            String tmpCluster = sub.getSubExtraParams().get(ConsumeSubscriptionOrderBo.SUB_FLAG_EXTREA_PARAMS_MQ_CLUSTER);
            String[] arr = tmpCluster.split(";");
            for (String cluster : arr) {
                if (mqServerService.findByName(cluster) == null) {
                    LOGGER.error("[Upstream] mqServer not found,  subId={}, extraParams={}, ", sub.getId(), sub.getSubExtraParams());
                    throw new ZkConfigException(String.format("[Upstream] subId<%s> not found extraParam mqserver<%s>", sub.getId(), sub.getTopicId()));
                }
            }
            return tmpCluster;
        }

        return null;
    }

    public UpstreamTopic buildUpstreamTopic(GroupConfig groupConfig, ConsumeSubscription subscription, String brokerCluster) throws ZkConfigException {
        UpstreamTopic upstreamTopic = new UpstreamTopic();

        upstreamTopic.setBrokerCluster(brokerCluster);
        if (subscription.getConsumeSubscriptionConfig() != null && MapUtils.isNotEmpty(subscription.getConsumeSubscriptionConfig().getProxies())) {
            upstreamTopic.setProxies(Maps.newHashMap(subscription.getConsumeSubscriptionConfig().getProxies()));
        }

        upstreamTopic.setTopic(subscription.getTopicName());
        upstreamTopic.setEnabled(subscription.getState() == IsEnable.ENABLE.getIndex());

        upstreamTopic.setMaxPullBatchSize(subscription.getConsumeSubscriptionConfig().getMaxPullBatchSize());
        upstreamTopic.setFetchThreads(subscription.getConsumeSubscriptionConfig().getFetchThreads());
        upstreamTopic.setMaxConsumeLag(subscription.getConsumeSubscriptionConfig().getMaxConsumeLag());

        if (subscription.getAlarmType() == ConsumeSubscriptionAlarmType.EXTEND_GROUP_CONFIG.getIndex()) {
            upstreamTopic.setEnableAlarm(groupConfig.isEnableAlarm());
            upstreamTopic.setDelayTimeThreshold(groupConfig.getDelayTimeThreshold());
            upstreamTopic.setCommittedLagThreshold(groupConfig.getCommittedLagThreshold());
        } else {
            upstreamTopic.setEnableAlarm(IsEnable.isEnable(subscription.getAlarmIsEnable()));
            upstreamTopic.setDelayTimeThreshold(subscription.getAlarmDelayTime());
            upstreamTopic.setCommittedLagThreshold(subscription.getAlarmMsgLag());
        }

        if (subscription.getSubActions().contains(Actions.ASYNC_HTTP)) {
            upstreamTopic.setConcurrency(subscription.getPushMaxConcurrency());
        } else {
            upstreamTopic.setConcurrency(subscription.getConsumeSubscriptionConfig().getConcurrency() < DEFAULT_MIN_CONCURRENCY ? DEFAULT_MIN_CONCURRENCY : subscription.getConsumeSubscriptionConfig().getConcurrency());
        }

        upstreamTopic.setAppendContext(subscription.getConsumeSubscriptionConfig().getAppendContext());
        upstreamTopic.setPressureTraffic(subscription.getPressureTraffic() == ConsumeSubscriptionPressureTraffic.ENABLE.getIndex());

        upstreamTopic.setActions(subscription.getSubActions());
        if (IsEnable.isEnable(subscription.getEnableGroovy())) {
            upstreamTopic.setGroovyScript(subscription.getGroovy());
        }

        upstreamTopic.setTotalMaxTps(subscription.getMaxTps());

        if (MapUtils.isNotEmpty(upstreamTopic.getProxies())) {
            int totalProxies = 0;
            for (Set<String> proxySet : upstreamTopic.getProxies().values()) {
                totalProxies += proxySet.size();
            }
            upstreamTopic.setMaxTps((int) (Math.ceil(upstreamTopic.getTotalMaxTps() / (double) totalProxies)));
        }
        upstreamTopic.setHttpMaxTps(subscription.getConsumeSubscriptionConfig().getHttpMaxTps());

        upstreamTopic.setTimeout(subscription.getConsumeTimeout());
        upstreamTopic.setMaxRetry(subscription.getErrorRetryTimes());
        if (CollectionUtils.isNotEmpty(subscription.getSubUrls())) {
            upstreamTopic.setUrls(subscription.getSubUrls());
        }
        if (CollectionUtils.isNotEmpty(subscription.getSubRetryIntervals())) {
            upstreamTopic.setRetryIntervals(subscription.getSubRetryIntervals());
        }

        if (IsEnable.isEnable(subscription.getEnableTransit()) && MapUtils.isNotEmpty(subscription.getSubTransit())) {
            upstreamTopic.setTransit(Maps.newHashMap(subscription.getSubTransit()));
        }

        if (MapUtils.isNotEmpty(subscription.getSubHttpQueryParams())) {
            upstreamTopic.setQueryParams(Maps.newHashMap(subscription.getSubHttpQueryParams()));
        }
        if (IsEnable.isEnable(subscription.getEnableOrder())) {
            upstreamTopic.setOrderKey(subscription.getOrderKey());
        }

        upstreamTopic.setHttpMethod(subscription.getHttpMethod() == null ? null : ConsumeSubscriptionHttpMethod.getByIndex(subscription.getHttpMethod()) == null ? null : ConsumeSubscriptionHttpMethod.getByIndex(subscription.getHttpMethod()).getName());
        upstreamTopic.setTokenKey(subscription.getHttpToken());

        if (subscription.getConsumeType() == ConsumeSubscriptionConsumeType.BIG_DATA.getIndex()) {
            try {
                if (subscription.getBigDataType() == ConsumeSubscriptionBigDataType.HDFS.getIndex()) {
                    upstreamTopic.setHdfsConfiguration(FastJsonUtils.toObject(subscription.getBigDataConfig(), HdfsConfiguration.class));
                } else if (subscription.getBigDataType() == ConsumeSubscriptionBigDataType.HBASE.getIndex()) {
                    upstreamTopic.setHbaseconfiguration(FastJsonUtils.toObject(subscription.getBigDataConfig(), HBaseConfiguration.class));
                } else if (subscription.getBigDataType() == ConsumeSubscriptionBigDataType.REDIS.getIndex()) {
                } else {
                    throw new ZkConfigException(String.format("[Sub] subId(%s) bigDataType error", subscription.getId()));
                }
            } catch (JSONException e) {
                throw new ZkConfigException(String.format("[Sub] subId(%s) bigDataConfig JSON error", subscription.getId()));
            }
        }

        if (!upstreamTopic.getActions().contains(Actions.ASYNC) && !upstreamTopic.getActions().contains(Actions.NONBLOCKASYNC)) {
            if (MapUtils.isEmpty(subscription.getSubExtraParams()) || !subscription.getSubExtraParams().containsKey(ConsumeSubscriptionOrderBo.SUB_FLAG_ACTION_IGNORE_ASYNC) || !"true".equalsIgnoreCase(subscription.getSubExtraParams().get(ConsumeSubscriptionOrderBo.SUB_FLAG_ACTION_IGNORE_ASYNC))) {
                upstreamTopic.getActions().add(0, Actions.ASYNC);
            }
            //skip
        }

        return upstreamTopic;
    }

    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void updatePProxyConfig(Long nodeId) throws Exception {
        Node node = nodeService.findById(nodeId);
        if (node == null) {
            LOGGER.warn("[ZK_V4_PProxy] node not found, nodeId={}", nodeId);
            throw new ZkConfigException(String.format("[PProxy] node not found, nodeId=%s", nodeId));
        }

        if (node.getNodeType() != NodeType.PRODUCER_PROXY.getIndex()) {
            LOGGER.warn("[ZK_V4_PProxy] node isn't pproxy node, nodeId={}", nodeId);
            throw new ZkConfigException(String.format("[PProxy] node isn't pproxy node, nodeId=%s", nodeId));
        }

        Cluster cluster = clusterService.findById(node.getClusterId());
        if (cluster == null || cluster.getIsDelete() == IsDelete.YES.getIndex()) {
            LOGGER.warn(String.format("[ZK_V4_PProxy] not found cluster, nodeId=%s, clusterId=%s", nodeId, node.getClusterId()));
            throw new ZkConfigException(String.format("[PProxy] not found cluster, nodeId=%s, clusterId=%s", nodeId, node.getClusterId()));
        }

        List<ClusterMqserverRelation> relationList = mqserverRelationService.findByClusterId(cluster.getId(), ClusterMqServerRelationType.P_PROXY);
        if (CollectionUtils.isEmpty(relationList)) {
            LOGGER.warn(String.format("[ZK_V4_PProxy] not found ClusterMqserverRelation, nodeId=%s, clusterId=%s", nodeId, node.getClusterId()));
            throw new ZkConfigException(String.format("[PProxy] not found ClusterMqserverRelation, nodeId=%s, clusterId=%s", nodeId, node.getClusterId()));
        }

        String host = HostUtils.getIpPortFromHost(node.getHost(), DEFAULT_PPROXY_PORT);
        PProxyConfig zkPProxyConfig = zkService.getPProxy(host);
        if (node.getIsDelete() == IsDelete.YES.getIndex()) {
            LOGGER.warn("[ZK_V4_PProxy] node is deleted, delete it from zk, nodeId={}, clusterId={}, host={}", nodeId, node.getClusterId(), node.getHost());
            if (zkPProxyConfig != null) {
                zkService.deletePProxy(host);
            }
            return;
        }

        PProxyConfig pProxyConfig = buildPProxyConfig(node, host, cluster, relationList);
        zkService.createOrUpdatePProxy(pProxyConfig);
        if (zkPProxyConfig == null) {
            updateBrokerConfigByMqserverId(relationList);
        }

        LOGGER.debug("[ZK_V4_PProxy] update pproxy success, nodeId={}, host={}, clusterId={}, pProxyConfig={}", nodeId, node.getClusterId(), node.getHost(), pProxyConfig);
        LOGGER.info("[ZK_V4_PProxy] update pproxy success, nodeId={}, clusterId={}, host={}", nodeId, node.getClusterId(), node.getHost());

    }

    private PProxyConfig buildPProxyConfig(Node node, String host, Cluster cluster, List<ClusterMqserverRelation> relationList) throws ZkConfigException, ConfigurationValidator.ConfigException {
        PProxyConfig pProxyConfig = new PProxyConfig();
        pProxyConfig.setInstance(host);
        pProxyConfig.setProxyCluster(getPProxyCluster(cluster.getName()));
        pProxyConfig.setBrokerClusters(relationList.stream().map(ClusterMqserverRelation::getMqServerName).collect(Collectors.toList()));

        Map<String, MqServer> mqServerTypeTable = getMqServerNameMap();
        for (ClusterMqserverRelation relation : relationList) {
            if (StringUtils.isEmpty(relation.getProxyConf()) || "{}".equals(relation.getProxyConf())) {
                continue;
            }

            if (!mqServerTypeTable.containsKey(relation.getMqServerName())) {
                throw new ZkConfigException(String.format("[PProxy] relaion mqserverId not exist, nodeId=%s, clusterId=%s, mqserverId=%s", node.getId(), node.getClusterId(), relation.getMqServerId()));
            }

            CarreraConfiguration carreraConfiguration = FastJsonUtils.toObject(relation.getProxyConf(), new TypeReference<CarreraConfiguration>() {
            });
            if (carreraConfiguration == null || !carreraConfiguration.validate()) {
                throw new ZkConfigException(String.format("[PProxy] CarreraConfiguration get failed, nodeId=%s, clusterId=%s, relationId=%s", node.getId(), node.getClusterId(), relation.getId()));

            }

            if (MapUtils.isNotEmpty(carreraConfiguration.getRocketmqConfigurationMap())) {
                for (Map.Entry<String, RocketmqConfiguration> entry : carreraConfiguration.getRocketmqConfigurationMap().entrySet()) {
                    String brokerCluster = entry.getKey();
                    RocketmqConfiguration conf = entry.getValue();

                    if (!mqServerTypeTable.containsKey(brokerCluster)) {
                        throw new ZkConfigException(String.format("[PProxy] CarreraConfiguration rocketmq config brokerCluster not found, nodeId=%s, clusterId=%s, relationId=%s, brokerCluster=%s", node.getId(), node.getClusterId(), relation.getId(), brokerCluster));
                    }

                    MqServer mqServer = mqServerTypeTable.get(brokerCluster);
                    conf.setNamesrvAddrs(Lists.newArrayList(mqServer.getAddr().split(";")));
                }
            }

            pProxyConfig.setCarreraConfiguration(carreraConfiguration);
            break;
        }

        if (pProxyConfig.getCarreraConfiguration() == null) {
            throw new ZkConfigException(String.format("[PProxy] CarreraConfiguration not found, nodeId=%s, clusterId=%s", node.getId(), node.getClusterId()));
        }

        Set<String> topics = Sets.newHashSet();
        pProxyConfig.setTopics(topics);

        List<TopicConf> topicConfList = topicConfService.findByClusterId(cluster.getId());
        if (CollectionUtils.isNotEmpty(topicConfList)) {
            List<Topic> topicList = topicService.findByClusterId(cluster.getId());
            Map<Long, Topic> topicMap = Maps.newHashMap();
            topicList.forEach(t -> topicMap.put(t.getId(), t));

            for (TopicConf topicConf : topicConfList) {
                if (!topicMap.containsKey(topicConf.getTopicId())) {
                    throw new ZkConfigException(String.format("[PProxy] clusterId(%s) not found topic, topicConfId=%s, topicId=%s", cluster.getId(), topicConf.getId(), topicConf.getTopicId()));
                }

                if (!mqServerTypeTable.containsKey(topicConf.getMqServerName())) {
                    throw new ZkConfigException(String.format("[PProxy] clusterId(%s) not found mqserverId, topicConfId=%s, mqServerId=%s", cluster.getId(), topicConf.getId(), topicConf.getMqServerId()));
                }

                if (pProxyConfig.getBrokerClusters().contains(topicConf.getMqServerName())) {
                    topics.add(topicConf.getTopicName());
                }
            }
        }
        return pProxyConfig;
    }

    private String getPProxyCluster(String clusterName) {
        return "P_" + clusterName;
    }

    private String getCProxyCluster(String clusterName) {
        return "C_" + clusterName;
    }

    private void updateBrokerConfigByMqserverId(List<ClusterMqserverRelation> relationList) throws Exception {
        for (ClusterMqserverRelation relation : relationList) {
            updateBrokerConfig(relation.getMqServerId());
        }
    }

    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void updateCProxyConfig(Long nodeId) throws Exception {
        Node node = nodeService.findById(nodeId);
        if (node == null) {
            LOGGER.warn("[ZK_V4_CProxy] node not found, nodeId={}", nodeId);
            throw new ZkConfigException(String.format("[CProxy] node not found, nodeId=%s", nodeId));
        }

        if (node.getNodeType() != NodeType.CONSUMER_PROXY.getIndex()) {
            LOGGER.warn("[ZK_V4_CProxy] node isn't cproxy node, nodeId={}", nodeId);
            throw new ZkConfigException(String.format("[CProxy] node isn't cproxy node, nodeId=%s", nodeId));
        }

        Cluster cluster = clusterService.findById(node.getClusterId());
        if (cluster == null || cluster.getIsDelete() == IsDelete.YES.getIndex()) {
            LOGGER.warn(String.format("[ZK_V4_CProxy] not found cluster, nodeId=%s, clusterId=%s", nodeId, node.getClusterId()));
            throw new ZkConfigException(String.format("[CProxy] not found cluster, nodeId=%s, clusterId=%s", nodeId, node.getClusterId()));
        }

        List<ClusterMqserverRelation> relationList = mqserverRelationService.findByClusterId(cluster.getId(), ClusterMqServerRelationType.C_PROXY);
        if (CollectionUtils.isEmpty(relationList)) {
            LOGGER.warn(String.format("[ZK_V4_CProxy] not found ClusterMqserverRelation, nodeId=%s, clusterId=%s", nodeId, node.getClusterId()));
            throw new ZkConfigException(String.format("[CProxy] not found ClusterMqserverRelation, nodeId=%s, clusterId=%s", nodeId, node.getClusterId()));
        }

        String host = HostUtils.getIpPortFromHost(node.getHost(), DEFAULT_CPROXY_PORT);
        CProxyConfig zkCProxyConfig = zkService.getCProxy(host);
        if (node.getIsDelete() == IsDelete.YES.getIndex()) {
            LOGGER.info("[ZK_V4_CProxy] node is deleted, delete it from zk, nodeId={}, clusterId={}, host={}", nodeId, node.getClusterId(), node.getHost());
            if (zkCProxyConfig != null) {
                updateBrokerConfigByMqserverId(relationList);
                zkService.deleteCProxy(host);
            }
            return;
        }

        CProxyConfig cProxyConfig = buildCProxyConfig(host, cluster, relationList);
        zkService.createOrUpdateCProxy(cProxyConfig);
        if (zkCProxyConfig == null) {
            updateBrokerConfigByMqserverId(relationList);
        }
        LOGGER.debug("[ZK_V4_CProxy] update cproxy success, nodeId={}, clusterId={}, host={}, cProxyConfig={}", nodeId, node.getClusterId(), node.getHost(), cProxyConfig);
        LOGGER.info("[ZK_V4_CProxy] update cproxy success, nodeId={}, clusterId={}, host={}", nodeId, node.getClusterId(), node.getHost());
    }

    private CProxyConfig buildCProxyConfig(String host, Cluster cluster, List<ClusterMqserverRelation> relationList) throws ZkConfigException {
        CProxyConfig cProxyConfig = new CProxyConfig();
        cProxyConfig.setInstance(host);
        cProxyConfig.setProxyCluster(getCProxyCluster(cluster.getName()));
        cProxyConfig.setBrokerClusters(relationList.stream().map(ClusterMqserverRelation::getMqServerName).collect(Collectors.toList()));

        Map<String/*proxyCluster*/, Set<String>> pproxies = Maps.newHashMap();

        List<Node> pNodes = nodeService.findByClusterIdNodeType(cluster.getId(), NodeType.PRODUCER_PROXY);
        if (CollectionUtils.isNotEmpty(pNodes)) {
            for (Node pNode : pNodes) {
                pproxies.computeIfAbsent(getPProxyCluster(cluster.getName()), s -> Sets.newHashSet()).add(pNode.getHost() + ":" + DEFAULT_PPROXY_PORT);
            }
        }

        Map<Long, MqServer> mqServerTypeTable = getMqServerMap();
        Map<String/*brokerCluster*/, KafkaConfiguration> kafkaConfigs = Maps.newHashMap();
        Map<String/*brokerCluster*/, com.xiaojukeji.carrera.config.v4.cproxy.RocketmqConfiguration> rocketmqConfigs = Maps.newHashMap();

        cProxyConfig.setKafkaConfigs(kafkaConfigs);
        cProxyConfig.setRocketmqConfigs(rocketmqConfigs);
        for (ClusterMqserverRelation relation : relationList) {
            if (!mqServerTypeTable.containsKey(relation.getMqServerId())) {
                throw new ZkConfigException(String.format("[CProxy] relationId<%s> not found mqServerId<%s>", relation.getId(), relation.getMqServerId()));
            }

            MqServer mqServer = mqServerTypeTable.get(relation.getMqServerId());

            if (mqServerTypeTable.get(relation.getMqServerId()).getType() == MqServerType.ROCKETMQ.getIndex()) {
                rocketmqConfigs.put(mqServer.getName(), getCproxyRocketmqConfiguration(mqServer, relation));
            } else {
                kafkaConfigs.put(mqServer.getName(), getCproxyKafkaConfiguration(mqServer, relation));
            }
        }

        cProxyConfig.setThriftServer(new ConsumeServerConfiguration());
        Set<String> groups = Sets.newHashSet();
        cProxyConfig.setGroups(groups);

        List<ConsumeGroup> groupList = consumeGroupService.findByClusterId(cluster.getId());
        Map<Long, ConsumeGroup> groupMap = Maps.newHashMap();
        groupList.forEach(group -> groupMap.put(group.getId(), group));

        List<ConsumeSubscription> subList = consumeSubscriptionService.findEnableByClusterId(cluster.getId());
        if (CollectionUtils.isNotEmpty(subList)) {
            Map<String, List<TopicConf>> topicConfMap = findTopicConfMap(subList);
            for (ConsumeSubscription sub : subList) {
                String topicConfMapKey = getTopicConfMapKey(sub.getTopicId(), sub.getClusterId());
                if (!topicConfMap.containsKey(topicConfMapKey) || CollectionUtils.isEmpty(topicConfMap.get(topicConfMapKey))) {
                    throw new ZkConfigException(String.format("[CProxy] subId(%s) not found topicConf, topicId=%s, clusterId=%s", sub.getId(), sub.getTopicId(), sub.getClusterId()));
                }

                if (!groupMap.containsKey(sub.getGroupId())) {
                    throw new ZkConfigException(String.format("[CProxy] subId(%s) not found group, groupId=%s", sub.getId(), sub.getGroupId()));
                }
                for (TopicConf topicConf : topicConfMap.get(topicConfMapKey)) {
                    if (cProxyConfig.getBrokerClusters().contains(topicConf.getMqServerName())) {
                        groups.add(sub.getGroupName());
                    }
                }
            }
        }
        return cProxyConfig;
    }

    private KafkaConfiguration getCproxyKafkaConfiguration(MqServer mqServer, ClusterMqserverRelation relation) throws ZkConfigException {
        KafkaConfiguration kafkaConfig = FastJsonUtils.toObject(relation.getProxyConf(), KafkaConfiguration.class);
        if (kafkaConfig == null) {
            LOGGER.warn("[CProxy] kafka cproxyConf convert failed, relationId={}, cproxyConf={}", relation.getId(), relation.getProxyConf());
            throw new ZkConfigException(String.format("[CProxy] relationId<%s> invalid kafka cproxyConf", relation.getId()));
        }

        kafkaConfig.setClusterName(mqServer.getName());
        kafkaConfig.setZookeeperAddr(mqServer.getAddr());
        return kafkaConfig;
    }

    private com.xiaojukeji.carrera.config.v4.cproxy.RocketmqConfiguration getCproxyRocketmqConfiguration(MqServer mqServer, ClusterMqserverRelation relation) throws ZkConfigException {
        com.xiaojukeji.carrera.config.v4.cproxy.RocketmqConfiguration rocketmqConfig = FastJsonUtils.toObject(relation.getProxyConf(), com.xiaojukeji.carrera.config.v4.cproxy.RocketmqConfiguration.class);
        if (rocketmqConfig == null) {
            LOGGER.warn("[CProxy] rmq cproxyConf convert failed, relationId={}, cproxyConf={}", relation.getId(), relation.getProxyConf());
            throw new ZkConfigException(String.format("[CProxy] relationId<%s> invalid rmq cproxyConf", relation.getId()));
        }

        rocketmqConfig.setClusterName(mqServer.getName());
        rocketmqConfig.setNamesrvAddrs(Lists.newArrayList(mqServer.getAddr().split(";")));
        return rocketmqConfig;
    }


    @Override
    public void updateBrokerConfig(Long mqServerId) throws Exception {
        MqServer mqServer = mqServerService.findById(mqServerId);
        if (mqServer == null) {
            LOGGER.warn("[ZK_V4_Broker] mqServer not found, mqServerId={}", mqServerId);
            throw new ZkConfigException(String.format("[Broker] mqServer not found, mqServerId=%s", mqServerId));
        }

        if (mqServer.getIsDelete() == IsDelete.YES.getIndex()) {
            LOGGER.warn("[ZK_V4_Broker] mqServer is deleted, delete it from zk, mqServerId={}, mqServer={}", mqServerId, mqServer.getName());
            zkService.deleteBroker(mqServer.getName());
            return;
        }

        BrokerConfig brokerConfig = new BrokerConfig();
        brokerConfig.setBrokerCluster(mqServer.getName());
        brokerConfig.setBrokerClusterAddrs(mqServer.getAddr());
        Map<String, Set<String>> brokers = Maps.newHashMap();
        Map<String/*proxyCluster*/, Set<String>> pproxies = Maps.newHashMap();
        Map<String/*proxyCluster*/, Set<String>> cproxies = Maps.newHashMap();
        brokerConfig.setBrokers(brokers);
        brokerConfig.setPproxies(pproxies);
        brokerConfig.setCproxies(cproxies);

        List<ClusterMqserverRelation> relationList = mqserverRelationService.findByMqServerId(mqServerId);
        if (CollectionUtils.isEmpty(relationList)) {
            zkService.createOrUpdateBroker(brokerConfig);
            return;
        }

        Map<Long, Cluster> clusterMap = getClusterMap();
        for (ClusterMqserverRelation relation : relationList) {
            Long clusterId = relation.getClusterId();
            if (!clusterMap.containsKey(clusterId)) {
                LOGGER.warn("[ZK_V4_Broker] relation cluster not found, relationId={}, cluster={}", relation.getId(), relation.getClusterName());
                throw new ZkConfigException(String.format("[Broker] relation cluster not found, relationId=%s, cluster=%s", relation.getId(), relation.getClusterName()));
            }

            List<Node> nodeList = nodeService.findByClusterId(clusterId);
            if (CollectionUtils.isEmpty(nodeList)) {
                continue;
            }

            for (Node node1 : nodeList) {
                if (node1.getNodeType() == NodeType.ROCKETMQ_BROKER_MASTER.getIndex()) {
                    String host = node1.getHost() + ":" + DEFAULT_BROKER_PORT;
                    if (!brokers.containsKey(host)) {
                        brokers.put(host, Sets.newHashSet());
                    }
                }
            }

            for (Node node : nodeList) {
                NodeType type = NodeType.getByIndex(node.getNodeType());
                if (type == null) {
                    throw new ZkConfigException(String.format("Unsuport Node Type, node=%s", node.toString()));
                }
                switch (type) {
                    case PRODUCER_PROXY:
                        pproxies.computeIfAbsent(getPProxyCluster(relation.getClusterName()), s -> Sets.newHashSet()).add(node.getHost() + ":" + DEFAULT_PPROXY_PORT);
                        break;
                    case CONSUMER_PROXY:
                        cproxies.computeIfAbsent(getCProxyCluster(relation.getClusterName()), s -> Sets.newHashSet()).add(node.getHost() + ":" + DEFAULT_CPROXY_PORT);
                        break;
                    case ROCKETMQ_BROKER_SLAVE:
                        Optional<Node> masterNode = nodeList.stream().filter(n -> node.getMasterId().equals(n.getId())).findFirst();
                        if (masterNode.isPresent()) {
                            String key = masterNode.get().getHost() + ":" + DEFAULT_BROKER_PORT;
                            if (brokers.containsKey(key)) {
                                brokers.get(key).add(node.getHost() + ":" + DEFAULT_BROKER_PORT);
                            } else {
                                LOGGER.warn("[ZK_V4_Broker] brokers can't find master node<{}>, curNode<{}>", key, node.getId());
                                throw new ZkConfigException("slave node:" + node.getId() + " can't find master node:" + key);
                            }
                        } else {
                            LOGGER.warn("[ZK_V4_Broker] slave node<{}> can't find master node<{}>", node.getId(), node.getMasterId());
                            throw new ZkConfigException("unknown slave node:" + node.getId());
                        }
                        break;
                }
            }

            zkService.createOrUpdateBroker(brokerConfig);
        }
    }
}