package com.didi.carrera.console.service;

import com.didi.carrera.console.dao.model.Cluster;
import com.didi.carrera.console.web.ConsoleBaseResponse;

import java.util.List;
import java.util.Map;


public interface ClusterService {

    Cluster findById(Long clusterId);

    Cluster findByClusterName(String clusterName);

    List<Cluster> findAll();

    void updateIdcName(Long idcId, String idcName);

    Map<Long, Cluster> findMap();

    ConsoleBaseResponse<?> initIdc() throws Exception;
}