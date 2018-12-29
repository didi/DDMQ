package com.xiaojukeji.carrera.pproxy.producer;

import com.xiaojukeji.carrera.config.v4.pproxy.RocketmqConfiguration;
import com.xiaojukeji.carrera.pproxy.constants.Constant;
import com.xiaojukeji.carrera.pproxy.utils.LogUtils;
import com.xiaojukeji.carrera.pproxy.utils.RoundRobinPickerList;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.impl.producer.DefaultMQProducerImpl;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;


public class RmqClusterProducer implements ClusterProducer {
    private static final Logger LOGGER = LoggerFactory.getLogger(RmqClusterProducer.class);
    private static final String RMQ_WARM_UP_TOPIC = "BenchmarkTest";

    private RoundRobinPickerList<DefaultMQProducer> rocketmqProducers;
    private ReadWriteLock rocketMqProducerRWLock = new ReentrantReadWriteLock();
    private BatchMQProducer autoBatchRmqProducer;
    private ConfigManager configManager;
    private final String brokerCluster;

    public RmqClusterProducer(ConfigManager configManager, String brokerCluster) {
        this.configManager = configManager;
        this.brokerCluster = brokerCluster;
        rocketmqProducers = new RoundRobinPickerList<>();
    }

    @Override
    public void initProducer() throws Exception {
        buildRocketMqProducers();
    }

    private void buildRocketMqProducers() throws Exception {
        rocketmqProducers.addAll(buildRMQProducerList());

        if (configManager.getCarreraConfig().isUseAutoBatch()) {
            autoBatchRmqProducer = new BatchMQProducer(this, configManager.getCarreraConfig().getAutoBatch());
            autoBatchRmqProducer.start();
        }
    }

    @Override
    public void send(CarreraRequest request) throws Exception {
        if (canAutoBatch(request)) {
            autoBatchRmqProducer.send(request, RmqSender.RMQ_MQ_SELECTOR);
        } else {
            DefaultMQProducer producer = pickRocketMQProducer();
            if (request instanceof CarreraRequestForRMQBatch) {
                RmqSender.send(
                        producer,
                        request.toRmqMessage(),
                        request.getMessageQueue(),
                        request,
                        Constant.DEFAULT_MQ_SEND_TIMEOUT_MS
                );
            } else {
                RmqSender.send(
                        producer,
                        request,
                        request,
                        request.timeout
                );
            }
        }
    }

    @Override
    public void warmUp() {
        warmUp(rocketmqProducers);
    }

    @Override
    public ProducerType getType() {
        return ProducerType.RMQ;
    }

    @Override
    public int getPartitionsSize(String topic) {
        DefaultMQProducerImpl producer = pickRocketMQProducer().getDefaultMQProducerImpl();
        if (producer.isPublishTopicNeedUpdate(topic)) {
            if (!producer.getmQClientFactory().updateTopicRouteInfoFromNameServer(topic)) {
                throw new RuntimeException("Fetching the size of rmq partition failed!");
            }
        }
        return producer.getTopicPublishInfoTable().get(topic).getMessageQueueList().size();
    }

    @Override
    public void updateConfig() throws Exception {
        RoundRobinPickerList<DefaultMQProducer> producers = buildRMQProducerList();

        warmUp(producers);

        //swap
        RoundRobinPickerList<DefaultMQProducer> oldProducers;
        rocketMqProducerRWLock.writeLock().lock();
        oldProducers = rocketmqProducers;
        rocketmqProducers = producers;
        rocketMqProducerRWLock.writeLock().unlock();

        for (DefaultMQProducer oldProducer : oldProducers) {
            oldProducer.shutdown();
        }
    }

    @Override
    public void shutdown() {
        LOGGER.info("start closing kafka producer, broker cluster:{}", brokerCluster);
        for (DefaultMQProducer rocketmqProducer : rocketmqProducers) {
            rocketmqProducer.shutdown();
        }
    }

    private boolean canAutoBatch(CarreraRequest request) {
        return configManager.getCarreraConfig().isUseAutoBatch()
                && request.binarySize() < configManager.getCarreraConfig().getAutoBatch().getDoBatchThresholdBytes()
                && configManager.getTopicConfigManager().isAutoBatch(request.getTopic())
                && !(request instanceof CarreraRequestForRMQBatch)
                && request.getRetries() == 0; //重试的消息，不用批量，作为批量生产失败的兜底方案。 后续批量稳定后，再去掉这个。
    }

    private RoundRobinPickerList<DefaultMQProducer> buildRMQProducerList() throws Exception {
        RoundRobinPickerList<DefaultMQProducer> producers = new RoundRobinPickerList<>();

        RocketmqConfiguration config = configManager.getCarreraConfig().getRocketmqConfigurationMap().get(brokerCluster);
        if (config == null) {
            LogUtils.logError("RmqClusterProducer.buildRMQProducerList", "no cluster config, cluster name:" + brokerCluster);
            throw new Exception("no cluster config, cluster name:" + brokerCluster);
        }

        String producerGroupPrefix = config.getGroupPrefix() + "_" + String.valueOf(System.currentTimeMillis());
        for (int i = 0; i < configManager.getCarreraConfig().getRocketmqProducers(); i++) {
            DefaultMQProducer defaultMQProducer = new DefaultMQProducer(producerGroupPrefix + i);
            defaultMQProducer.setInstanceName(producerGroupPrefix + i);
            defaultMQProducer.setNamesrvAddr(StringUtils.join(config.getNamesrvAddrs().iterator(), ";"));
            defaultMQProducer.setSendMsgTimeout(config.getSendMsgTimeout());
            defaultMQProducer.setCompressMsgBodyOverHowmuch(config.getCompressMsgBodyOverHowmuch());
            defaultMQProducer.setRetryAnotherBrokerWhenNotStoreOK(config.isRetryAnotherBrokerWhenNotStoreOK());
            defaultMQProducer.setMaxMessageSize(config.getMaxMessageSize());
            defaultMQProducer.setClientCallbackExecutorThreads(config.getClientCallbackExecutorThreads());
            defaultMQProducer.setPollNameServerInterval(config.getPollNameServerInterval());
            defaultMQProducer.setHeartbeatBrokerInterval(config.getHeartbeatBrokerInterval());
            defaultMQProducer.setPersistConsumerOffsetInterval(config.getPersistConsumerOffsetInterval());
            // 同步发送，禁止rmq client重试
            defaultMQProducer.setRetryTimesWhenSendFailed(0);
            // 异步发送，禁止rmq client重试
            defaultMQProducer.setRetryTimesWhenSendAsyncFailed(0);
            defaultMQProducer.start();

            producers.add(defaultMQProducer);
        }

        return producers;
    }

    public DefaultMQProducer pickRocketMQProducer() {
        rocketMqProducerRWLock.readLock().lock();
        DefaultMQProducer producer = rocketmqProducers.pick();
        rocketMqProducerRWLock.readLock().unlock();
        return producer;
    }

    public DefaultMQProducer getRocketMQProducerByIndex(int index) {
        try {
            rocketMqProducerRWLock.readLock().lock();
            if (index < rocketmqProducers.size()) {
                return rocketmqProducers.get(index);
            }
            return null;
        } finally {
            rocketMqProducerRWLock.readLock().unlock();
        }
    }

    private void warmUp(RoundRobinPickerList<DefaultMQProducer> rmqProducers) {
        List<String> warmUpTopics = configManager.getTopicConfigManager().getTopicConfigs().keySet().stream().collect(Collectors.toList());
        List<Thread> threads = new ArrayList<>();

        long start = System.nanoTime();
        rmqProducers.forEach(producer -> {
            Thread thread = new Thread(() -> {
                if (configManager.getCarreraConfig().isWarmUpFetchTopicRouteInfo() && CollectionUtils.isNotEmpty(warmUpTopics)) {
                    for (String warmUpTopic : warmUpTopics) {
                        if (configManager.getTopicConfigManager().containsCluster(warmUpTopic, brokerCluster)) {
                            long s = System.nanoTime();
                            producer.getDefaultMQProducerImpl().getmQClientFactory().updateTopicRouteInfoFromNameServer(warmUpTopic);
                            LOGGER.info("[WARM UP RMQ] fetch topic route info {} info cost {} ms", warmUpTopic, (System.nanoTime() - s) / 1e6);
                        }
                    }
                }

                if (configManager.getCarreraConfig().isWarmUpConnection()) {
                    try {
                        List<MessageQueue> mqs = producer.fetchPublishMessageQueues(RMQ_WARM_UP_TOPIC);
                        for (MessageQueue mq : mqs) {
                            long s = System.currentTimeMillis();
                            producer.send(new Message(RMQ_WARM_UP_TOPIC, new byte[]{0, 1, 2}), mq);
                            LOGGER.info("[WARM UP RMQ] send message to mq B-{},Q-{} cost {} ms", mq.getBrokerName(),
                                    mq.getQueueId(), System.currentTimeMillis() - s);
                        }
                    } catch (Exception e) {
                        LogUtils.logError("RmqClusterProducer.warmUp", "[WARM UP ROCKETMQ] ERROR", e);
                    }
                }
            }, "RMQ-WARM-UP-THREAD-" + producer.getProducerGroup());

            thread.start();
            threads.add(thread);
        });

        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                LogUtils.logError("RmqClusterProducer.warmUp", "join warm thread exception", e);
            }
        }
        LOGGER.info("WARM UP broker cluster:{}, total cost {} ms", brokerCluster, (System.nanoTime() - start) / 1e6);
    }

}