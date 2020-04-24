package com.didi.carrera.console.service;

import com.didi.carrera.console.dao.model.ConsumeSubscription;
import com.didi.carrera.console.dao.model.custom.CustomConsumeSubscription;
import com.didi.carrera.console.dao.model.custom.CustomSubscriptionStateCount;
import com.didi.carrera.console.service.bean.PageModel;
import com.didi.carrera.console.service.exception.ZkConfigException;
import com.didi.carrera.console.service.vo.SearchItemVo;
import com.didi.carrera.console.service.vo.SubscriptionOrderListVo;
import com.didi.carrera.console.web.ConsoleBaseResponse;
import com.didi.carrera.console.web.controller.bo.ConsumeGroupResetOffsetBo;
import com.didi.carrera.console.web.controller.bo.ConsumeSubscriptionOrderBo;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;


public interface ConsumeSubscriptionService {

    ConsoleBaseResponse<?> validateConsumeSubscriptionBo(ConsumeSubscriptionOrderBo subscriptionBo) throws ZkConfigException;

    List<CustomConsumeSubscription> findByTopicIdClusterId(Long topicId, Long clusterId);

    ConsoleBaseResponse<SubscriptionOrderListVo> findByGroupClusterTopicId(Long groupId, Long clusterId, Long topicId);

    List<ConsumeSubscription> findByNotNullGroupClusterTopicId(Long groupId, Long clusterId, Long topicId);

    ConsumeSubscription findById(Long subId);

    List<ConsumeSubscription> findEnableByGroupId(Long groupId);

    List<ConsumeSubscription> findEnableByClusterId(Long clusterId);

    List<ConsumeSubscription> findByClusterId(Long clusterId);

    ConsoleBaseResponse<PageModel<SubscriptionOrderListVo>> findAll(String user, String text, Long clusterId, Long groupId, Integer consumeType, Integer state, Integer curPage, @RequestParam Integer pageSize);

    ConsoleBaseResponse<?> changeState(String user, Long subId, Integer state) throws Exception;

    ConsoleBaseResponse<?> changeState(Long groupId, Integer state) throws Exception;

    List<ConsumeSubscription> findByGroupId(Long groupId);

    List<CustomSubscriptionStateCount> findStateCountByGroupId(List<Long> groupIds);

    ConsoleBaseResponse<?> createConsumeSubscription(ConsumeSubscriptionOrderBo subscriptionBo) throws Exception;

    ConsoleBaseResponse<?> resetOffset(ConsumeGroupResetOffsetBo resetOffsetBo) throws Exception;

    ConsoleBaseResponse<?> delete(String user, Long subId) throws Exception;

    ConsoleBaseResponse<?> deleteByGroupId(String user, Long groupId) throws Exception;

    ConsoleBaseResponse<List<SearchItemVo>> findMsgPushType(String user);

    List<ConsumeSubscription> findAll();

    ConsoleBaseResponse<?> addCProxy(String clusterName, String host) throws Exception;
    ConsoleBaseResponse<?> addCProxy(String groupName, String clusterName, String host) throws Exception;
    ConsoleBaseResponse<?> removeCProxy(String host) throws Exception;
    ConsoleBaseResponse<?> removeCProxy(String groupName, String host) throws Exception;

}