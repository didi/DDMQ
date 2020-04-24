package com.didi.carrera.console.service.impl;

import com.didi.carrera.console.dao.dict.ClusterMqServerRelationType;
import com.didi.carrera.console.dao.dict.IsDelete;
import com.didi.carrera.console.dao.mapper.ClusterMqserverRelationMapper;
import com.didi.carrera.console.dao.model.ClusterMqserverRelation;
import com.didi.carrera.console.dao.model.ClusterMqserverRelationCriteria;
import com.didi.carrera.console.service.ClusterMqserverRelationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class ClusterMqserverRelationServiceImpl implements ClusterMqserverRelationService {

    @Autowired
    private ClusterMqserverRelationMapper clusterMqserverRelationMapper;

    private List<ClusterMqserverRelation> findByCondition(Long clusterId, Long mqServerId, ClusterMqServerRelationType type) {
        ClusterMqserverRelationCriteria cmrc = getClusterMqserverRelationCriteria(clusterId, mqServerId, type);

        return clusterMqserverRelationMapper.selectByExample(cmrc);
    }

    private ClusterMqserverRelationCriteria getClusterMqserverRelationCriteria(Long clusterId, Long mqServerId, ClusterMqServerRelationType type) {
        ClusterMqserverRelationCriteria cmrc = new ClusterMqserverRelationCriteria();
        ClusterMqserverRelationCriteria.Criteria cmrcc = cmrc.createCriteria();
        cmrcc.andIsDeleteEqualTo(IsDelete.NO.getIndex());
        if(clusterId != null && clusterId > 0) {
            cmrcc.andClusterIdEqualTo(clusterId);
        }

        if(mqServerId != null && mqServerId > 0) {
            cmrcc.andMqServerIdEqualTo(mqServerId);
        }

        if(type != null) {
            cmrcc.andTypeEqualTo(type.getIndex());
        }
        return cmrc;
    }

    @Override
    public List<ClusterMqserverRelation> findByClusterId(Long clusterId) {
        return findByCondition(clusterId, null, null);
    }

    @Override
    public List<ClusterMqserverRelation> findByClusterId(Long clusterId, ClusterMqServerRelationType type) {
        return findByCondition(clusterId, null, type);
    }

    @Override
    public List<ClusterMqserverRelation> findByMqServerId(Long mqServerId) {
        return findByCondition(null, mqServerId, null);
    }

    @Override
    public boolean insert(ClusterMqserverRelation relation) {
        relation.setId(null);
        return clusterMqserverRelationMapper.insertSelective(relation) > 0;
    }

    @Override
    public boolean updateByPrimaryKey(ClusterMqserverRelation relation) {
        return clusterMqserverRelationMapper.updateByPrimaryKeySelective(relation) > 0;
    }
}