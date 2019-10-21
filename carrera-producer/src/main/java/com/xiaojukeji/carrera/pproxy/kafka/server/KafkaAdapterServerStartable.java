package com.xiaojukeji.carrera.pproxy.kafka.server;

import com.xiaojukeji.carrera.pproxy.kafka.KafkaAdapterServer;
import com.xiaojukeji.carrera.pproxy.kafka.KafkaConfig;
import com.xiaojukeji.carrera.pproxy.kafka.LoggerUtils;
import com.xiaojukeji.carrera.pproxy.producer.ConfigManager;
import com.xiaojukeji.carrera.pproxy.producer.ProducerPool;
import com.xiaojukeji.carrera.pproxy.server.ProducerAsyncServerImpl;

import java.util.concurrent.CountDownLatch;

public class KafkaAdapterServerStartable {

    private KafkaAdapterServer kafkaAdapterServer;
    private CountDownLatch awaitForShutDown = new CountDownLatch(1);

    public KafkaAdapterServerStartable(KafkaConfig kafkaConfig, ProducerPool producerPool, ConfigManager configManager) {
        kafkaAdapterServer = new KafkaAdapterServer(kafkaConfig,producerPool, configManager);
    }

    public void startup () {
        kafkaAdapterServer.startup();
        Runtime.getRuntime().addShutdownHook(new Thread("kafka-adapter-shutdown-hook") {
            @Override
            public void run() {
                try {
                    LoggerUtils.KafkaAdapterLog.info("start to stop kafka adapter...");
                    final long start = System.currentTimeMillis();
                    kafkaAdapterServer.shutdown();
                    final long cost = System.currentTimeMillis() - start;
                    LoggerUtils.KafkaAdapterLog.info("succ stop kafka adapter, cost:{}ms", cost);
                    awaitForShutDown.countDown();
                } catch (Exception e) {
                    LoggerUtils.KafkaAdapterLog.error("error while shutdown kafka adapter, err:{}", e.getMessage(), e);
                }
            }
        });
    }

    public void awaitShutdown() throws InterruptedException {
        awaitForShutDown.await();
    }
}
