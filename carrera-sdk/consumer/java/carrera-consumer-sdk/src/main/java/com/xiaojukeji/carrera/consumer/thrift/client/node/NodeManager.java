package com.xiaojukeji.carrera.consumer.thrift.client.node;

import com.xiaojukeji.carrera.consumer.thrift.client.CarreraConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;


public abstract class NodeManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(NodeManager.class);
    protected CarreraConfig config;

    public NodeManager(CarreraConfig config) {
        this.config = config;
    }

    public boolean start() throws Exception {
        return true;
    }

    public void shutdown() {
    }

    public CarreraConfig getConfig() {
        return config;
    }

    public abstract List<Node> getNodes();

    public static NodeManager buildNodeManager(CarreraConfig config, NodeUpdateInterface nodeUpdate) {
        return new LocalNodeManager(config);
    }

}