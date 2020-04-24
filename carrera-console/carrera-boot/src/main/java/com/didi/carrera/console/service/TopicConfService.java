package com.didi.carrera.console.service;

import com.didi.carrera.console.dao.model.TopicConf;
import com.didi.carrera.console.dao.model.custom.CustomTopicConf;

import java.util.List;


public interface TopicConfService {

    List<CustomTopicConf> findByTopicId(List<Long> topicIds);

    List<TopicConf> findByTopicId(Long topicId);

    List<TopicConf> findByClusterId(Long clusterId);

    List<TopicConf> findByTopicClusterId(Long topicId, Long clusterId);

    boolean insert(TopicConf conf) throws Exception;

    boolean insert(List<TopicConf> confList) throws Exception;

    boolean updateByPrimaryKey(TopicConf conf) throws Exception;

    boolean deleteByIds(List<Long> configIds);

    List<TopicConf> findAll();

    List<Long> findTopicByClusterIdWithDeleted(Long clusterId);

    List<TopicConf> findByTopicClusterIds(List<Long> topicIds, List<Long> clusterIds);
}