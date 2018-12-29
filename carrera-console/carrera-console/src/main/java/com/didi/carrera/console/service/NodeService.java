package com.didi.carrera.console.service;

import com.didi.carrera.console.dao.dict.NodeType;
import com.didi.carrera.console.dao.model.Node;
import com.didi.carrera.console.web.ConsoleBaseResponse;
import com.didi.carrera.console.web.controller.bo.NodeBo;

import java.util.List;


public interface NodeService {

    Node findById(Long nodeId);

    List<Node> findByClusterId(Long clusterId);

    List<Node> findByClusterIdNodeType(Long clusterId, NodeType nodeType);

    List<Node> findByClusterIdNodeType(Long clusterId, List<NodeType> nodeType);

    List<Node> findByHostNodeType(String host, NodeType nodeType);

    List<Node> findByClusterHostNodeType(Long clusterId, String host, NodeType nodeType);

    List<Node> findByHostNodeType(String host, List<NodeType> nodeTypes);

    ConsoleBaseResponse<?> create(NodeBo bo) throws Exception;

    List<Node> findAll();

    boolean isSupportHttpCluster(Long clusterId);
}