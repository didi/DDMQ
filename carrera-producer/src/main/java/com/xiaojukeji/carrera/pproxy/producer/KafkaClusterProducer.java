package com.xiaojukeji.carrera.pproxy.producer;

import com.xiaojukeji.carrera.config.v4.pproxy.KafkaConfiguration;
import com.xiaojukeji.carrera.pproxy.utils.LogUtils;
import com.xiaojukeji.carrera.pproxy.utils.RandomUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;


public class KafkaClusterProducer implements ClusterProducer {
    private static final Logger LOGGER = LoggerFactory.getLogger(RmqClusterProducer.class);
    private List<KafkaProducer<String, byte[]>> kafkaProducers;
    private ReadWriteLock kafkaProducerRWLock = new ReentrantReadWriteLock();
    private ConfigManager configManager;
    private final String brokerCluster;

    public KafkaClusterProducer(ConfigManager configManager, String brokerCluster) {
        this.configManager = configManager;
        this.brokerCluster = brokerCluster;
        kafkaProducers = new ArrayList<>();
    }

    @Override
    public void initProducer() throws Exception {
        buildKafkaProducers();
    }

    @Override
    public void send(CarreraRequest request) {
        KafkaProducer<String, byte[]> producer = getProducer();
        producer.send(request.toKafkaRecord(producer), request::onKafkaCompletion);
    }

    @Override
    public int getPartitionsSize(String topic) {
        return getProducer().partitionsFor(topic).size();
    }

    @Override
    public void updateConfig() throws Exception {
        List<KafkaProducer<String, byte[]>> producers = buildProducerList();

        List<KafkaProducer<String, byte[]>> oldProducers;
        kafkaProducerRWLock.writeLock().lock();
        oldProducers = kafkaProducers;
        kafkaProducers = producers;
        kafkaProducerRWLock.writeLock().unlock();

        for (KafkaProducer producer : oldProducers) {
            producer.close();
        }
    }

    @Override
    public ProducerType getType() {
        return ProducerType.KAFKA;
    }

    @Override
    public void shutdown() {
        LOGGER.info("start closing kafka producer, broker cluster:{}", brokerCluster);
        for (KafkaProducer<String, byte[]> kafkaProducer : kafkaProducers) {
            kafkaProducer.close();
        }
    }

    @Override
    public void warmUp() {
        warmUp(kafkaProducers);
    }

    private void buildKafkaProducers() throws Exception {
        kafkaProducers.addAll(buildProducerList());
    }

    private List<KafkaProducer<String, byte[]>> buildProducerList() throws Exception {
        List<KafkaProducer<String, byte[]>> producers = new ArrayList<>();
        KafkaConfiguration config = configManager.getCarreraConfig().getKafkaConfigurationMap().get(brokerCluster);
        if (config == null) {
            LogUtils.logError("KafkaClusterProducer.buildProducerList", "no cluster config, cluster name:" + brokerCluster);
            throw new Exception("no cluster config, cluster name:" + brokerCluster);
        }

        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, config.getBootstrapServers());
        props.put(ProducerConfig.ACKS_CONFIG, config.getAcks());
        if (config.getRetries() != -1) {
            props.put(ProducerConfig.RETRIES_CONFIG, config.getRetries());
        }
        if (config.getBatchSize() != -1) {
            props.put(ProducerConfig.BATCH_SIZE_CONFIG, config.getBatchSize());
        }
        if (config.getRetryBackoff() != -1) {
            props.put(ProducerConfig.RETRY_BACKOFF_MS_CONFIG, config.getRetryBackoff());
        }
        if (config.getMetadataMaxAge() != -1) {
            props.put(ProducerConfig.METADATA_MAX_AGE_CONFIG, config.getMetadataMaxAge());
        }

        props.put(ProducerConfig.TIMEOUT_CONFIG, config.getAckTimeout());
        props.put(ProducerConfig.BUFFER_MEMORY_CONFIG, config.getBufferMemory());
        props.put(ProducerConfig.BLOCK_ON_BUFFER_FULL_CONFIG, false);
        props.put(ProducerConfig.METADATA_FETCH_TIMEOUT_CONFIG, config.getMetadataFetchTimeout());

        String hostname = "UNKNOWN_HOST";
        try {
            hostname = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            LogUtils.logError("getHostname", e);
        }

        String clientId = config.getClientId() + "-" + System.currentTimeMillis();
        for (int i = 0; i < configManager.getCarreraConfig().getKafkaProducers(); i++) {
            props.put(ProducerConfig.CLIENT_ID_CONFIG, clientId + "-" + hostname + i);
            producers.add(new KafkaProducer<>(props, new StringSerializer(), new ByteArraySerializer()));
            LOGGER.info("build kafka producer with properties : " + props.toString());
        }

        return producers;
    }

    private KafkaProducer<String, byte[]> getProducer() {
        try {
            kafkaProducerRWLock.readLock().lock();
            return RandomUtils.pick(kafkaProducers);
        } finally {
            kafkaProducerRWLock.readLock().unlock();
        }
    }

    private void warmUp(List<KafkaProducer<String, byte[]>> producers) {
        List<String> warmUpTopics = new ArrayList<>(configManager.getTopicConfigManager().getTopicConfigs().keySet());
        long start = System.nanoTime();
        List<Thread> threads = new ArrayList<>();

        if (CollectionUtils.isNotEmpty(warmUpTopics)) {
            for (int i = 0; i < producers.size(); i++) {
                final KafkaProducer producer = producers.get(i);
                Thread thread = new Thread(() -> warmUpTopics.forEach(topic -> {
                    if (configManager.getTopicConfigManager().containsCluster(topic, brokerCluster)) {
                        long s = System.currentTimeMillis();
                        try {
                            producer.partitionsFor(topic);
                        } catch (Exception e) {
                            LogUtils.logError("KafkaClusterProducer.warmUp", "[WARM UP KAFKA] get partition for topic(" + topic + ")Failed.", e);
                        }
                        LOGGER.info("[WARM UP KAFKA] fetch partitions info {} info cost {} ms", topic,
                                System.currentTimeMillis() - s);
                    }
                }), "KAFKA-WARM-UP-THREAD-" + i);
                thread.start();
                threads.add(thread);
            }

            for (Thread thread : threads) {
                try {
                    thread.join();
                } catch (InterruptedException e) {
                    LogUtils.logError("KafkaClusterProducer.warmUp", "join warm thread exception", e);
                }
            }
        }

        LOGGER.info("WARM UP broker cluster:{}, total cost {} ms", brokerCluster, (System.nanoTime() - start) / 1e6);
    }

}