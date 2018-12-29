package com.didi.carrera.console.service;

import com.didi.carrera.console.dao.dict.ClusterMqServerRelationType;
import com.didi.carrera.console.dao.model.ClusterMqserverRelation;

import java.util.List;


public interface ClusterMqserverRelationService {

    List<ClusterMqserverRelation> findByClusterId(Long clusterId);

    List<ClusterMqserverRelation> findByClusterId(Long clusterId, ClusterMqServerRelationType type);

    List<ClusterMqserverRelation> findByMqServerId(Long mqServerId);

    boolean insert(ClusterMqserverRelation relation);

    boolean updateByPrimaryKey(ClusterMqserverRelation relation);

}