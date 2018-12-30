package com.didi.carrera.console.service.impl;

import com.didi.carrera.console.dao.DaoUtil;
import com.didi.carrera.console.dao.dict.IsDelete;
import com.didi.carrera.console.dao.dict.IsEnable;
import com.didi.carrera.console.dao.dict.MqServerType;
import com.didi.carrera.console.dao.mapper.ConsumeGroupMapper;
import com.didi.carrera.console.dao.mapper.custom.ConsumeGroupCustomMapper;
import com.didi.carrera.console.dao.model.ConsumeGroup;
import com.didi.carrera.console.dao.model.ConsumeGroupCriteria;
import com.didi.carrera.console.dao.model.ConsumeSubscription;
import com.didi.carrera.console.dao.model.MqServer;
import com.didi.carrera.console.dao.model.TopicConf;
import com.didi.carrera.console.dao.model.custom.ConsumeGroupConfig;
import com.didi.carrera.console.dao.model.custom.CustomSubscriptionStateCount;
import com.didi.carrera.console.service.ConsumeGroupService;
import com.didi.carrera.console.service.ConsumeSubscriptionService;
import com.didi.carrera.console.service.MqServerService;
import com.didi.carrera.console.service.OffsetManagerService;
import com.didi.carrera.console.service.RmqAdminService;
import com.didi.carrera.console.service.TopicConfService;
import com.didi.carrera.console.service.ZKV4ConfigService;
import com.didi.carrera.console.service.bean.PageModel;
import com.didi.carrera.console.service.vo.ConsumeGroupSearchItemVo;
import com.didi.carrera.console.service.vo.ConsumeGroupVo;
import com.didi.carrera.console.service.vo.GroupConsumeStateVo;
import com.didi.carrera.console.service.vo.SearchItemVo;
import com.didi.carrera.console.web.ConsoleBaseResponse;
import com.didi.carrera.console.web.controller.bo.ConsumeGroupBo;
import com.didi.carrera.console.web.controller.bo.ConsumeGroupResetOffsetBo;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.commons.collections.CollectionUtils;
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
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;


@Service("didiConsumeGroupServiceImpl")
@EnableTransactionManagement
public class ConsumeGroupServiceImpl implements ConsumeGroupService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConsumeGroupServiceImpl.class);

    @Autowired
    private ConsumeGroupCustomMapper consumeGroupCustomMapper;

    @Autowired
    private ConsumeGroupMapper consumeGroupMapper;

    @Autowired
    private ZKV4ConfigService zkv4ConfigService;

    @Resource(name = "didiConsumeSubscriptionServiceImpl")
    private ConsumeSubscriptionService consumeSubscriptionService;

    @Resource(name = "didiTopicConfServiceImpl")
    private TopicConfService topicConfService;

    @Resource(name = "didiMqServerServiceImpl")
    private MqServerService mqServerService;

    @Resource(name = "didiOffsetManagerServiceImpl")
    private OffsetManagerService offsetManagerService;

    @Resource(name = "didiRmqAdminServiceImpl")
    private RmqAdminService rmqAdminService;

    @Override
    public ConsumeGroup findByGroupName(String groupName) {
        ConsumeGroupCriteria cgc = new ConsumeGroupCriteria();
        cgc.createCriteria().andIsDeleteEqualTo(IsDelete.NO.getIndex()).andGroupNameEqualTo(groupName);
        List<ConsumeGroup> list = consumeGroupMapper.selectByExample(cgc);
        if (CollectionUtils.isEmpty(list)) {
            return null;
        }

        return list.get(0);
    }

    @Override
    public ConsumeGroup findById(Long groupId) {
        return consumeGroupMapper.selectByPrimaryKey(groupId);
    }

    @Override
    public List<ConsumeGroup> findById(List<Long> idList) {
        ConsumeGroupCriteria cgc = new ConsumeGroupCriteria();
        cgc.createCriteria().andIsDeleteEqualTo(IsDelete.NO.getIndex()).andIdIn(idList);
        return consumeGroupMapper.selectByExample(cgc);
    }

    @Override
    public List<ConsumeGroup> findByClusterId(Long clusterId) {
        List<ConsumeSubscription> subList = consumeSubscriptionService.findByClusterId(clusterId);
        if (CollectionUtils.isEmpty(subList)) {
            return Collections.emptyList();
        }

        Set<Long> groupIdSet = Sets.newHashSet();
        subList.forEach(sub -> groupIdSet.add(sub.getGroupId()));

        return findById(Lists.newArrayList(groupIdSet));
    }

    @Override
    public List<ConsumeGroup> findAll() {
        ConsumeGroupCriteria cgc = new ConsumeGroupCriteria();
        cgc.createCriteria().andIsDeleteEqualTo(IsDelete.NO.getIndex());
        return consumeGroupMapper.selectByExample(cgc);
    }

    @Override
    public ConsoleBaseResponse<List<ConsumeGroupVo>> findAllWithoutPage(String user) {
        user = DaoUtil.getLikeField(user + ";");
        ConsumeGroupCriteria cgc = new ConsumeGroupCriteria();
        cgc.createCriteria().andIsDeleteEqualTo(IsDelete.NO.getIndex()).andContactersLike(user);
        cgc.setOrderByClause("create_time desc");

        List<ConsumeGroup> list = consumeGroupMapper.selectByExample(cgc);

        return ConsoleBaseResponse.success(Lists.newArrayList(getConsumeGroupVoMap(list).values()));
    }

    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public ConsoleBaseResponse<?> delete(String user, Long groupId) throws Exception {
        ConsumeGroup group = findById(groupId);
        if (group == null) {
            return ConsoleBaseResponse.error(ConsoleBaseResponse.Status.INVALID_PARAM, String.format("groupId<%s>subscription info not exist.", groupId));
        }

        ConsoleBaseResponse ret = consumeSubscriptionService.deleteByGroupId(user, groupId);
        if (ret.isSuccess()) {
            deleteById(user, groupId);
        }

        return ConsoleBaseResponse.success();
    }

    private void deleteById(String user, Long subId) {
        ConsumeGroup group = new ConsumeGroup();
        group.setIsDelete(IsDelete.YES.getIndex());
        group.setId(subId);
        group.setRemark(user);
        consumeGroupMapper.updateByPrimaryKeySelective(group);

    }

    @Override
    public ConsoleBaseResponse<PageModel<ConsumeGroupVo>> findAll(String user, String text, Integer curPage, Integer pageSize) {
        return findAllByCondition(user, text, curPage, pageSize);
    }

    private ConsoleBaseResponse<PageModel<ConsumeGroupVo>> findAllByCondition(String user, String text, Integer curPage, Integer pageSize) {
        if (!StringUtils.isEmpty(user)) {
            user = DaoUtil.getLikeField(user + ";");
        }

        if (!StringUtils.isEmpty(text)) {
            text = DaoUtil.getLikeField(text);
        }

        Integer totalCount = consumeGroupCustomMapper.selectCountByCondition(user, text);
        PageModel<ConsumeGroupVo> pageModel = new PageModel<>(curPage, pageSize, totalCount);
        if (totalCount == 0) {
            pageModel.setList(Collections.emptyList());
            return ConsoleBaseResponse.success(pageModel);
        }

        List<ConsumeGroup> list = consumeGroupCustomMapper.selectByCondition(user, text, pageModel.getPageIndex(), pageModel.getPageSize());
        if (CollectionUtils.isEmpty(list)) {
            pageModel.setList(Collections.emptyList());
            return ConsoleBaseResponse.success(pageModel);
        }

        Map<Long, ConsumeGroupVo> retMap = getConsumeGroupVoMap(list);
        fillSubCount(retMap, Lists.newArrayList(retMap.keySet()));

        pageModel.setList(Lists.newArrayList(retMap.values()));
        return ConsoleBaseResponse.success(pageModel);
    }

    private Map<Long, ConsumeGroupVo> getConsumeGroupVoMap(List<ConsumeGroup> list) {
        Map<Long, ConsumeGroupVo> retMap = Maps.newLinkedHashMap();
        list.forEach(consumeGroup -> {
            ConsumeGroupVo vo = ConsumeGroupVo.buildConsumeGroupVo(consumeGroup);
            retMap.put(consumeGroup.getId(), vo);
        });

        return retMap;
    }

    private void fillSubCount(Map<Long, ConsumeGroupVo> retMap, List<Long> groupIdList) {
        List<CustomSubscriptionStateCount> consumeNumList = consumeSubscriptionService.findStateCountByGroupId(groupIdList);
        if (CollectionUtils.isNotEmpty(consumeNumList)) {
            consumeNumList.forEach(item -> {
                if (retMap.containsKey(item.getGroupId())) {
                    ConsumeGroupVo vo = retMap.get(item.getGroupId());
                    int subNum = vo.getSubscriptionNum() == null ? 0 : vo.getSubscriptionNum();
                    vo.setSubscriptionNum(subNum + item.getCount());

                    if (IsEnable.isEnable(item.getState().byteValue())) {
                        int subEnableNum = vo.getSubscriptionEnableNum() == null ? 0 : vo.getSubscriptionEnableNum();
                        vo.setSubscriptionEnableNum(subEnableNum + item.getCount());
                    }
                }
            });
        }
    }

    private List<ConsumeGroup> findByGroupNameWithDelete(String groupName) {
        ConsumeGroupCriteria cgc = new ConsumeGroupCriteria();
        cgc.createCriteria().andGroupNameEqualTo(groupName);

        return consumeGroupMapper.selectByExample(cgc);
    }

    private ConsoleBaseResponse<?> validConsumeGroupBo(ConsumeGroupBo groupBo) {
        if (!groupBo.isModify() && CollectionUtils.isNotEmpty(findByGroupNameWithDelete(groupBo.getGroupName()))) {
            return ConsoleBaseResponse.error(ConsoleBaseResponse.Status.INVALID_PARAM, "consumer group already exist.");
        }

        return ConsoleBaseResponse.success();
    }

    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public ConsoleBaseResponse<?> create(ConsumeGroupBo groupBo) throws Exception {
        ConsoleBaseResponse<?> validRet = validConsumeGroupBo(groupBo);
        if (!validRet.isSuccess()) {
            return validRet;
        }

        groupBo.setContacters(groupBo.getContacters().toLowerCase());

        ConsumeGroup group = groupBo.buildConsumeGroup();
        if (groupBo.isModify()) {
            group.setId(groupBo.getGroupId());

            ConsumeGroup dbGroup = findById(groupBo.getGroupId());
            if (dbGroup == null || dbGroup.getIsDelete() == IsDelete.YES.getIndex()) {
                return ConsoleBaseResponse.error(ConsoleBaseResponse.Status.INVALID_PARAM, "groupId<" + groupBo.getGroupId() + "> not exist");
            }
            group.setConsumeGroupConfig(dbGroup.getConsumeGroupConfig() == null ? new ConsumeGroupConfig() : dbGroup.getConsumeGroupConfig());

            insertOrUpdate(group);
        } else {
            group.setId(null);
            group.setConsumeGroupConfig(new ConsumeGroupConfig());
            group.setCreateTime(new Date());
            group.setIsDelete(IsDelete.NO.getIndex());

            insertOrUpdate(group);
        }
        pushV4ZkInfo(group.getId());
        return ConsoleBaseResponse.success();
    }

    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void insertOrUpdate(ConsumeGroup group) {
        if (group.getId() != null && group.getId() > 0) {
            consumeGroupMapper.updateByPrimaryKeySelective(group);
        } else {
            consumeGroupMapper.insertSelective(group);
        }
    }

    private void pushV4ZkInfo(Long groupId) throws Exception {
        zkv4ConfigService.onlyUpdateGroupConfig(groupId);
    }

    @Override
    public boolean validUserExist(String user, Long groupId) {
        if (StringUtils.isBlank(user)) {
            return false;
        }

        ConsumeGroupCriteria csc = new ConsumeGroupCriteria();
        csc.createCriteria().andIsDeleteEqualTo(IsDelete.NO.getIndex()).andIdEqualTo(groupId).andContactersLike(DaoUtil.getLikeField(user));
        return consumeGroupMapper.countByExample(csc) > 0;
    }

    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public ConsoleBaseResponse<?> changeState(String user, Long groupId, Integer state) throws Exception {
        if (IsEnable.getByIndex(state.byteValue()) == null) {
            return ConsoleBaseResponse.error(ConsoleBaseResponse.Status.INVALID_PARAM, "only 0 or 1");
        }

        if (!validUserExist(user, groupId)) {
            return ConsoleBaseResponse.error(ConsoleBaseResponse.Status.INVALID_PARAM, user + "not authorized");
        }
        return consumeSubscriptionService.changeState(groupId, state);
    }

    private boolean primaryKeyIsNull(Long id) {
        return id == null || id.equals(0L);
    }

    @Override
    public ConsoleBaseResponse<List<GroupConsumeStateVo>> getConsumeState(String user, Long groupId, Long topicId, Long clusterId) {
        if (primaryKeyIsNull(groupId)) {
            return ConsoleBaseResponse.error(ConsoleBaseResponse.Status.INVALID_PARAM, "consumerGroup is empty");
        }

        List<ConsumeSubscription> list = consumeSubscriptionService.findByNotNullGroupClusterTopicId(groupId, clusterId, topicId);
        if (CollectionUtils.isEmpty(list)) {
            return ConsoleBaseResponse.error(ConsoleBaseResponse.Status.INVALID_PARAM, "subscription info not found");
        }

        ConsumeSubscription sub = list.get(0);

        return ConsoleBaseResponse.success(buildGroupConsumeStateVoList(groupId, sub.getTopicId(), sub.getClusterId(), sub));
    }

    private List<GroupConsumeStateVo> buildGroupConsumeStateVoList(Long groupId, Long topicId, Long clusterId, ConsumeSubscription sub) {
        List<GroupConsumeStateVo> voList = Lists.newArrayList();
        Map<String/*min/max key*/, Map<String/*qid*/, Long>> minOffsetMap = getRmqTopicMinOffsetMap(topicId, clusterId);
        try {
            Map<String, Map<String, Long>> consumeOffsetMap = offsetManagerService.getConsumeOffset(clusterId, groupId, topicId);
            Map<String, Map<String, Long>> produceOffsetMap = offsetManagerService.getProduceOffset(clusterId, topicId);

            consumeOffsetMap.forEach((mqType, offsetMap) -> offsetMap.forEach((qid, consumeOffset) -> {
                GroupConsumeStateVo vo = new GroupConsumeStateVo();
                BeanUtils.copyProperties(sub, vo);
                voList.add(vo);

                if (MqServerType.ROCKETMQ.getName().equalsIgnoreCase(mqType)) {
                    vo.setQid(qid);
                    vo.setConsumeOffset(consumeOffset);
                    vo.setMinOffset(minOffsetMap.get("min").getOrDefault(qid, null));
                    vo.setMaxOffset(minOffsetMap.get("max").containsKey(qid) ? minOffsetMap.get("max").get(qid) : null);
                    vo.setLag(getConsumeLag(produceOffsetMap, MqServerType.ROCKETMQ.getName(), qid, consumeOffset));
                } else {
                    vo.setQid(qid);
                    vo.setConsumeOffset(consumeOffset);
                    vo.setMinOffset(null);
                    vo.setLag(getConsumeLag(produceOffsetMap, MqServerType.KAFKA.getName(), qid, consumeOffset));
                }
            }));
        } catch (Exception e) {
            LOGGER.error("build GroupConsumeStateVo exception", e);
        }

        return voList;
    }

    private long getConsumeLag(Map<String, Map<String, Long>> produceOffsetMap, String mqType, String qid, Long consumeOffset) {
        long produceOffset = -1L;
        if (produceOffsetMap.containsKey(mqType) && produceOffsetMap.get(mqType).containsKey(qid)) {
            produceOffset = produceOffsetMap.get(mqType).remove(qid);
        }
        return produceOffset > consumeOffset ? produceOffset - consumeOffset : 0;
    }

    private Map<String/*min/max key*/, Map<String/*qid*/, Long>> getRmqTopicMinOffsetMap(Long topicId, Long clusterId) {
        List<TopicConf> confList = topicConfService.findByTopicClusterId(topicId, clusterId);
        MqServer rmqServer = null;
        TopicConf topicConf = null;
        for (TopicConf conf : confList) {
            MqServer mqServer = mqServerService.findById(conf.getMqServerId());
            if (mqServer.getType() == MqServerType.ROCKETMQ.getIndex()) {
                topicConf = conf;
                rmqServer = mqServer;
            }
        }
        Map<String/*min/max key*/, Map<String/*qid*/, Long>> ret = Maps.newHashMap();
        Map<String, Long> minOffsetMap = Maps.newHashMap();
        Map<String, Long> maxOffsetMap = Maps.newHashMap();
        ret.put("min", minOffsetMap);
        ret.put("max", maxOffsetMap);
        if (rmqServer == null) {
            return ret;
        }

        try {
            TopicStatsTable statsTable = rmqAdminService.queryTopicConsumeState(rmqServer.getAddr(), topicConf.getTopicName());
            statsTable.getOffsetTable().forEach(((messageQueue, topicOffset) -> {
                minOffsetMap.put(RmqAdminServiceImpl.getNewQid(messageQueue), topicOffset.getMinOffset());
                maxOffsetMap.put(RmqAdminServiceImpl.getNewQid(messageQueue), topicOffset.getMaxOffset());
            }));

            return ret;
        } catch (Exception e) {
            LOGGER.error("queryTopicConsumeState exception, topic:" + topicConf.getTopicName() + ", mqserver:" + topicConf.getMqServerName());
            return ret;
        }
    }

    @Override
    public ConsoleBaseResponse<?> resetOffset(ConsumeGroupResetOffsetBo resetOffsetBo) throws Exception {
        return consumeSubscriptionService.resetOffset(resetOffsetBo);
    }

    @Override
    public ConsoleBaseResponse<ConsumeGroupSearchItemVo> findSearchItem(String user, Long groupId) {
        ConsumeGroupSearchItemVo vo = new ConsumeGroupSearchItemVo();
        vo.setCluster(Lists.newArrayList());
        vo.setTopic(Lists.newArrayList());

        List<ConsumeSubscription> list = consumeSubscriptionService.findByGroupId(groupId);
        if (CollectionUtils.isEmpty(list)) {
            return ConsoleBaseResponse.success(vo);
        }

        Map<Long, SearchItemVo> clusterMap = Maps.newHashMap();
        Map<Long, SearchItemVo> topicMap = Maps.newHashMap();
        list.forEach(sub -> {
            if (!clusterMap.containsKey(sub.getClusterId())) {
                clusterMap.put(sub.getClusterId(), new SearchItemVo(sub.getClusterId(), sub.getClusterName()));
            }

            if (!topicMap.containsKey(sub.getTopicId())) {
                topicMap.put(sub.getTopicId(), new SearchItemVo(sub.getTopicId(), sub.getTopicName()));
            }
        });
        vo.getCluster().addAll(clusterMap.values());
        vo.getTopic().addAll(topicMap.values());

        return ConsoleBaseResponse.success(vo);
    }
}