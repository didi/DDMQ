package com.xiaojukeji.carrera.consumer.thrift.client;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.xiaojukeji.carrera.consumer.thrift.client.node.Node;
import com.xiaojukeji.carrera.consumer.thrift.client.node.NodeManager;
import com.xiaojukeji.carrera.consumer.thrift.client.node.NodeUpdateInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


public abstract class BaseCarreraConsumerPool {

    private static final Logger LOGGER = LoggerFactory.getLogger(BaseCarreraConsumerPool.class);
    protected CarreraConfig config;

    private volatile ExecutorService executorService;

    private ConcurrentHashMap<Node, ConcurrentLinkedQueue<BaseCarreraConsumer>> consumerMap = new ConcurrentHashMap<>();

    private NodeManager nodeManager;
    private BaseMessageProcessor msgProcessor;
    private int concurrency;
    private Map<String, Integer> extraConcurrency;


    protected BaseCarreraConsumerPool(CarreraConfig config) {
        this.config = config;
        initNodeManager();
        init(nodeManager.getNodes());
    }

    private void initNodeManager() {
        NodeUpdateInterface updateInterface = new NodeUpdateInterface() {
            @Override
            public boolean updateNodes(List<Node> nodes) throws Exception {
                return BaseCarreraConsumerPool.this.onUpdateNodes(nodes);
            }
        };
        nodeManager = NodeManager.buildNodeManager(config, updateInterface);

        try {
            if (!nodeManager.start()) {
                LOGGER.warn("do not get nodes info, will retry later");
            } else {
                LOGGER.info("current nodes:" + nodeManager.getNodes());
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex.getMessage());
        }
    }

    public CarreraConfig getConfig() {
        return config;
    }

    public void setConfig(CarreraConfig config) {
        this.config = config;
    }

    protected void baseStartConsume(BaseMessageProcessor processor, int concurrency, Map<String, Integer> extraConcurrency) {
        if (executorService != null) {
            throw new RuntimeException("consumer already running.");
        }

        config.validate(false);
        List<Node> servers = nodeManager.getNodes();
        startConsume(processor, concurrency, extraConcurrency, servers);

        //save for update
        this.msgProcessor = processor;
        this.concurrency = concurrency;
        this.extraConcurrency = extraConcurrency;
    }

    private void startConsume(BaseMessageProcessor processor, int concurrency, Map<String, Integer> extraConcurrency, List<Node> servers) {
        int totalThreads = concurrency > 0 ? Math.max(concurrency, servers.size()) : 0;
        for (Integer topicConcurrency : extraConcurrency.values()) {
            totalThreads += topicConcurrency > 0 ? Math.max(topicConcurrency, servers.size()) : 0;
        }
        if (totalThreads == 0) {
            throw new RuntimeException("concurrency is too small, at least one for each server.");
        }
        executorService = Executors.newFixedThreadPool(totalThreads, new ThreadFactoryBuilder().setNameFormat("MessageProcess-%d").build());

        Collections.shuffle(servers);

        int serverCnt = servers.size();
        if (concurrency > 0) {
            if (concurrency < serverCnt) {
                LOGGER.warn("concurrency({})<server number({}), use {} as concurrency", concurrency, serverCnt, serverCnt);
                concurrency = serverCnt;
            }
            for (int i = 0; i < serverCnt; i++) {
                int threadNumber = concurrency / serverCnt;
                threadNumber += i < concurrency % serverCnt ? 1 : 0;
                if (threadNumber == 0) {
                    LOGGER.warn("no thread for server:{}", servers.get(i));
                } else {
                    createConsumer(processor, threadNumber, servers.get(i), null);
                }
            }
        }

        for (Map.Entry<String, Integer> entry : extraConcurrency.entrySet()) {
            int c = entry.getValue();
            if (c == 0) continue;
            if (c < serverCnt) {
                LOGGER.warn("concurrency({})<server number({}), use {} as concurrency", c, serverCnt, serverCnt);
                c = serverCnt;
            }
            Collections.shuffle(servers);
            for (int i = 0; i < serverCnt; i++) {
                int threadNumber = c / serverCnt;
                threadNumber += i < c % serverCnt ? 1 : 0;
                createConsumer(processor, threadNumber, servers.get(i), entry.getKey());
            }
        }
    }

    protected void init(List<Node> nodes) {
    }

    protected void updateNode(List<Node> nodes) {
    }

    public synchronized boolean onUpdateNodes(List<Node> nodes) throws Exception {
        LOGGER.info("nodes change, current node:" + nodes);
        //todo:可以不用全部重启，待优化
        BaseCarreraConsumerPool.this.stopConsume();
        LOGGER.info("stop old connections, and will start new connections");
        startConsume(msgProcessor, concurrency, extraConcurrency, nodes);
        updateNode(nodes);
        LOGGER.info("restart new connections");
        return true;
    }

    private void createConsumer(final BaseMessageProcessor processor, int consumerNumber,
                                Node server, String topic) {
        for (int i = 0; i < consumerNumber; i++) {
            CarreraConfig newConfig = config.clone();
            newConfig.setServers(server.toStrStyle());
            final BaseCarreraConsumer consumer = createConsumer(newConfig, topic);

            consumer.setType(getConsumeType());
            if (!consumerMap.containsKey(server)) {
                consumerMap.put(server, new ConcurrentLinkedQueue<BaseCarreraConsumer>());
            }
            consumerMap.get(server).add(consumer);
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        consumer.startConsume(processor);
                    } catch (InterruptedException e) {
                        if (consumer.isStopped()) {
                            LOGGER.info("consumer finished!");
                        } else {
                            LOGGER.error("consumer is interrupted!", e);
                        }
                    }
                }
            });
        }
    }

    protected abstract BaseCarreraConsumer createConsumer(CarreraConfig config, String topic);

    protected abstract String getConsumeType();

    private void stopBaseConsumers() {
        if (consumerMap != null) {
            for (Map.Entry<Node, ConcurrentLinkedQueue<BaseCarreraConsumer>> entry : consumerMap.entrySet()) {
                if (entry.getValue() == null || entry.getValue().isEmpty()) {
                    continue;
                }
                for (BaseCarreraConsumer consumer : entry.getValue()) {
                    if (!consumer.isStopped()) {
                        consumer.stop();
                    }
                }
            }
        }
    }

    private void stopConsume() {
        stopBaseConsumers();

        if (executorService != null) {
            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(config.getRetryInterval(), TimeUnit.MILLISECONDS)) {
                    LOGGER.warn("do not stop all consumer, retry");
                    stopBaseConsumers();
                    if (!executorService.awaitTermination(2 * config.getRetryInterval(), TimeUnit.MILLISECONDS)) {
                        executorService.shutdownNow();
                    }
                }
            } catch (Exception ex) {
                LOGGER.warn("executorService shut down failed", ex);
            }

            executorService = null;
        }
        consumerMap.clear();
    }

    public synchronized void stop() {
        stopConsume();

        if (nodeManager != null) {
            nodeManager.shutdown();
        }
    }
}