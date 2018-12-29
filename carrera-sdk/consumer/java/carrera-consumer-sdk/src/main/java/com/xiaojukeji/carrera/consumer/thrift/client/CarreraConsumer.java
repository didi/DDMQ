package com.xiaojukeji.carrera.consumer.thrift.client;

import com.xiaojukeji.carrera.consumer.thrift.ConsumeStats;
import com.xiaojukeji.carrera.consumer.thrift.client.node.Node;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;


public class CarreraConsumer extends BaseCarreraConsumerPool {

    private static final Logger LOGGER = LoggerFactory.getLogger(CarreraConsumer.class);
    public static final String CARRERA_CONSUMER_TYPE = "carrera";

    private volatile List<SimpleCarreraConsumer> consumeStatsFetcherList;

    private volatile int consumerIdx = 0;

    private static final int GET_CONSUME_STATS_MAX_RETRY = 3;

    public CarreraConsumer(CarreraConfig config) {
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

    public synchronized void startConsume(MessageProcessor processor, int concurrency, Map<String, Integer> extraConcurrency) {
        baseStartConsume(processor, concurrency, extraConcurrency);
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

    public synchronized void startConsume(BatchMessageProcessor processor, int concurrency, Map<String, Integer> extraConcurrency) {
        baseStartConsume(processor, concurrency, extraConcurrency);
    }


    @Deprecated
    public List<ConsumeStats> getConsumeStats() throws TException {
        return getConsumeStats(null);
    }

    @Deprecated
    public List<ConsumeStats> getConsumeStats(String topic) throws TException {
        return getConsumeStats(topic, GET_CONSUME_STATS_MAX_RETRY);
    }

    @Deprecated
    private synchronized List<ConsumeStats> getConsumeStats(String topic, int retry) throws TException {

        int index = ++consumerIdx % consumeStatsFetcherList.size();
        if (consumerIdx >= consumeStatsFetcherList.size()) {
            consumerIdx = 0;
        }
        try {
            return consumeStatsFetcherList.get(index).getConsumeStats(topic);
        } catch (Exception e) {
            consumeStatsFetcherList.get(index).close();
            if ((retry - 1) > 0) {
                return getConsumeStats(topic, retry - 1);
            } else {
                throw e;
            }
        }
    }

    @Deprecated
    public void close() {
        if (consumeStatsFetcherList != null) {
            for (SimpleCarreraConsumer consumer : consumeStatsFetcherList) {
                consumer.close();
            }
        }
    }

    @Override
    protected void init(List<Node> nodes) {
        consumeStatsFetcherList = new ArrayList<>();
        startFetcher(nodes);
    }

    private void startFetcher(List<Node> nodes) {
        for (Node node : nodes) {
            CarreraConfig newConfig = config.clone();
            newConfig.setServers(node.toStrStyle());
            SimpleCarreraConsumer consumer = new SimpleCarreraConsumer(newConfig);
            consumer.setType(getConsumeType());
            consumeStatsFetcherList.add(consumer);
        }
    }

    @Override
    protected BaseCarreraConsumer createConsumer(CarreraConfig config, String topic) {
        return new SimpleCarreraConsumer(config, topic);
    }


    @Override
    protected String getConsumeType() {
        return CARRERA_CONSUMER_TYPE;
    }

    @Override
    protected synchronized void updateNode(List<Node> nodes) {
        close();
        consumeStatsFetcherList.clear();
        startFetcher(nodes);
    }

    @Override
    public synchronized void stop() {
        super.stop();
        LOGGER.info("to shutdown fetcher, {}", consumeStatsFetcherList);
        for (SimpleCarreraConsumer consumer : consumeStatsFetcherList) {
            consumer.stop();
        }
        LOGGER.info("consumer stop");
    }
}