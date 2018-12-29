package com.xiaojukeji.carrera.consumer.thrift.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;


public class LowLevelPoolCarreraConsumer extends BaseCarreraConsumerPool {

    private static final Logger LOGGER = LoggerFactory.getLogger(LowLevelPoolCarreraConsumer.class);

    public LowLevelPoolCarreraConsumer(LowLevelCarreraConfig config) {
        super(config);
    }

    /**
     * 启动消费线程
     *
     * @param processor
     * @param concurrency 总的消费线程数。每个线程只能消费一个server。concurrency 小于server数时，将直接使用server数作为concurrency。
     */
    public synchronized void startConsume(MessageProcessor processor, int concurrency) {
        baseStartConsume(processor, concurrency, Collections.EMPTY_MAP);
    }

    /**
     * 启动消费线程
     *
     * @param processor
     * @param concurrency 总的消费线程数。每个线程只能消费一个server。concurrency 小于server数时，将直接使用server数作为concurrency。
     */
    public synchronized void startConsume(BatchMessageProcessor processor, int concurrency) {
        baseStartConsume(processor, concurrency, Collections.EMPTY_MAP);
    }

    @Override
    protected BaseCarreraConsumer createConsumer(CarreraConfig config, String topic) {
        return new LowLevelCarreraConsumer((LowLevelCarreraConfig) config);
    }

    @Override
    protected String getConsumeType() {
        return "carrera_lowlevel";
    }
}