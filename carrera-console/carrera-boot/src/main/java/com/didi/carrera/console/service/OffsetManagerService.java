package com.didi.carrera.console.service;

import java.util.Date;
import java.util.Map;


public interface OffsetManagerService {
    void resetOffsetToLatest(Long clusterId, Long groupId, Long topicId) throws Exception;

    void resetOffsetByTime(Long clusterId, Long groupId, Long topicId, Date time) throws Exception;

    void resetOffsetByOffset(Long clusterId, Long groupId, Long topicId, String qid, long offset) throws Exception;

    Map<String, Map<String, Long>> getProduceOffset(Long clusterId, Long topicId) throws Exception;
    Map<String, Map<String, Long>> getConsumeOffset(Long clusterId, Long groupId, Long topicId) throws Exception;
}