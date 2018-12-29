package com.xiaojukeji.carrera.pproxy.producer;


public interface ClusterProducer {

    void initProducer() throws Exception;

    void warmUp();

    void updateConfig() throws Exception;

    ProducerType getType();

    int getPartitionsSize(String topic);

    void send(CarreraRequest request) throws Exception;

    void shutdown();

}