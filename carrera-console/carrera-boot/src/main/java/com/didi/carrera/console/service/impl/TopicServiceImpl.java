package com.didi.carrera.console.service.impl;

import com.didi.carrera.console.common.util.HostUtils;
import com.didi.carrera.console.dao.dict.IsDelete;
import com.didi.carrera.console.dao.dict.IsEnable;
import com.didi.carrera.console.dao.dict.MqServerType;
import com.didi.carrera.console.dao.dict.NodeType;
import com.didi.carrera.console.dao.mapper.TopicMapper;
import com.didi.carrera.console.dao.mapper.custom.TopicCustomMapper;
import com.didi.carrera.console.dao.model.*;
import com.didi.carrera.console.dao.model.custom.CustomConsumeSubscription;
import com.didi.carrera.console.dao.model.custom.CustomTopicConf;
import com.didi.carrera.console.dao.model.custom.TopicConfConfig;
import com.didi.carrera.console.dao.model.custom.TopicConfig;
import com.didi.carrera.console.data.Message;
import com.didi.carrera.console.service.*;
import com.didi.carrera.console.service.bean.PageModel;
import com.didi.carrera.console.service.vo.*;
import com.didi.carrera.console.web.ConsoleBaseResponse;
import com.didi.carrera.console.web.controller.bo.AcceptTopicConfBo;
import com.didi.carrera.console.web.controller.bo.NodeBo;
import com.didi.carrera.console.web.controller.bo.TopicConfBo;
import com.didi.carrera.console.web.controller.bo.TopicOrderBo;
import com.didi.carrera.console.web.util.DateUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.common.admin.TopicStatsTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;


@Service("didiTopicServiceImpl")
@EnableTransactionManagement
public class TopicServiceImpl implements TopicService {
    private static final Logger LOGGER = LoggerFactory.getLogger(TopicServiceImpl.class);

    @Autowired
    private TopicMapper topicMapper;

    @Autowired
    private TopicCustomMapper topicCustomMapper;

    @Resource(name = "didiClusterServiceImpl")
    private ClusterService clusterService;

    @Resource(name = "didiConsumeSubscriptionServiceImpl")
    private ConsumeSubscriptionService consumeSubscriptionService;

    @Resource(name = "didiTopicConfServiceImpl")
    private TopicConfService topicConfService;

    @Resource(name = "didiMqServerServiceImpl")
    private MqServerService mqServerService;

    @Autowired
    private ZKV4ConfigService zkv4ConfigService;

    @Resource(name = "didiRmqAdminServiceImpl")
    private RmqAdminService rmqAdminService;

    @Resource(name = "didiNodeServiceImpl")
    private NodeService nodeService;

    public <T extends TopicConfBo> ConsoleBaseResponse<?> validateTopicBo(TopicOrderBo<T> topicInfo) {
        if (!topicInfo.isModify() && CollectionUtils.isNotEmpty(findByTopicNameWithDelete(topicInfo.getTopicName()))) {
            return ConsoleBaseResponse.error(ConsoleBaseResponse.Status.INVALID_PARAM, "Topic existed");
        }

        if (CollectionUtils.isEmpty(topicInfo.getConf())) {
            return ConsoleBaseResponse.error(ConsoleBaseResponse.Status.INVALID_PARAM, "Please specify cluster config");
        }

        Map<Long, MqServer> mqServerMap = getAllMqServer();

        if (IsEnable.isEnable(topicInfo.getEnableSchemaVerify()) && StringUtils.isEmpty(topicInfo.getSchema())) {
            return ConsoleBaseResponse.error(ConsoleBaseResponse.Status.INVALID_PARAM, "Enable Schema verify，Schema can not be empty");
        }

        Map<Long, AtomicInteger> clusterCountMap = Maps.newHashMap();
        topicInfo.getConf().forEach(conf -> clusterCountMap.computeIfAbsent(conf.getClusterId(), s -> new AtomicInteger(0)).incrementAndGet());

        List<TopicConf> dbConfList = null;
        Map<Long, AtomicInteger> dbClusterCountMap = Maps.newHashMap();
        if (topicInfo.isModify()) {
            dbConfList = topicConfService.findByTopicId(topicInfo.getTopicId());
            if (CollectionUtils.isNotEmpty(dbConfList)) {
                dbConfList.forEach(conf -> dbClusterCountMap.computeIfAbsent(conf.getClusterId(), s -> new AtomicInteger(0)).incrementAndGet());
            }

            for (Map.Entry<Long, AtomicInteger> entry : clusterCountMap.entrySet()) {
                if (entry.getValue().get() > 1 && (!dbClusterCountMap.containsKey(entry.getKey()) || dbClusterCountMap.get(entry.getKey()).get() <= 1)) {
                    return ConsoleBaseResponse.error(ConsoleBaseResponse.Status.INVALID_PARAM, "cluster<" + entry.getKey() + ">repeated");
                } else if (dbClusterCountMap.containsKey(entry.getKey()) && dbClusterCountMap.get(entry.getKey()).get() > 1 && dbClusterCountMap.get(entry.getKey()).get() != entry.getValue().get()) {
                    return ConsoleBaseResponse.error(ConsoleBaseResponse.Status.INVALID_PARAM, "multi-cluster<" + entry.getKey() + ">number can not be modified");
                }
            }

        } else {
            for (Map.Entry<Long, AtomicInteger> entry : clusterCountMap.entrySet()) {
                if (entry.getValue().get() > 1) {
                    return ConsoleBaseResponse.error(ConsoleBaseResponse.Status.INVALID_PARAM, "cluster<" + entry.getKey() + ">repeated");
                }
            }
        }

        Map<Long, List<String>> confIdcMap = Maps.newHashMap();
        for (T bo : topicInfo.getConf()) {
            Cluster cluster = clusterService.findById(bo.getClusterId());
            if (cluster == null) {
                return ConsoleBaseResponse.error(ConsoleBaseResponse.Status.INVALID_PARAM, "cluster<" + bo.getClusterName() + ">not found");
            } else {
                bo.setClusterName(cluster.getName());
            }

            if (confIdcMap.containsKey(bo.getServerIdcId())) {
                return ConsoleBaseResponse.error(ConsoleBaseResponse.Status.INVALID_PARAM, "集群 " + confIdcMap.get(bo.getServerIdcId()).get(0) + " 和 " + bo.getClusterName() + " 同属于一个IDC, 不允许重复添加");
            } else {
                confIdcMap.put(bo.getServerIdcId(), Lists.newArrayList(bo.getClusterName()));
            }

            if (bo instanceof AcceptTopicConfBo) {
                AcceptTopicConfBo acceptBo = (AcceptTopicConfBo) bo;
                if (acceptBo.getMqServerId() == null || acceptBo.getMqServerId().equals(0L)) {
                    return ConsoleBaseResponse.error(ConsoleBaseResponse.Status.INVALID_PARAM, "必须指定mqServerId");
                }
                if (!mqServerMap.containsKey(acceptBo.getMqServerId())) {
                    return ConsoleBaseResponse.error(ConsoleBaseResponse.Status.INVALID_PARAM, "mqServerId<" + acceptBo.getMqServerId() + ">不存在");
                }

                acceptBo.setMqServerName(mqServerMap.get(acceptBo.getMqServerId()).getName());
            }
        }

        if (topicInfo.isModify()) {
            if (CollectionUtils.isNotEmpty(dbConfList)) {
                for (TopicConf topicConf : dbConfList) {
                    List<CustomConsumeSubscription> groupList = consumeSubscriptionService.findByTopicIdClusterId(topicConf.getTopicId(), topicConf.getClusterId());

                    if (CollectionUtils.isNotEmpty(groupList) && !clusterCountMap.containsKey(topicConf.getClusterId())) {
                        return ConsoleBaseResponse.error(ConsoleBaseResponse.Status.INVALID_PARAM, "集群 " + topicConf.getClusterName() + " 已经被订阅，必须先删除订阅，才能删除此集群");
                    }
                }

            }
        }
        return ConsoleBaseResponse.success();
    }


    private Map<Long, MqServer> getAllMqServer() {
        Map<Long, MqServer> mqServerMap = Maps.newHashMap();
        mqServerService.findAll().forEach(server -> mqServerMap.put(server.getId(), server));

        return mqServerMap;
    }

    private void setProxies(TopicConf topicConf) {
        TopicConfConfig config = new TopicConfConfig();
        config.setProxies(Maps.newHashMap());
        topicConf.setTopicConfig(config);

        List<Node> nodeList = nodeService.findByClusterIdNodeType(topicConf.getClusterId(), NodeType.PRODUCER_PROXY);
        if (CollectionUtils.isNotEmpty(nodeList)) {
            Set<String> hostSet = nodeList.stream().map(n -> HostUtils.getIpPortFromHost(n.getHost(), ZKV4ConfigServiceImpl.DEFAULT_PPROXY_PORT)).collect(Collectors.toSet());

            config.getProxies().computeIfAbsent("P_" + topicConf.getClusterName(), s -> Sets.newHashSet()).addAll(hostSet);
        }
    }

    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public ConsoleBaseResponse<?> addPProxy(String clusterName, String host) throws Exception {
        Cluster cluster = clusterService.findByClusterName(clusterName);
        if (cluster == null) {
            return ConsoleBaseResponse.error(ConsoleBaseResponse.Status.INVALID_PARAM, "cluster not found");
        }
        if (!validNodeExist(host, cluster)) {
            return ConsoleBaseResponse.error(ConsoleBaseResponse.Status.INVALID_PARAM, "host not found");
        }

        List<TopicConf> confList = topicConfService.findByClusterId(cluster.getId());
        if (CollectionUtils.isEmpty(confList)) {
            return ConsoleBaseResponse.success();
        }

        addPProxy(host, confList);

        return ConsoleBaseResponse.success();
    }

    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public ConsoleBaseResponse<?> addPProxy(String topicName, String clusterName, String host) throws Exception {
        Topic topic = findByTopicName(topicName);
        if (topic == null) {
            return ConsoleBaseResponse.error(ConsoleBaseResponse.Status.INVALID_PARAM, "topic not found");
        }
        Cluster cluster = clusterService.findByClusterName(clusterName);
        if (cluster == null) {
            return ConsoleBaseResponse.error(ConsoleBaseResponse.Status.INVALID_PARAM, "cluster not found");
        }
        if (!validNodeExist(host, cluster)) {
            return ConsoleBaseResponse.error(ConsoleBaseResponse.Status.INVALID_PARAM, "host not found");
        }

        List<TopicConf> confList = topicConfService.findByTopicClusterId(topic.getId(), cluster.getId());
        if (CollectionUtils.isEmpty(confList)) {
            return ConsoleBaseResponse.error(ConsoleBaseResponse.Status.INVALID_PARAM, "topic conf not found");
        }

        addPProxy(host, confList);

        return ConsoleBaseResponse.success();
    }

    private boolean validNodeExist(String host, Cluster cluster) {
        List<Node> nodeList = nodeService.findByClusterHostNodeType(cluster.getId(), host, NodeType.PRODUCER_PROXY);
        if (CollectionUtils.isEmpty(nodeList)) {
            String ip = HostUtils.getIp(host);
            if (host.equals(ip)) {
                return createPProxyNode(host, cluster);
            }
            nodeList = nodeService.findByClusterHostNodeType(cluster.getId(), host, NodeType.PRODUCER_PROXY);
            if (CollectionUtils.isEmpty(nodeList)) {
                return createPProxyNode(host, cluster);
            }
        }
        return true;
    }

    private boolean createPProxyNode(String host, Cluster cluster) {
        try {
            NodeBo nodeBo = new NodeBo();
            nodeBo.setClusterid(cluster.getId());
            nodeBo.setHost(host);
            nodeBo.setNodetype(NodeType.PRODUCER_PROXY.getIndex());
            nodeService.create(nodeBo);
            return true;
        } catch (Exception e) {
            LOGGER.error("create node error {}, {}", host, cluster);
            return false;
        }
    }

    private void addPProxy(String host, List<TopicConf> confList) throws Exception {
        String ipPort = HostUtils.getIpPortFromHost(host, ZKV4ConfigServiceImpl.DEFAULT_PPROXY_PORT);
        Set<Long> clusterIds = Sets.newHashSet();
        for (TopicConf topicConf : confList) {
            if (topicConf.getTopicConfConfig() != null && MapUtils.isNotEmpty(topicConf.getTopicConfConfig().getProxies())) {
                Map<String, Set<String>> proxyMap = topicConf.getTopicConfConfig().getProxies();
                for (Set<String> ipLists : proxyMap.values()) {
                    if (ipLists.contains(ipPort)) {
                        continue;
                    }

                    ipLists.add(ipPort);
                    clusterIds.add(topicConf.getClusterId());
                    topicConfService.updateByPrimaryKey(topicConf);
                    pushV4ZkInfo(topicConf.getTopicId(), null);

                    LOGGER.info("add pproxy {} success, topicConfId={}, topic={}", ipPort, topicConf.getId(), topicConf.getTopicName());
                }
            }
        }
        if (CollectionUtils.isNotEmpty(clusterIds)) {
            zkv4ConfigService.updatePProxyConfigByClusterId("addPProxy", clusterIds);
        }
    }

    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public ConsoleBaseResponse<?> removePProxy(String host) throws Exception {
        List<TopicConf> confList = topicConfService.findAll();
        removePProxy(host, confList);

        return ConsoleBaseResponse.success();
    }

    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public ConsoleBaseResponse<?> removePProxy(String topicName, String host) throws Exception {
        Topic topic = findByTopicName(topicName);
        if (topic == null) {
            return ConsoleBaseResponse.error(ConsoleBaseResponse.Status.INVALID_PARAM, "topicName not found");
        }
        List<TopicConf> confList = topicConfService.findByTopicId(topic.getId());
        if (CollectionUtils.isEmpty(confList)) {
            return ConsoleBaseResponse.error(ConsoleBaseResponse.Status.INVALID_PARAM, "topic conf not found");
        }
        removePProxy(host, confList);

        return ConsoleBaseResponse.success();
    }

    private void removePProxy(String host, List<TopicConf> confList) throws Exception {
        String ipPort = HostUtils.getIpPortFromHost(host, ZKV4ConfigServiceImpl.DEFAULT_PPROXY_PORT);
        Set<Long> clusterIds = Sets.newHashSet();
        for (TopicConf topicConf : confList) {
            if (topicConf.getTopicConfConfig() != null && MapUtils.isNotEmpty(topicConf.getTopicConfConfig().getProxies())) {
                Map<String, Set<String>> proxyMap = topicConf.getTopicConfConfig().getProxies();
                for (Set<String> ipLists : proxyMap.values()) {
                    if (ipLists.contains(ipPort)) {
                        ipLists.remove(ipPort);
                        clusterIds.add(topicConf.getClusterId());
                        topicConfService.updateByPrimaryKey(topicConf);
                        pushV4ZkInfo(topicConf.getTopicId(), null);

                        LOGGER.info("remove pproxy {} success, topicConfId={}, topic={}", ipPort, topicConf.getId(), topicConf.getTopicName());
                    }
                }
            }
        }
        if (CollectionUtils.isNotEmpty(clusterIds)) {
            zkv4ConfigService.updatePProxyConfigByClusterId("removePProxy", clusterIds);
        }
    }

    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public ConsoleBaseResponse<?> create(TopicOrderBo<AcceptTopicConfBo> topicOrderBo) throws Exception {
        ConsoleBaseResponse<?> validateBo = validateTopicBo(topicOrderBo);
        if (!validateBo.isSuccess()) {
            return validateBo;
        }

        Topic topic = topicOrderBo.buildTopic();
        List<TopicConf> confList = topicOrderBo.buildTopicConf();
        Set<Long> clusterIdSet = Sets.newHashSet();
        Set<Long> dbClusterIdSet = Sets.newHashSet();
        Set<Long> changedClusterIdSet = Sets.newHashSet();

        Map<String, TopicConf> newClusterMqserverMap = Maps.newHashMap();
        Map<String, TopicConf> oldClusterMqserverMap = Maps.newHashMap();
        Map<String, AcceptTopicConfBo> newConfBoMap = Maps.newHashMap();
        topicOrderBo.getConf().forEach(conf -> newConfBoMap.put(getClusterMqserverIdKey(conf.getClusterId(), conf.getMqServerId()), conf));

        confList.forEach(conf -> {
            conf.setId(null);
            conf.setIsDelete(IsDelete.NO.getIndex());
            conf.setCreateTime(new Date());
            conf.setState(IsEnable.ENABLE.getIndex());

            newClusterMqserverMap.put(getClusterMqserverIdKey(conf.getClusterId(), conf.getMqServerId()), conf);
            clusterIdSet.add(conf.getClusterId());
        });
        changedClusterIdSet.addAll(clusterIdSet);

        if (!topicOrderBo.isModify()) {
            topic.setId(null);
            topic.setIsDelete(IsDelete.NO.getIndex());
            topic.setCreateTime(new Date());
            topic.setState(IsEnable.ENABLE.getIndex());
            topic.setTopicConfig(new TopicConfig());

            topicMapper.insertSelective(topic);

            for (TopicConf conf : confList) {
                conf.setTopicId(topic.getId());
                setProxies(conf);
                topicConfService.insert(conf);
            }
        } else {
            Topic dbTopic = findById(topic.getId());
            if (dbTopic == null || dbTopic.getIsDelete() == IsDelete.YES.getIndex()) {
                return ConsoleBaseResponse.error(ConsoleBaseResponse.Status.INVALID_PARAM, "topicId<" + topic.getId() + "> not found");
            }
            topic.setTopicConfig(dbTopic.getTopicConfig() == null ? new TopicConfig() : dbTopic.getTopicConfig());

            List<TopicConf> dbConfList = topicConfService.findByTopicId(topic.getId());
            if (CollectionUtils.isNotEmpty(dbConfList)) {
                dbConfList.forEach(conf -> {
                    dbClusterIdSet.add(conf.getClusterId());
                    oldClusterMqserverMap.put(getClusterMqserverIdKey(conf.getClusterId(), conf.getMqServerId()), conf);
                });
            }

            changedClusterIdSet.addAll(dbClusterIdSet);

            topicMapper.updateByPrimaryKeySelective(topic);

            for (Map.Entry<String, TopicConf> entry : newClusterMqserverMap.entrySet()) {
                String key = entry.getKey();
                TopicConf newConf = entry.getValue();
                if (oldClusterMqserverMap.containsKey(key)) {
                    newConf.setId(oldClusterMqserverMap.get(key).getId());
                    newConf.setTopicConfig(oldClusterMqserverMap.get(key).getTopicConfConfig());
                    topicConfService.updateByPrimaryKey(newConf);
                    oldClusterMqserverMap.remove(key);
                } else {
                    setProxies(newConf);
                    topicConfService.insert(newConf);
                }
            }

            if (MapUtils.isNotEmpty(oldClusterMqserverMap)) {
                topicConfService.deleteByIds(oldClusterMqserverMap.values().stream().map(TopicConf::getId).collect(Collectors.toList()));
            }

        }

        Set<Long> v4ClusterSet = Sets.newHashSet(changedClusterIdSet);
        Set<Long> retainClusterSet = Sets.newHashSet(dbClusterIdSet);
        retainClusterSet.retainAll(clusterIdSet);
        v4ClusterSet.removeAll(retainClusterSet);
        pushV4ZkInfo(topic.getId(), v4ClusterSet);
        return ConsoleBaseResponse.success();
    }

    private String getClusterMqserverIdKey(Long clusterId, Long mqServerId) {
        return clusterId + "_" + mqServerId;
    }

    private void pushV4ZkInfo(Long topicId, Set<Long> clusterIds) throws Exception {
        zkv4ConfigService.updateTopicConfig(topicId, clusterIds);
    }

    @Override
    public Topic findByTopicName(String topicName) {
        TopicCriteria tc = new TopicCriteria();
        tc.createCriteria().andTopicNameEqualTo(topicName).andIsDeleteEqualTo(IsDelete.NO.getIndex());
        List<Topic> list = topicMapper.selectByExampleWithBLOBs(tc);
        if (CollectionUtils.isEmpty(list)) {
            return null;
        }

        return list.get(0);
    }

    @Override
    public List<Topic> findByTopicNameWithDelete(String topicName) {
        TopicCriteria tc = new TopicCriteria();
        tc.createCriteria().andTopicNameEqualTo(topicName);
        return topicMapper.selectByExampleWithBLOBs(tc);
    }

    @Override
    public List<Topic> findById(List<Long> idList) {
        TopicCriteria tc = new TopicCriteria();
        tc.createCriteria().andIsDeleteEqualTo(IsDelete.NO.getIndex()).andIdIn(idList);
        return topicMapper.selectByExampleWithBLOBs(tc);
    }

    @Override
    public Topic findById(Long topicId) {
        return topicMapper.selectByPrimaryKey(topicId);
    }

    @Override
    public List<Topic> findByClusterId(Long clusterId) {
        List<TopicConf> confList = topicConfService.findByClusterId(clusterId);
        if (CollectionUtils.isEmpty(confList)) {
            return Collections.emptyList();
        }

        Set<Long> topicIdSet = Sets.newHashSet();
        confList.forEach(conf -> topicIdSet.add(conf.getTopicId()));

        return findById(Lists.newArrayList(topicIdSet));
    }

    private ConsoleBaseResponse<PageModel<TopicOrderVo>> findListByPage(Long clusterId, String text, String user, Integer curPage, Integer pageSize) {
        if (StringUtils.isNotEmpty(user)) {
            user = user + ";";
        }
        Integer totalCount = topicCustomMapper.selectCountByCondition(clusterId, user, text);
        PageModel<TopicOrderVo> pageModel = new PageModel<>(curPage, pageSize, totalCount);
        if (totalCount == 0) {
            pageModel.setList(Collections.emptyList());
            return ConsoleBaseResponse.success(pageModel);
        }

        List<Topic> list = topicCustomMapper.selectByCondition(clusterId, user, text, pageModel.getPageIndex(), pageModel.getPageSize());
        if (CollectionUtils.isEmpty(list)) {
            pageModel.setList(Collections.emptyList());
            return ConsoleBaseResponse.success(pageModel);
        }

        List<CustomTopicConf> confList = topicConfService.findByTopicId(getTopicIdList(list));

        pageModel.setList(Lists.newArrayList(getTopicVoMap(list, confList, true).values()));

        return ConsoleBaseResponse.success(pageModel);
    }

    private List<Long> getTopicIdList(List<Topic> list) {
        List<Long> idList = Lists.newArrayListWithCapacity(list.size());
        list.forEach(t -> idList.add(t.getId()));
        return idList;
    }

    private Map<Long, TopicOrderVo> getTopicVoMap(List<Topic> list, List<CustomTopicConf> confList, Boolean needExtraParams) {
        Map<Long/*topicID*/, TopicOrderVo> topicVoMap = Maps.newLinkedHashMap();
        list.forEach((topic -> {
            TopicOrderVo vo = TopicOrderVo.buildTopicVo(topic);
            topicVoMap.put(topic.getId(), vo);
        }));
        if (CollectionUtils.isNotEmpty(confList)) {
            Map<Long, Cluster> clusterMap = clusterService.findMap();

            confList.forEach(conf -> {
                if (topicVoMap.containsKey(conf.getTopicId())) {
                    try {
                        TopicConfVo confVo = TopicConfVo.buildTopicConfVo(conf);
                        confVo.setClusterDesc(clusterMap.get(conf.getClusterId()).getDescription());
                        topicVoMap.get(conf.getTopicId()).addConf(confVo);

                    } catch (Exception e) {
                        LOGGER.error("getTopicVoMap exception", e);
                    }
                }
            });
        }
        return topicVoMap;
    }

    @Override
    public ConsoleBaseResponse<TopicOrderVo> findVoById(Long topicId) {
        Topic topic = findById(topicId);
        if (topic == null) {
            return ConsoleBaseResponse.success();
        }

        List<CustomTopicConf> confList = topicConfService.findByTopicId(Lists.newArrayList(topicId));
        return ConsoleBaseResponse.success(getTopicVoMap(Lists.newArrayList(topic), confList, false).get(topicId));
    }

    @Override
    public ConsoleBaseResponse<PageModel<TopicOrderVo>> findAll(Long clusterId, String text, String user, Integer curPage, Integer pageSize) {
        return findListByPage(clusterId, text, null, curPage, pageSize);
    }

    @Override
    public ConsoleBaseResponse<List<TopicSimpleVo>> findAllSimple(String user) {
        List<Topic> list = findAllWithoutPage();
        List<TopicSimpleVo> voList = Lists.newArrayListWithCapacity(list.size());
        list.forEach((topic -> voList.add(TopicSimpleVo.buildVo(topic))));
        return ConsoleBaseResponse.success(voList);
    }


    @Override
    public List<Topic> findAllWithoutPage() {
        TopicCriteria tc = new TopicCriteria();
        tc.createCriteria().andIsDeleteEqualTo(IsDelete.NO.getIndex());
        tc.setOrderByClause("create_time desc");

        return topicMapper.selectByExample(tc);
    }

    @Override
    public ConsoleBaseResponse<List<TopicListGroupVo>> findGroup(String user, Long topicId, Long clusterId) {
        List<CustomConsumeSubscription> list = consumeSubscriptionService.findByTopicIdClusterId(topicId, clusterId);
        if (CollectionUtils.isEmpty(list)) {
            return ConsoleBaseResponse.success();
        }

        List<TopicListGroupVo> voList = Lists.newArrayListWithCapacity(list.size());
        list.forEach(sub -> {
            TopicListGroupVo vo = new TopicListGroupVo();
            BeanUtils.copyProperties(sub, vo);
            voList.add(vo);
        });

        return ConsoleBaseResponse.success(voList);
    }

    @Override
    public ConsoleBaseResponse<List<TopicStateVo>> findState(String user, Long topicId, Long clusterId) {
        List<TopicConf> confList = topicConfService.findByTopicClusterId(topicId, clusterId);
        if (CollectionUtils.isEmpty(confList)) {
            return ConsoleBaseResponse.success();
        }

        MqServer rmqServer = null;
        TopicConf topicConf = null;
        for (TopicConf conf : confList) {
            MqServer mqServer = mqServerService.findById(conf.getMqServerId());
            if (mqServer == null) {
                continue;
            }

            if (mqServer.getType() == MqServerType.ROCKETMQ.getIndex()) {
                rmqServer = mqServer;
                topicConf = conf;
            }
        }

        if (rmqServer == null) {
            return ConsoleBaseResponse.error(ConsoleBaseResponse.Status.INVALID_PARAM, "only support query RMQ cluster state");
        }

        final TopicConf conf = topicConf;
        List<TopicStateVo> voList = Lists.newArrayList();
        try {
            TopicStatsTable topicStatsTable = rmqAdminService.queryTopicConsumeState(rmqServer.getAddr(), topicConf.getTopicName());
            topicStatsTable.getOffsetTable().forEach(((messageQueue, topicOffset) -> {
                TopicStateVo vo = new TopicStateVo();
                vo.setTopicId(conf.getTopicId());
                vo.setTopicName(conf.getTopicName());
                vo.setClusterId(conf.getClusterId());
                vo.setQid(getRmqQid(conf.getClusterName(), RmqAdminServiceImpl.getNewQid(messageQueue)));
                vo.setMaxOffset(topicOffset.getMaxOffset());
                vo.setMinOffset(topicOffset.getMinOffset());
                vo.setLastUpdateTime(topicOffset.getLastUpdateTimestamp() <= 0 ? "2 days ago(data is kept for 2 days only)" : DateUtil.longToString(topicOffset.getLastUpdateTimestamp(), DateUtil.DateStyle.YYYY_MM_DD_HH_MM_SS_CN));
                voList.add(vo);
            }));
        } catch (Exception e) {
            LOGGER.error("Query Topic State Exception", e);
            return ConsoleBaseResponse.error(ConsoleBaseResponse.Status.INVALID_PARAM, "no data，retry later");
        }

        return ConsoleBaseResponse.success(voList);
    }

    private String getBrokerQid(String prefix, String clusterName, String qid) {
        return prefix + "_" + clusterName + "_" + qid;
    }

    private String getRmqQid(String clusterName, String qid) {
        return getBrokerQid("R", clusterName, qid);
    }

    private String getKafkaQid(String clusterName, String qid) {
        return getBrokerQid("K", clusterName, qid);
    }


    @Override
    public ConsoleBaseResponse<TopicMessageVo> findMessage(String user, Long topicId, Long clusterId) {
        List<TopicConf> confList = topicConfService.findByTopicClusterId(topicId, clusterId);
        if (CollectionUtils.isEmpty(confList)) {
            return ConsoleBaseResponse.success();
        }

        MqServer rmqServer = null;
        TopicConf topicConf = null;
        for (TopicConf conf : confList) {
            MqServer mqServer = mqServerService.findById(conf.getMqServerId());
            if (mqServer == null) {
                continue;
            }
            rmqServer = mqServer;
            topicConf = conf;
        }

        if (rmqServer == null) {
            return ConsoleBaseResponse.error(ConsoleBaseResponse.Status.INVALID_PARAM, "cluster info not found");
        }

        Message message = null;
        try {
            if (rmqServer.getType() == MqServerType.ROCKETMQ.getIndex()) {
                message = rmqAdminService.queryLatestMessage(rmqServer.getAddr(), topicConf.getTopicName());
            }
        } catch (Exception e) {
            LOGGER.error("query Topic latest message Exception, topic=" + topicConf.getTopicName(), e);
        }

        TopicMessageVo vo = new TopicMessageVo();
        vo.setTopicId(topicConf.getTopicId());
        vo.setTopicName(topicConf.getTopicName());
        vo.setClusterId(topicConf.getClusterId());
        vo.setClusterName(topicConf.getClusterName());

        if (message == null) {
            vo.setQid("");
            vo.setOffset(0L);
            vo.setMsg("no msg");
        } else {
            if (rmqServer.getType() == MqServerType.ROCKETMQ.getIndex()) {
                vo.setQid(getRmqQid(topicConf.getClusterName(), message.getQid()));
            } else {
                vo.setQid(getKafkaQid(topicConf.getClusterName(), message.getQid()));
            }

            vo.setOffset(message.getOffset());
            vo.setMsg(message.getBody());
        }

        return ConsoleBaseResponse.success(vo);
    }
}