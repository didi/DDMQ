package com.xiaojukeji.carrera.pproxy.producer;

import com.xiaojukeji.carrera.config.v4.pproxy.KafkaConfiguration;
import com.xiaojukeji.carrera.config.v4.pproxy.RocketmqConfiguration;
import com.xiaojukeji.carrera.pproxy.utils.LogUtils;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class ProducerManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProducerManager.class);

    private ConfigManager configManager;

    private ConcurrentHashMap<String/*brokerCluster*/, ClusterProducer> producersOfCluster = new ConcurrentHashMap<>(8);

    public ProducerManager(ConfigManager configManager) {
        this.configManager = configManager;
    }

    public void initProducer() throws Exception {
        if (configManager.getCarreraConfig().isUseKafka() && MapUtils.isNotEmpty(configManager.getCarreraConfig().getKafkaConfigurationMap())) {
            // Kafka
            for (Map.Entry<String, KafkaConfiguration> kafkaConfig : configManager.getCarreraConfig().getKafkaConfigurationMap().entrySet()) {
                ClusterProducer kafkaProducer = new KafkaClusterProducer(configManager, kafkaConfig.getKey());
                kafkaProducer.initProducer();
                producersOfCluster.put(kafkaConfig.getKey(), kafkaProducer);
            }
        }

        if (configManager.getCarreraConfig().isUseRocketmq() && MapUtils.isNotEmpty(configManager.getCarreraConfig().getRocketmqConfigurationMap())) {
            // RocketMQ
            for (Map.Entry<String, RocketmqConfiguration> rmqConfig : configManager.getCarreraConfig().getRocketmqConfigurationMap().entrySet()) {
                ClusterProducer rmqProducer = new RmqClusterProducer(configManager, rmqConfig.getKey());
                rmqProducer.initProducer();
                producersOfCluster.put(rmqConfig.getKey(), rmqProducer);
            }
        }
    }

    public void warmUp() {
        long start = System.nanoTime();
        for (ClusterProducer producer : producersOfCluster.values()) {
            producer.warmUp();
        }
        LOGGER.info("WARM UP total cost {} ms", (System.nanoTime() - start) / 1e6);
    }

    public ClusterProducer getProducer(String brokerCluster) {
        if (!producersOfCluster.containsKey(brokerCluster)) {
            return null;
        }

        ClusterProducer producer = producersOfCluster.get(brokerCluster);
        if (producer == null) {
            return null;
        }

        if (producer.getType() == ProducerType.KAFKA && configManager.getCarreraConfig().isUseKafka()) {
            return producer;
        } else if (producer.getType() == ProducerType.RMQ && configManager.getCarreraConfig().isUseRocketmq()) {
            return producer;
        }

        return null;
    }

    public synchronized void addAndUpdateRmqProducer(String brokerCluster, ProducerType type) throws Exception {
        if (producersOfCluster.containsKey(brokerCluster)) {
            producersOfCluster.get(brokerCluster).updateConfig();
        } else {
            ClusterProducer producer;
            if (type == ProducerType.RMQ) {
                producer = new RmqClusterProducer(configManager, brokerCluster);
            } else {
                producer = new KafkaClusterProducer(configManager, brokerCluster);
            }
            producer.initProducer();
            producer.warmUp();
            producersOfCluster.put(brokerCluster, producer);
        }
    }

    public synchronized void deleteCluster(String brokerCluster) {
        if (producersOfCluster.containsKey(brokerCluster)) {
            ClusterProducer producer = producersOfCluster.remove(brokerCluster);
            producer.shutdown();
        }
    }

    public void shutdown() {
        LogUtils.getMainLogger().info("shut down producer manager");
        for (ClusterProducer producer : producersOfCluster.values()) {
            producer.shutdown();
        }
    }

}