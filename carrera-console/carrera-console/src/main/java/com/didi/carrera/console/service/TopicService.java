package com.didi.carrera.console.service;

import com.didi.carrera.console.dao.model.Topic;
import com.didi.carrera.console.service.bean.PageModel;
import com.didi.carrera.console.service.vo.TopicListGroupVo;
import com.didi.carrera.console.service.vo.TopicMessageVo;
import com.didi.carrera.console.service.vo.TopicOrderVo;
import com.didi.carrera.console.service.vo.TopicSimpleVo;
import com.didi.carrera.console.service.vo.TopicStateVo;
import com.didi.carrera.console.web.ConsoleBaseResponse;
import com.didi.carrera.console.web.controller.bo.AcceptTopicConfBo;
import com.didi.carrera.console.web.controller.bo.TopicConfBo;
import com.didi.carrera.console.web.controller.bo.TopicOrderBo;

import java.util.List;


public interface TopicService {

    Topic findByTopicName(String topicName);

    List<Topic> findByTopicNameWithDelete(String topicName);

    List<Topic> findByClusterId(Long clusterId);

    List<Topic> findById(List<Long> idList);

    Topic findById(Long topicId);

    <T extends TopicConfBo> ConsoleBaseResponse<?> validateTopicBo(TopicOrderBo<T> topicInfo);

    ConsoleBaseResponse<PageModel<TopicOrderVo>> findAll(Long clusterId, String text, String user, Integer curPage, Integer pageSize);

    ConsoleBaseResponse<List<TopicSimpleVo>> findAllSimple(String user);

    List<Topic> findAllWithoutPage();

    ConsoleBaseResponse<List<TopicListGroupVo>> findGroup(String user, Long topicId, Long clusterId);

    ConsoleBaseResponse<?> create(TopicOrderBo<AcceptTopicConfBo> topicOrderBo) throws Exception;

    ConsoleBaseResponse<List<TopicStateVo>> findState(String user, Long topicId, Long clusterId);

    ConsoleBaseResponse<TopicMessageVo> findMessage(String user, Long topicId, Long clusterId);

    ConsoleBaseResponse<TopicOrderVo> findVoById(Long topicId);

    ConsoleBaseResponse<?> addPProxy(String clusterName, String host) throws Exception;
    ConsoleBaseResponse<?> addPProxy(String topicName, String clusterName, String host) throws Exception;
    ConsoleBaseResponse<?> removePProxy(String host) throws Exception;
    ConsoleBaseResponse<?> removePProxy(String topicName, String host) throws Exception;
}