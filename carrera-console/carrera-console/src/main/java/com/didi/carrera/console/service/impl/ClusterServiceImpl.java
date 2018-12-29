package com.didi.carrera.console.service.impl;

import com.didi.carrera.console.dao.dict.IsDelete;
import com.didi.carrera.console.dao.mapper.ClusterMapper;
import com.didi.carrera.console.dao.model.Cluster;
import com.didi.carrera.console.dao.model.ClusterCriteria;
import com.didi.carrera.console.dao.model.Idc;
import com.didi.carrera.console.service.ClusterService;
import com.didi.carrera.console.service.IdcService;
import com.didi.carrera.console.web.ConsoleBaseResponse;
import com.didi.carrera.console.web.controller.bo.IdcBo;
import com.google.common.collect.Maps;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;


@Service("didiClusterServiceImpl")
public class ClusterServiceImpl implements ClusterService {

    @Autowired
    private ClusterMapper clusterMapper;

    @Autowired
    private IdcService idcService;

    @Override
    public Cluster findById(Long clusterId) {
        return clusterMapper.selectByPrimaryKey(clusterId);
    }

    private List<Cluster> findByIdcId(Long idcId) {
        ClusterCriteria cc = new ClusterCriteria();
        cc.createCriteria().andIdcIdEqualTo(idcId).andIsDeleteEqualTo(IsDelete.NO.getIndex());
        return clusterMapper.selectByExample(cc);
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

    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public ConsoleBaseResponse<?> initIdc() throws Exception {
        List<Cluster> clusterList = findAll();
        for (Cluster cluster : clusterList) {
            Long idcId;
            Idc idc = idcService.findByName(cluster.getIdc());
            if (idc == null) {
                IdcBo bo = new IdcBo(null, cluster.getIdc(), "");
                ConsoleBaseResponse<Long> ret = idcService.create(bo);
                if (!ret.isSuccess()) {
                    return ret;
                }
                idcId = ret.getData();
            } else {
                idcId = idc.getId();
            }

            cluster.setIdcId(idcId);
            clusterMapper.updateByPrimaryKeySelective(cluster);
        }

        return ConsoleBaseResponse.success();
    }

    @Override
    public void updateIdcName(Long idcId, String idcName) {
        List<Cluster> clusters = findByIdcId(idcId);
        if (CollectionUtils.isEmpty(clusters)) {
            return;
        }
        for (Cluster cluster : clusters) {
            cluster.setIdc(idcName);
            clusterMapper.updateByPrimaryKeySelective(cluster);
        }
    }
}