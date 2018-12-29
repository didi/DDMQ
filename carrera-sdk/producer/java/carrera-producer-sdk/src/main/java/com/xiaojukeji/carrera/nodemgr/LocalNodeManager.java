package com.xiaojukeji.carrera.nodemgr;

import com.xiaojukeji.carrera.config.CarreraConfig;

import java.util.List;


public class LocalNodeManager extends NodeManager {

    public LocalNodeManager(CarreraConfig config, List<String> hosts) {
        super(config);
        for (String host : hosts) {
            Node node = new Node(host);
            allNodesMap.put(node, new NodeInfo(host));
            healthyNodes.add(node);
        }
    }
}