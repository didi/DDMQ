package com.didi.carrera.console.service.impl;

import com.didi.carrera.console.dao.dict.IsDelete;
import com.didi.carrera.console.dao.mapper.ClusterMapper;
import com.didi.carrera.console.dao.model.Cluster;
import com.didi.carrera.console.dao.model.ClusterCriteria;
import com.didi.carrera.console.service.ClusterService;
import com.google.common.collect.Maps;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;


@Service("didiClusterServiceImpl")
public class ClusterServiceImpl implements ClusterService {

    @Autowired
    private ClusterMapper clusterMapper;

    @Override
    public Cluster findById(Long clusterId) {
        return clusterMapper.selectByPrimaryKey(clusterId);
    }

    @Override
    public Cluster findByClusterName(String clusterName) {
        ClusterCriteria cc = new ClusterCriteria();
        cc.createCriteria().andNameEqualTo(clusterName).andIsDeleteEqualTo(IsDelete.NO.getIndex());
        List<Cluster> list = clusterMapper.selectByExample(cc);
        if (CollectionUtils.isNotEmpty(list)) {
            return list.get(0);
        }

        return null;
    }

    @Override
    public List<Cluster> findAll() {
        ClusterCriteria cc = new ClusterCriteria();
        cc.createCriteria().andIsDeleteEqualTo(IsDelete.NO.getIndex());
        return clusterMapper.selectByExample(cc);
    }


    @Override
    public Map<Long, Cluster> findMap() {
        Map<Long, Cluster> map = Maps.newHashMap();
        findAll().forEach(cluster -> map.put(cluster.getId(), cluster));
        return map;
    }
}