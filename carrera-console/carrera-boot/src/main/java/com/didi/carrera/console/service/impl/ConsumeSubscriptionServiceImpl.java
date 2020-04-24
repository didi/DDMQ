package com.didi.carrera.console.service.impl;

import com.didi.carrera.console.common.util.CacheLockUtils;
import com.didi.carrera.console.common.util.HostUtils;
import com.didi.carrera.console.dao.DaoUtil;
import com.didi.carrera.console.dao.dict.ConsumeSubscriptionMsgPushType;
import com.didi.carrera.console.dao.dict.IsDelete;
import com.didi.carrera.console.dao.dict.IsEnable;
import com.didi.carrera.console.dao.dict.NodeType;
import com.didi.carrera.console.dao.mapper.ConsumeSubscriptionMapper;
import com.didi.carrera.console.dao.mapper.custom.ConsumeSubscriptionCustomMapper;
import com.didi.carrera.console.dao.model.Cluster;
import com.didi.carrera.console.dao.model.ConsumeGroup;
import com.didi.carrera.console.dao.model.ConsumeSubscription;
import com.didi.carrera.console.dao.model.ConsumeSubscriptionCriteria;
import com.didi.carrera.console.dao.model.Node;
import com.didi.carrera.console.dao.model.Topic;
import com.didi.carrera.console.dao.model.custom.ConsumeSubscriptionConfig;
import com.didi.carrera.console.dao.model.custom.CustomConsumeSubscription;
import com.didi.carrera.console.dao.model.custom.CustomSubscriptionStateCount;
import com.didi.carrera.console.service.ClusterService;
import com.didi.carrera.console.service.ConsumeGroupService;
import com.didi.carrera.console.service.ConsumeSubscriptionService;
import com.didi.carrera.console.service.NodeService;
import com.didi.carrera.console.service.OffsetManagerService;
import com.didi.carrera.console.service.TopicService;
import com.didi.carrera.console.service.ZKV4ConfigService;
import com.didi.carrera.console.service.exception.ZkConfigException;
import com.didi.carrera.console.service.bean.PageModel;
import com.didi.carrera.console.service.vo.SearchItemVo;
import com.didi.carrera.console.service.vo.SubscriptionOrderListVo;
import com.didi.carrera.console.web.ConsoleBaseResponse;
import com.didi.carrera.console.web.controller.bo.ConsumeGroupResetOffsetBo;
import com.didi.carrera.console.web.controller.bo.ConsumeSubscriptionOrderBo;
import com.didi.carrera.console.web.controller.bo.NodeBo;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.xiaojukeji.carrera.config.Actions;
import com.xiaojukeji.carrera.config.AppendContext;
import com.xiaojukeji.carrera.config.v4.GroupConfig;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;


@Service("didiConsumeSubscriptionServiceImpl")
@EnableTransactionManagement
public class ConsumeSubscriptionServiceImpl implements ConsumeSubscriptionService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConsumeSubscriptionServiceImpl.class);

    @Autowired
    private ConsumeSubscriptionCustomMapper consumeSubscriptionCustomMapper;

    @Autowired
    private ConsumeSubscriptionMapper consumeSubscriptionMapper;

    @Resource(name = "didiConsumeGroupServiceImpl")
    private ConsumeGroupService consumeGroupService;

    @Resource(name = "didiOffsetManagerServiceImpl")
    private OffsetManagerService offsetManagerService;

    @Autowired
    private ZKV4ConfigService zkv4ConfigService;

    @Resource(name = "didiTopicServiceImpl")
    private TopicService topicService;

    @Resource(name = "didiClusterServiceImpl")
    private ClusterService clusterService;

    @Resource(name = "didiNodeServiceImpl")
    private NodeService nodeService;

    @Override
    public ConsoleBaseResponse<?> validateConsumeSubscriptionBo(ConsumeSubscriptionOrderBo subscriptionBo) throws ZkConfigException {
        Topic topic = topicService.findById(subscriptionBo.getTopicId());
        if (topic == null) {
            LOGGER.info("validate error:topic not exist <topicId:{}>", subscriptionBo.getTopicId());
            return ConsoleBaseResponse.error(ConsoleBaseResponse.Status.INVALID_PARAM, "topic not exist");
        }

        subscriptionBo.setTopicName(topic.getTopicName());

        ConsumeGroup group = consumeGroupService.findById(subscriptionBo.getGroupId());
        if (group == null) {
            LOGGER.info("validate error:group not exist <groupId:{}>", subscriptionBo.getGroupId());
            return ConsoleBaseResponse.error(ConsoleBaseResponse.Status.INVALID_PARAM, "group not exist");
        }

        subscriptionBo.setGroupName(group.getGroupName());

        for (Long clusterId : subscriptionBo.getClusters().values()) {
            Cluster cluster = clusterService.findById(clusterId);
            if (cluster == null) {
                LOGGER.info("validate error:cluster not exist <clusterId:{}>", clusterId);
                return ConsoleBaseResponse.error(ConsoleBaseResponse.Status.INVALID_PARAM, "cluster<" + subscriptionBo.getClusters().get(clusterId) + ">not exist");
            }

            subscriptionBo.getClusters().put(cluster.getName(), clusterId);
        }

        if (!subscriptionBo.isModify()) {
            for (Long clusterId : subscriptionBo.getClusters().values()) {
                if (findByIds(subscriptionBo.getGroupId(), clusterId, subscriptionBo.getTopicId()) != null) {
                    LOGGER.info("validate error:subscription exist <groupId:{}, clusterId:{}, topicId:{}>", subscriptionBo.getGroupId(), clusterId, subscriptionBo.getTopicId());
                    return ConsoleBaseResponse.error(ConsoleBaseResponse.Status.INVALID_PARAM, "subscription existed");
                }
            }
        } else {
            ConsumeSubscription dbSub = findById(subscriptionBo.getSubId());
            if (dbSub == null || dbSub.getIsDelete() == IsDelete.YES.getIndex()) {
                return ConsoleBaseResponse.error(ConsoleBaseResponse.Status.INVALID_PARAM, "subscription not found");
            }

            if (IsEnable.isEnable(dbSub.getState())) {
                validateMixAction(subscriptionBo.getSubId(), subscriptionBo.getGroupId(), subscriptionBo.buildActions());
            }
        }

        com.xiaojukeji.carrera.config.v4.cproxy.UpstreamTopic upstreamTopic = buildUpstreamTopicV4(subscriptionBo);

        try {
            if (!upstreamTopic.validate()) {
                return ConsoleBaseResponse.error(ConsoleBaseResponse.Status.INVALID_PARAM, "UpstreamTopic validate Failed");
            }
        } catch (Exception e) {
            LOGGER.error("validate error:" + e.getMessage(), e);
            return ConsoleBaseResponse.error(ConsoleBaseResponse.Status.INVALID_PARAM, e.getMessage());
        }

        return ConsoleBaseResponse.success();
    }

    private com.xiaojukeji.carrera.config.v4.cproxy.UpstreamTopic buildUpstreamTopicV4(ConsumeSubscriptionOrderBo subscriptionBo) throws ZkConfigException {
        ConsumeSubscription subscription = subscriptionBo.buildConsumeSubscription();
        subscription.setState(IsEnable.DISABLE.getIndex());

        ConsumeSubscriptionConfig config = new ConsumeSubscriptionConfig();
        subscription.setConsumeSubscriptionConfig(config);
        return zkv4ConfigService.buildUpstreamTopic(new GroupConfig(), subscription, "0");
    }

    @Override
    public List<ConsumeSubscription> findAll() {
        ConsumeSubscriptionCriteria csc = new ConsumeSubscriptionCriteria();
        csc.createCriteria().andIsDeleteEqualTo(IsDelete.NO.getIndex());
        return consumeSubscriptionMapper.selectByExampleWithBLOBs(csc);
    }

    private void initProxies(ConsumeSubscription sub) {
        ConsumeGroup group = consumeGroupService.findById(sub.getGroupId());
        if (group == null) {
            throw new RuntimeException("group not found, subId=" + sub.getId());
        }
        doInitProxies(sub);
    }

    private void doInitProxies(ConsumeSubscription sub) {
        ConsumeSubscriptionConfig config = sub.getConsumeSubscriptionConfig();
        if (config.getProxies() == null) {
            config.setProxies(Maps.newHashMap());
        }
        config.getProxies().clear();

        List<Node> nodeList;
        String clusterName = sub.getClusterName();

        nodeList = nodeService.findByClusterIdNodeType(sub.getClusterId(), NodeType.CONSUMER_PROXY);

        if (CollectionUtils.isNotEmpty(nodeList)) {
            Set<String> hostSet = nodeList.stream().map(n -> HostUtils.getIpPortFromHost(n.getHost(), ZKV4ConfigServiceImpl.DEFAULT_CPROXY_PORT)).collect(Collectors.toSet());
            config.getProxies().computeIfAbsent("C_" + clusterName, s -> Sets.newHashSet()).addAll(hostSet);
        }
    }

    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public ConsoleBaseResponse<?> addCProxy(String clusterName, String host) throws Exception {
        Cluster cluster = clusterService.findByClusterName(clusterName);
        if (cluster == null) {
            return ConsoleBaseResponse.error(ConsoleBaseResponse.Status.INVALID_PARAM, "cluster not found");
        }

        if (!validNodeExist(host, cluster, NodeType.CONSUMER_PROXY)) {
            return ConsoleBaseResponse.error(ConsoleBaseResponse.Status.INVALID_PARAM, "host not found");
        }

        List<ConsumeSubscription> subscriptionList = findByClusterId(cluster.getId());
        if (CollectionUtils.isEmpty(subscriptionList)) {
            return ConsoleBaseResponse.success();
        }
        addCProxy(host, subscriptionList);

        return ConsoleBaseResponse.success();
    }


    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public ConsoleBaseResponse<?> addCProxy(String groupName, String clusterName, String host) throws Exception {
        ConsumeGroup group = consumeGroupService.findByGroupName(groupName);
        if (group == null) {
            return ConsoleBaseResponse.error(ConsoleBaseResponse.Status.INVALID_PARAM, "group not found");
        }

        Cluster cluster = clusterService.findByClusterName(clusterName);
        if (cluster == null) {
            return ConsoleBaseResponse.error(ConsoleBaseResponse.Status.INVALID_PARAM, "cluster not found");
        }

        if (!validNodeExist(host, cluster, NodeType.CONSUMER_PROXY)) {
            return ConsoleBaseResponse.error(ConsoleBaseResponse.Status.INVALID_PARAM, "host not found");
        }

        List<ConsumeSubscription> subscriptionList = findByNotNullGroupClusterTopicId(group.getId(), cluster.getId(), null);
        if (CollectionUtils.isEmpty(subscriptionList)) {
            return ConsoleBaseResponse.error(ConsoleBaseResponse.Status.INVALID_PARAM, "subscription not found");
        }
        addCProxy(host, subscriptionList);

        return ConsoleBaseResponse.success();
    }

    private boolean validNodeExist(String host, Cluster cluster, NodeType nodeType) {
        List<Node> nodeList = nodeService.findByClusterHostNodeType(cluster.getId(), host, nodeType);
        if (CollectionUtils.isEmpty(nodeList)) {
            String ip = HostUtils.getIp(host);
            if (host.equals(ip)) {
                return createCProxyNode(host, cluster, nodeType);
            }
            nodeList = nodeService.findByClusterHostNodeType(cluster.getId(), host, nodeType);
            if (CollectionUtils.isEmpty(nodeList)) {
                return createCProxyNode(host, cluster, nodeType);
            }
        }
        return true;
    }

    private boolean createCProxyNode(String host, Cluster cluster, NodeType nodeType) {
        try {
            NodeBo nodeBo = new NodeBo();
            nodeBo.setClusterid(cluster.getId());
            nodeBo.setHost(host);
            nodeBo.setNodetype(nodeType.getIndex());
            nodeService.create(nodeBo);
            return true;
        } catch (Exception e) {
            LOGGER.error("create node error {}, {}", host, cluster);
            return false;
        }
    }

    private void addCProxy(String host, List<ConsumeSubscription> subList) throws Exception {
        String ipPort = HostUtils.getIpPortFromHost(host, ZKV4ConfigServiceImpl.DEFAULT_CPROXY_PORT);
        Set<Long> clusterIds = Sets.newHashSet();
        for (ConsumeSubscription sub : subList) {
            if (sub.getConsumeSubscriptionConfig() != null && MapUtils.isNotEmpty(sub.getConsumeSubscriptionConfig().getProxies())) {
                Map<String, Set<String>> proxyMap = sub.getConsumeSubscriptionConfig().getProxies();
                for (Map.Entry<String, Set<String>> entry : proxyMap.entrySet()) {
                    Set<String> ipLists = entry.getValue();
                    if (!ipLists.contains(ipPort)) {
                        ipLists.add(ipPort);
                        clusterIds.add(sub.getClusterId());
                        updateByPrimaryKey(sub);

                        pushV4ZkInfo(sub.getGroupId(), null);
                        LOGGER.info("add cproxy {} success, subId={}, group={}, topic={}, cluster={}", ipPort, sub.getId(), sub.getGroupName(), sub.getTopicName(), sub.getClusterName());
                    }
                }
            }
        }
        if (CollectionUtils.isNotEmpty(clusterIds)) {
            zkv4ConfigService.updateCProxyConfigByClusterId("addCProxy", clusterIds);
        }
    }

    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public ConsoleBaseResponse<?> removeCProxy(String host) throws Exception {
        List<ConsumeSubscription> subList = findAll();
        removeCProxy(host, subList);

        return ConsoleBaseResponse.success();
    }

    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public ConsoleBaseResponse<?> removeCProxy(String groupName, String host) throws Exception {
        ConsumeGroup group = consumeGroupService.findByGroupName(groupName);
        if (group == null) {
            return ConsoleBaseResponse.error(ConsoleBaseResponse.Status.INVALID_PARAM, "groupName not found");
        }
        List<ConsumeSubscription> subList = findByGroupId(group.getId());
        if (CollectionUtils.isEmpty(subList)) {
            return ConsoleBaseResponse.error(ConsoleBaseResponse.Status.INVALID_PARAM, "subscription not found");
        }
        removeCProxy(host, subList);

        return ConsoleBaseResponse.success();
    }

    private void removeCProxy(String host, List<ConsumeSubscription> subList) throws Exception {
        String ipPort = HostUtils.getIpPortFromHost(host, ZKV4ConfigServiceImpl.DEFAULT_CPROXY_PORT);
        Set<Long> clusterIds = Sets.newHashSet();
        for (ConsumeSubscription sub : subList) {
            if (sub.getConsumeSubscriptionConfig() != null && MapUtils.isNotEmpty(sub.getConsumeSubscriptionConfig().getProxies())) {
                Map<String, Set<String>> proxyMap = sub.getConsumeSubscriptionConfig().getProxies();
                for (Set<String> ipLists : proxyMap.values()) {
                    if (ipLists.contains(ipPort)) {
                        ipLists.remove(ipPort);
                        clusterIds.add(sub.getClusterId());
                        updateByPrimaryKey(sub);

                        pushV4ZkInfo(sub.getGroupId(), null);
                        LOGGER.info("remove cproxy {} success, subId={}, group={}, topic={}, cluster={}", ipPort, sub.getId(), sub.getGroupName(), sub.getTopicName(), sub.getClusterName());
                    }
                }
            }
        }
        if (CollectionUtils.isNotEmpty(clusterIds)) {
            zkv4ConfigService.updateCProxyConfigByClusterId("removeCProxy", clusterIds);
        }
    }

    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public ConsoleBaseResponse<?> createConsumeSubscription(ConsumeSubscriptionOrderBo subscriptionBo) throws Exception {
        ConsoleBaseResponse<?> validateBo = validateConsumeSubscriptionBo(subscriptionBo);
        if (!validateBo.isSuccess()) {
            return validateBo;
        }

        ConsumeSubscription subscription = subscriptionBo.buildConsumeSubscription();

        if (!subscriptionBo.isModify()) {
            for (Map.Entry<String, Long> entry : subscriptionBo.getClusters().entrySet()) {
                Long clusterId = entry.getValue();
                String clusterName = entry.getKey();
                subscription.setClusterId(clusterId);
                subscription.setClusterName(clusterName);

                subscription.setId(null);
                ConsumeSubscriptionConfig config = new ConsumeSubscriptionConfig();
                if (subscription.getSubActions().contains(Actions.PULL_SERVER)) {
                    config.setAppendContext(null);
                }
                config.setNeedResetOffset(true);

                subscription.setConsumeSubscriptionConfig(config);
                subscription.setIsDelete(IsDelete.NO.getIndex());
                subscription.setCreateTime(new Date());
                //创建默认禁用
                subscription.setState(IsEnable.DISABLE.getIndex());

                initProxies(subscription);

                consumeSubscriptionMapper.insertSelective(subscription);
            }
        } else {
            subscription.setId(subscriptionBo.getSubId());

            subscription.setGroupName(null);
            subscription.setTopicName(null);
            subscription.setClusterName(null);
            subscription.setClusterId(null);
            ConsumeSubscription dbSub = findById(subscriptionBo.getSubId());
            if (dbSub == null) {
                return ConsoleBaseResponse.error(ConsoleBaseResponse.Status.INVALID_PARAM, "subId<" + subscriptionBo.getSubId() + "> not found");
            }

            updateSubConfig(subscription, dbSub);
        }

        pushV4ZkInfo(subscription.getGroupId(), null);
        return ConsoleBaseResponse.success();
    }

    private void updateSubConfig(ConsumeSubscription subscription, ConsumeSubscription dbSub) throws Exception {
        ConsumeSubscriptionConfig config = dbSub.getConsumeSubscriptionConfig();
        if (dbSub.getSubActions().contains(Actions.ASYNC_HTTP) && subscription.getSubActions().contains(Actions.PULL_SERVER)) {
            config.setAppendContext(null);
        } else if (dbSub.getSubActions().contains(Actions.PULL_SERVER) && subscription.getSubActions().contains(Actions.ASYNC_HTTP)) {
            config.setAppendContext(Lists.newArrayList(AppendContext.values()));
        } else if (dbSub.getSubActions().contains(Actions.ASYNC_HTTP) && subscription.getSubActions().contains(Actions.ASYNC_HTTP)) {
            config.setAppendContext(Lists.newArrayList(AppendContext.values()));
        }

        subscription.setConsumeSubscriptionConfig(config);

        updateByPrimaryKey(subscription);
    }

    private void updateByPrimaryKey(ConsumeSubscription subscription) {
        consumeSubscriptionMapper.updateByPrimaryKeySelective(subscription);
    }

    @Override
    public List<ConsumeSubscription> findByNotNullGroupClusterTopicId(Long groupId, Long clusterId, Long topicId) {
        ConsumeSubscriptionCriteria csc = new ConsumeSubscriptionCriteria();
        ConsumeSubscriptionCriteria.Criteria cscc = csc.createCriteria();
        cscc.andIsDeleteEqualTo(IsDelete.NO.getIndex());
        if (groupId != null) {
            cscc.andGroupIdEqualTo(groupId);
        }
        if (clusterId != null) {
            cscc.andClusterIdEqualTo(clusterId);
        }
        if (topicId != null) {
            cscc.andTopicIdEqualTo(topicId);
        }

        return consumeSubscriptionMapper.selectByExampleWithBLOBs(csc);
    }

    @Override
    public List<ConsumeSubscription> findEnableByClusterId(Long clusterId) {
        ConsumeSubscriptionCriteria csc = new ConsumeSubscriptionCriteria();
        csc.createCriteria().andIsDeleteEqualTo(IsDelete.NO.getIndex()).andClusterIdEqualTo(clusterId).andStateEqualTo(IsEnable.ENABLE.getIndex());
        return consumeSubscriptionMapper.selectByExampleWithBLOBs(csc);
    }

    @Override
    public List<ConsumeSubscription> findEnableByGroupId(Long groupId) {
        ConsumeSubscriptionCriteria csc = new ConsumeSubscriptionCriteria();
        csc.createCriteria().andIsDeleteEqualTo(IsDelete.NO.getIndex()).andGroupIdEqualTo(groupId).andStateEqualTo(IsEnable.ENABLE.getIndex());
        return consumeSubscriptionMapper.selectByExampleWithBLOBs(csc);
    }

    @Override
    public List<ConsumeSubscription> findByClusterId(Long clusterId) {
        ConsumeSubscriptionCriteria csc = new ConsumeSubscriptionCriteria();
        csc.createCriteria().andIsDeleteEqualTo(IsDelete.NO.getIndex()).andClusterIdEqualTo(clusterId);
        return consumeSubscriptionMapper.selectByExampleWithBLOBs(csc);
    }

    @Override
    public List<CustomConsumeSubscription> findByTopicIdClusterId(Long topicId, Long clusterId) {
        return consumeSubscriptionCustomMapper.selectByTopicIdClusterId(topicId, clusterId);
    }

    @Override
    public ConsumeSubscription findById(Long subId) {
        return consumeSubscriptionMapper.selectByPrimaryKey(subId);
    }

    private Map<Long, Cluster> getClusterMap() {
        Map<Long, Cluster> clusterMap = Maps.newHashMap();
        clusterService.findAll().forEach(cluster -> clusterMap.put(cluster.getId(), cluster));
        return clusterMap;
    }

    @Override
    public ConsoleBaseResponse<PageModel<SubscriptionOrderListVo>> findAll(String user, String text, Long clusterId, Long groupId, Integer consumeType, Integer state, Integer curPage, Integer pageSize) {
        return findAllByCondition(user, text, clusterId, groupId, consumeType, state, curPage, pageSize);
    }

    private ConsoleBaseResponse<PageModel<SubscriptionOrderListVo>> findAllByCondition(String user, String text, Long clusterId, Long groupId, Integer consumeType, Integer state, Integer curPage, Integer pageSize) {

        if (StringUtils.isNotEmpty(user)) {
            user = DaoUtil.getLikeField(user);
        }
        if (StringUtils.isNotEmpty(text)) {
            text = DaoUtil.getLikeField(text);
        }

        Integer totalCount = consumeSubscriptionCustomMapper.selectCountByCondition(user, groupId, clusterId, consumeType, state, text);
        PageModel<SubscriptionOrderListVo> pageModel = new PageModel<>(curPage, pageSize, totalCount);
        if (totalCount == 0) {
            pageModel.setList(Collections.emptyList());
            return ConsoleBaseResponse.success(pageModel);
        }

        List<ConsumeSubscription> list = consumeSubscriptionCustomMapper.selectByCondition(user, groupId, clusterId, consumeType, state, text, pageModel.getPageIndex(), pageModel.getPageSize());
        if (CollectionUtils.isEmpty(list)) {
            pageModel.setList(Collections.emptyList());
            return ConsoleBaseResponse.success(pageModel);
        }

        Map<Long, Cluster> clusterMap = getClusterMap();

        List<SubscriptionOrderListVo> retList = Lists.newArrayList();
        list.forEach(sub -> retList.add(SubscriptionOrderListVo.buildSubscriptionListVo(sub, clusterMap.get(sub.getClusterId()).getIdcId(), clusterMap.get(sub.getClusterId()).getIdc(), clusterMap.get(sub.getClusterId()).getDescription())));

        pageModel.setList(retList);
        return ConsoleBaseResponse.success(pageModel);
    }

    @Override
    public ConsoleBaseResponse<SubscriptionOrderListVo> findByGroupClusterTopicId(Long groupId, Long clusterId, Long topicId) {
        ConsumeSubscription sub = findByIds(groupId, clusterId, topicId);
        if (sub == null) {
            return ConsoleBaseResponse.error(ConsoleBaseResponse.Status.INVALID_PARAM, "subscription info not found");
        }

        Cluster cluster = clusterService.findById(sub.getClusterId());

        return ConsoleBaseResponse.success(SubscriptionOrderListVo.buildSubscriptionListVo(sub, cluster.getIdcId(), cluster.getIdc(), cluster.getDescription()));
    }

    private ConsumeSubscription findByIds(Long groupId, Long clusterId, Long topicId) {
        ConsumeSubscriptionCriteria csc = new ConsumeSubscriptionCriteria();
        csc.createCriteria().andIsDeleteEqualTo(IsDelete.NO.getIndex()).andGroupIdEqualTo(groupId).andClusterIdEqualTo(clusterId).andTopicIdEqualTo(topicId);

        List<ConsumeSubscription> list = consumeSubscriptionMapper.selectByExampleWithBLOBs(csc);
        if (CollectionUtils.isEmpty(list)) {
            return null;
        }
        return list.get(0);
    }

    private String getStateLockKey(Long id) {
        return "CARRERA_GROUP_STAT_LOCK_" + id;
    }

    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public ConsoleBaseResponse<?> changeState(String user, Long subId, Integer state) throws Exception {
        ConsumeSubscription sub = findById(subId);
        if (!consumeGroupService.validUserExist(user, sub.getGroupId())) {
            return ConsoleBaseResponse.error(ConsoleBaseResponse.Status.INVALID_PARAM, user + "not authorized");
        }
        if (IsEnable.getByIndex(state.byteValue()) == null) {
            return ConsoleBaseResponse.error(ConsoleBaseResponse.Status.INVALID_PARAM, "only 0 or 1");
        }

        return updateSubStateById(sub, state);
    }

    @Transactional(isolation = Isolation.SERIALIZABLE, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public ConsoleBaseResponse<?> changeState(Long groupId, Integer state) throws Exception {
        List<ConsumeSubscription> list = findByGroupId(groupId);
        if (CollectionUtils.isEmpty(list)) {
            return ConsoleBaseResponse.error(ConsoleBaseResponse.Status.INVALID_PARAM, "not subscription info found");
        }
        for (ConsumeSubscription sub : list) {
            updateSubStateById(sub, state);
        }
        return ConsoleBaseResponse.success();
    }

    private ConsoleBaseResponse<?> updateSubStateById(ConsumeSubscription subscription, Integer state) throws Exception {
        if (subscription == null || subscription.getIsDelete() == IsDelete.YES.getIndex()) {
            return ConsoleBaseResponse.error(ConsoleBaseResponse.Status.INVALID_PARAM, "subscription<" + (subscription == null ? "" : subscription.getId()) + ">not found");
        }

        if (subscription.getState() == state.byteValue()) {
            return ConsoleBaseResponse.error(ConsoleBaseResponse.Status.INVALID_PARAM, "subscription<" + subscription.getId() + ">is " + IsEnable.getByIndex(state.byteValue()).getName() + " state");
        }

        if (IsEnable.isEnable(state.byteValue())) {
            validateMixAction(subscription.getId(), subscription.getGroupId(), subscription.getSubActions());
        }


        if (!CacheLockUtils.lock(getStateLockKey(subscription.getGroupId()))) {
            return ConsoleBaseResponse.error(ConsoleBaseResponse.Status.INVALID_PARAM, subscription.getGroupName() + "is" + IsEnable.getByIndex(state.byteValue()).getName() + "");
        }

        try {
            boolean updateConfig = false;
            ConsumeSubscriptionConfig config = subscription.getConsumeSubscriptionConfig();
            if (IsEnable.isEnable(state.byteValue()) && config.isNeedResetOffset()) {
                try {
                    offsetManagerService.resetOffsetToLatest(subscription.getClusterId(), subscription.getGroupId(), subscription.getTopicId());
                } catch (Exception e) {
                    LOGGER.error("resetOffsetToLatest exception", e);
                }

                updateConfig = true;
                config.setNeedResetOffset(false);
            }
            ConsumeSubscription cs = new ConsumeSubscription();
            cs.setId(subscription.getId());
            if (updateConfig) {
                cs.setConsumeSubscriptionConfig(config);
            }
            cs.setState(state.byteValue());
            int ret = consumeSubscriptionMapper.updateByPrimaryKeySelective(cs);
            if (ret > 0) {
                Set<Long> clusterIdSet = Sets.newHashSet(subscription.getClusterId());
                pushV4ZkInfo(subscription.getGroupId(), clusterIdSet);
            }
        } finally {
            CacheLockUtils.unlock(getStateLockKey(subscription.getGroupId()));
        }
        return ConsoleBaseResponse.success();
    }

    private void validateMixAction(Long subId, Long groupId, List<String> actions) {
        List<ConsumeSubscription> subList = findEnableByGroupId(groupId);
        if (CollectionUtils.isEmpty(subList)) {
            return;
        }

        int lowLevelCount = 0;
        int httpCount = 0;
        int totalCount = subList.size();
        boolean containsCurSub = false;

        for (ConsumeSubscription sub : subList) {
            if (sub.getId().equals(subId)) {
                containsCurSub = true;
                continue;
            }
            if (sub.getActions().contains(Actions.LowLevel)) {
                lowLevelCount++;
            }
            if (sub.getActions().contains(Actions.ASYNC_HTTP)) {
                httpCount++;
            }
        }

        if (!containsCurSub) {
            totalCount++;
        }

        if (actions.contains(Actions.LowLevel)) {
            lowLevelCount++;
        }

        if (actions.contains(Actions.ASYNC_HTTP)) {
            httpCount++;
        }

        if (lowLevelCount > 0 && lowLevelCount != totalCount) {
            LOGGER.info("validate error:subscription can not mix low-level and non-low-level topics <subId:{}, groupId:{}>", subId, groupId);
            throw new RuntimeException("HighLevel and LowLevel can not mixed");
        }

        if (httpCount > 0 && httpCount != totalCount) {
            LOGGER.info("validate error:subscription can not mix SDK and Http <subId:{}, groupId:{}>", subId, groupId);
            throw new RuntimeException("SDK and Http can not mixed");
        }
    }

    private void pushV4ZkInfo(Long groupId, Set<Long> clusterIdSet) throws Exception {
        zkv4ConfigService.updateSubConfig(groupId, clusterIdSet);
    }

    @Override
    public List<ConsumeSubscription> findByGroupId(Long groupId) {
        ConsumeSubscriptionCriteria csc = new ConsumeSubscriptionCriteria();
        csc.createCriteria().andIsDeleteEqualTo(IsDelete.NO.getIndex()).andGroupIdEqualTo(groupId);

        return consumeSubscriptionMapper.selectByExampleWithBLOBs(csc);
    }

    @Override
    public List<CustomSubscriptionStateCount> findStateCountByGroupId(List<Long> groupIds) {
        if (CollectionUtils.isEmpty(groupIds)) {
            return Collections.emptyList();
        }
        return consumeSubscriptionCustomMapper.selectStateCountByGroupId(groupIds);
    }

    private ConsoleBaseResponse<?> validateConsumeGroupResetOffsetBo(ConsumeGroupResetOffsetBo resetOffsetBo) {
        if (!consumeGroupService.validUserExist(resetOffsetBo.getUser(), resetOffsetBo.getGroupId())) {
            return ConsoleBaseResponse.error(ConsoleBaseResponse.Status.INVALID_PARAM, resetOffsetBo.getUser() + "not authorized");
        }

        ConsumeSubscription sub = findByIds(resetOffsetBo.getGroupId(), resetOffsetBo.getClusterId(), resetOffsetBo.getTopicId());
        if (sub == null) {
            LOGGER.info("validate error:subscription not exist <groupId:{}, clusterId:{}, topicId:{}>", resetOffsetBo.getGroupId(), resetOffsetBo.getClusterId(), resetOffsetBo.getTopicId());
            return ConsoleBaseResponse.error(ConsoleBaseResponse.Status.INVALID_PARAM, "subscription not found");
        }

        if (IsEnable.isEnable(sub.getState())) {
            LOGGER.info("validate error:subscription must is disable <subId:{}, groupId:{}, clusterId:{}, topicId:{}>", sub.getId(), resetOffsetBo.getGroupId(), resetOffsetBo.getClusterId(), resetOffsetBo.getTopicId());
            return ConsoleBaseResponse.error(ConsoleBaseResponse.Status.INVALID_PARAM, "Reset Offset require disable subscription first!");
        }

        if (resetOffsetBo.getResetType().byteValue() == ConsumeSubscriptionResetType.RESET_BY_TIME.getIndex() && resetOffsetBo.getResetTime() == null) {
            return ConsoleBaseResponse.error(ConsoleBaseResponse.Status.INVALID_PARAM, "Please Specify Timestamp");
        }

        if (resetOffsetBo.getResetType().byteValue() == ConsumeSubscriptionResetType.RESET_BY_OFFSET.getIndex() && (resetOffsetBo.getOffset() == null || resetOffsetBo.getOffset().equals(0L))) {
            return ConsoleBaseResponse.error(ConsoleBaseResponse.Status.INVALID_PARAM, "Please Specify Offset Value");
        }

        return ConsoleBaseResponse.success();
    }

    @Override
    public ConsoleBaseResponse<?> resetOffset(ConsumeGroupResetOffsetBo resetOffsetBo) throws Exception {
        ConsoleBaseResponse<?> validateBo = validateConsumeGroupResetOffsetBo(resetOffsetBo);
        if (!validateBo.isSuccess()) {
            return validateBo;
        }

        switch (ConsumeSubscriptionResetType.getByIndex(resetOffsetBo.getResetType().byteValue())) {
            case RESET_TO_LATEST:
                offsetManagerService.resetOffsetToLatest(resetOffsetBo.getClusterId(), resetOffsetBo.getGroupId(), resetOffsetBo.getTopicId());
                break;
            case RESET_BY_TIME:
                offsetManagerService.resetOffsetByTime(resetOffsetBo.getClusterId(), resetOffsetBo.getGroupId(), resetOffsetBo.getTopicId(), resetOffsetBo.getResetTime());
                break;
            case RESET_BY_OFFSET:
                offsetManagerService.resetOffsetByOffset(resetOffsetBo.getClusterId(), resetOffsetBo.getGroupId(), resetOffsetBo.getTopicId(), resetOffsetBo.getQid(), resetOffsetBo.getOffset());
                break;
        }

        return ConsoleBaseResponse.success();
    }

    private boolean deleteById(String user, Long subId) throws Exception {
        ConsumeSubscription sub = new ConsumeSubscription();
        sub.setIsDelete(IsDelete.YES.getIndex());
        sub.setId(subId);
        sub.setRemark(user);
        updateByPrimaryKey(sub);
        return true;
    }

    private boolean deleteByIds(String user, List<Long> subIds) throws Exception {
        ConsumeSubscription sub = new ConsumeSubscription();
        sub.setIsDelete(IsDelete.YES.getIndex());
        sub.setRemark(user);

        ConsumeSubscriptionCriteria csc = new ConsumeSubscriptionCriteria();
        csc.createCriteria().andIdIn(subIds);
        consumeSubscriptionMapper.updateByExampleSelective(sub, csc);
        return true;
    }

    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public ConsoleBaseResponse<?> delete(String user, Long subId) throws Exception {
        ConsumeSubscription sub = findById(subId);
        if (sub == null) {
            return ConsoleBaseResponse.error(ConsoleBaseResponse.Status.INVALID_PARAM, String.format("subId<%s>not found", subId));
        }

        deleteById(user, subId);

        Set<Long> clusterIdSet = Sets.newHashSet(sub.getClusterId());
        pushV4ZkInfo(sub.getGroupId(), clusterIdSet);

        return ConsoleBaseResponse.success();
    }

    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public ConsoleBaseResponse<?> deleteByGroupId(String user, Long groupId) throws Exception {
        List<ConsumeSubscription> list = findByGroupId(groupId);
        if (CollectionUtils.isEmpty(list)) {
            return ConsoleBaseResponse.success();
        }

        deleteByIds(user, list.stream().map(ConsumeSubscription::getId).collect(Collectors.toList()));

        Set<Long> clusterIdSet = list.stream().map(ConsumeSubscription::getClusterId).collect(Collectors.toSet());
        pushV4ZkInfo(groupId, clusterIdSet);

        return ConsoleBaseResponse.success();
    }

    @Override
    public ConsoleBaseResponse<List<SearchItemVo>> findMsgPushType(String user) {
        List<SearchItemVo> list = Lists.newArrayList();
        for (ConsumeSubscriptionMsgPushType type : ConsumeSubscriptionMsgPushType.values()) {
            SearchItemVo vo = new SearchItemVo((long) type.getIndex(), type.getName());
            list.add(vo);
        }
        return ConsoleBaseResponse.success(list);
    }

    public enum ConsumeSubscriptionResetType {
        //1.重置到最新2.按时间点重置 3. 重置到指定offset
        RESET_TO_LATEST((byte) 1, "reset to latest"),
        RESET_BY_TIME((byte) 2, "reset by time"),
        RESET_BY_OFFSET((byte) 3, "reset by offset");

        private byte index;

        private String name;

        ConsumeSubscriptionResetType(byte index, String name) {
            this.index = index;
            this.name = name;
        }

        public byte getIndex() {
            return this.index;
        }

        public void setIndex(byte index) {
            this.index = index;
        }

        public String getName() {
            return this.name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public static ConsumeSubscriptionResetType getByIndex(byte index) {
            List<ConsumeSubscriptionResetType> all = Arrays.asList(values());
            for (ConsumeSubscriptionResetType item : all) {
                if (item.getIndex() == index) {
                    return item;
                }
            }
            return null;
        }
    }
}