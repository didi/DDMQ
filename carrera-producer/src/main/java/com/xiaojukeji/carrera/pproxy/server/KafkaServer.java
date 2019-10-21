package com.xiaojukeji.carrera.pproxy.server;

import com.xiaojukeji.carrera.pproxy.kafka.KafkaConfig;
import com.xiaojukeji.carrera.pproxy.kafka.server.KafkaAdapterServerStartable;
import com.xiaojukeji.carrera.pproxy.producer.ConfigManager;
import com.xiaojukeji.carrera.pproxy.producer.ProducerPool;

public class KafkaServer implements Server{

    KafkaAdapterServerStartable kafkaAdapterServerStartable;

    public KafkaServer(ProducerPool producerPool, ConfigManager configManager) {
        KafkaConfig kafkaConfig = new KafkaConfig();
        kafkaAdapterServerStartable = new KafkaAdapterServerStartable(kafkaConfig, producerPool, configManager);
    }

    @Override
    public void startServer() {
        kafkaAdapterServerStartable.startup();
    }

    @Override
    public void stopServer() {

    }
}
