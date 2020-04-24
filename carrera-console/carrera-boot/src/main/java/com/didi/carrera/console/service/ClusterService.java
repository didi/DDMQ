package com.didi.carrera.console.service;

import com.didi.carrera.console.dao.model.Cluster;

import java.util.List;
import java.util.Map;


public interface ClusterService {

    Cluster findById(Long clusterId);

    Cluster findByClusterName(String clusterName);

    List<Cluster> findAll();

    Map<Long, Cluster> findMap();
}