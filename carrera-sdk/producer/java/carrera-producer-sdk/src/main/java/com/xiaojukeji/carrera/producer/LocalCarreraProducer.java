package com.xiaojukeji.carrera.producer;

import com.xiaojukeji.carrera.config.CarreraConfig;
import com.xiaojukeji.carrera.nodemgr.NodeManager;


public class LocalCarreraProducer extends CarreraProducerBase implements ProducerInterface {

    public LocalCarreraProducer(CarreraConfig config) {
        super(config);
    }

    @Override
    protected void initNodeMgr() throws Exception {
        nodeMgr = NodeManager.newLocalNodeManager(config, config.getCarreraProxyList());
        nodeMgr.initConnectionPool();
    }
}