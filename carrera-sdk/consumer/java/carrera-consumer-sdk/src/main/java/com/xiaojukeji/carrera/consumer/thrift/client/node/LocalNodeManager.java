package com.xiaojukeji.carrera.consumer.thrift.client.node;

import com.xiaojukeji.carrera.consumer.thrift.client.CarreraConfig;

import java.util.ArrayList;
import java.util.List;


public class LocalNodeManager extends NodeManager {
    List<Node> nodes = new ArrayList<>();

    public LocalNodeManager(CarreraConfig config) {
        super(config);
        String[] servers = config.getServers().split(";");
        for (String server : servers) {
            nodes.add(new Node(server));
        }
    }

    @Override
    public List<Node> getNodes() {
        return nodes;
    }
}