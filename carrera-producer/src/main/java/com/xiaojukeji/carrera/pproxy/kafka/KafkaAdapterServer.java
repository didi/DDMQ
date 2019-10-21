package com.xiaojukeji.carrera.pproxy.kafka;

import com.xiaojukeji.carrera.pproxy.kafka.network.SocketServer;
import com.xiaojukeji.carrera.pproxy.kafka.server.KafkaApis;
import com.xiaojukeji.carrera.pproxy.kafka.server.KafkaRequestHandlerPool;
import com.xiaojukeji.carrera.pproxy.producer.ConfigManager;
import com.xiaojukeji.carrera.pproxy.producer.ProducerPool;
import com.xiaojukeji.carrera.pproxy.server.ProducerAsyncServerImpl;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

public class KafkaAdapterServer {

    private KafkaConfig kafkaConfig;
    private AtomicBoolean startupComplete = new AtomicBoolean(false);
    private AtomicBoolean isShuttingDown = new AtomicBoolean(false);
    private AtomicBoolean isStartingUp = new AtomicBoolean(false);
    private SocketServer socketServer;
    private KafkaRequestHandlerPool requestHandlerPool;
    private ProducerPool producerPool;
    private ProducerAsyncServerImpl serverImpl;
    private ConfigManager configManager;

    private CountDownLatch shutdownLatch = new CountDownLatch(1);

    public KafkaAdapterServer(KafkaConfig kafkaConfig, ProducerPool producerPool, ConfigManager configManager) {
        this.kafkaConfig = kafkaConfig;
        this.producerPool = producerPool;
        this.configManager = configManager;
    }

    public void startup() {
        socketServer = new SocketServer(kafkaConfig);
        socketServer.startup();
        KafkaApis apis = new KafkaApis(socketServer.getRequestChannel(), producerPool, configManager);
        requestHandlerPool = new KafkaRequestHandlerPool(socketServer.getRequestChannel(), apis, kafkaConfig.getNumIoThreads());
    }

    public void shutdown() {

    }

    public KafkaConfig getKafkaConfig() {
        return kafkaConfig;
    }
}
