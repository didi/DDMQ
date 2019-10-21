package com.xiaojukeji.carrera.pproxy.kafka;

import com.xiaojukeji.carrera.config.v4.pproxy.RocketmqConfiguration;
import com.xiaojukeji.carrera.pproxy.utils.QidUtils;
import org.apache.kafka.common.TopicPartition;
import org.apache.logging.log4j.util.Strings;
import org.apache.rocketmq.client.consumer.DefaultMQPullConsumer;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.message.MessageQueue;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

public class QueueToPartitionManager {

    private Map<String/*topic*/,Map<String/*qid*/, Integer/*partition*/>> qidToPartitionMap = new ConcurrentHashMap<>();//todo future 需要用持久化的映射代替
    private Map<String/*topic*/,Map<String/*qid*/, MessageQueue>> qidToMessageQueueMap = new ConcurrentHashMap<>();
    private Map<String/*topic*/,Map<Integer/*partition*/, MessageQueue>> partitionToMessageQueueMap = new ConcurrentHashMap<>();
    private Map<String/*group*/,DefaultMQPullConsumer> consumersMap = new ConcurrentHashMap<>();//todo future 一个topic可以属于多个集群，因此也需要扩展下
    private String clusterName;
    private DefaultMQPullConsumer queueMetaDataConsumer;

    public QueueToPartitionManager(String clusterName, Map<String, RocketmqConfiguration> rocketmqConfigurationMap) {
        RocketmqConfiguration rocketmqConfiguration = rocketmqConfigurationMap.values().iterator().next();//todo 暂且只支持一个
        queueMetaDataConsumer = new DefaultMQPullConsumer();
        queueMetaDataConsumer.setConsumerGroup("GetQueueMetaDataGroup");
        queueMetaDataConsumer.setNamesrvAddr(Strings.join(rocketmqConfiguration.getNamesrvAddrs().iterator(), ';'));
        try {
            queueMetaDataConsumer.start();
        } catch (MQClientException e) {
            throw new RuntimeException("start GetQueueMetaDataGroup failed");
        }
        this.clusterName = clusterName;
    }

    private boolean isContain(String topic) {
        return qidToPartitionMap.containsKey(topic);
    }

    public synchronized void initMessageQueueInfo(String topic) throws MQClientException {
        if (!isContain(topic)) {
            qidToPartitionMap.putIfAbsent(topic,new ConcurrentHashMap<>());
            partitionToMessageQueueMap.putIfAbsent(topic,new ConcurrentHashMap<>());
            qidToMessageQueueMap.putIfAbsent(topic, new ConcurrentHashMap<>());
        }
        TreeSet<MessageQueue> messageQueues = new TreeSet<>(queueMetaDataConsumer.fetchSubscribeMessageQueues(topic));
        updateMessageQueueInfo(clusterName, topic, messageQueues);
    }

    public synchronized void updateMessageQueueInfo(String clusterName, String topic, Set<MessageQueue> queueSet) {
        Map<String,Integer> newQueueToQidInfo = new ConcurrentHashMap<>();
        Map<Integer,MessageQueue> newQidToQueueMapInfo = new ConcurrentHashMap<>();
        Map<String,MessageQueue> newQidToMessageQueue = new ConcurrentHashMap<>();
        TreeSet<MessageQueue> messageQueues = new TreeSet<>(queueSet);
        int index = 0;
        Iterator<MessageQueue> iterator = messageQueues.iterator();
        while(iterator.hasNext()){
            MessageQueue messageQueue = iterator.next();
            String qid = QidUtils.rmqMakeQid(clusterName,messageQueue.getBrokerName(),messageQueue.getQueueId());
            newQueueToQidInfo.put(qid, index);
            newQidToQueueMapInfo.put(index, messageQueue);
            newQidToMessageQueue.put(qid, messageQueue);
            index++;
        }
        qidToPartitionMap.put(topic, newQueueToQidInfo);
        partitionToMessageQueueMap.put(topic, newQidToQueueMapInfo);
        qidToMessageQueueMap.put(topic,newQidToMessageQueue);
    }

    /**
     * 每个集群添加一个consumer,用于查询queue信息，做queue和partition之间的转换
     */
    public synchronized void addClusterConsumer(String group,DefaultMQPullConsumer consumer) {
        consumersMap.putIfAbsent(clusterName, consumer);
    }

    public String getClusterName() {//todo future暂时先只支持一个集群
        return this.clusterName;
    }

    public DefaultMQPullConsumer getConsumer(String clusterName, String group) {
        return consumersMap.get(clusterName);
    }

    public Map<String,Integer> getQueueToQidInfo(String topic) {
        return qidToPartitionMap.get(topic);
    }

    public Map<Integer, MessageQueue> getQidToQueueInfo(String topic) {
        return partitionToMessageQueueMap.get(topic);
    }

    public MessageQueue getMessageQueueByQid(String topic, String qid) {
        return null == qidToMessageQueueMap.get(topic) ? null : qidToMessageQueueMap.get(topic).get(qid);
    }

    public MessageQueue topicPartitionToMessageQueue(TopicPartition topicPartition) {
        return partitionToMessageQueueMap.get(topicPartition.topic()).get(topicPartition.partition());
    }

    public long getInitConsumerOffset(String group, TopicPartition topicPartition) {
        MessageQueue messageQueue = topicPartitionToMessageQueue(topicPartition);
        try {
            String clusterName = getClusterName();
            DefaultMQPullConsumer consumer = getConsumer(clusterName, group);//todo 如果为空，创建个该group的消费者
            long consumeOffset = consumer.fetchConsumeOffset(messageQueue, true);
            long minOffset = consumer.minOffset(messageQueue);
            return Math.max(minOffset, consumeOffset);
        } catch (MQClientException e) {
            throw new KafkaAdapterException("fetchConsumeOffset exception", e);
        }

    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public void shutdown() {
        consumersMap.values().forEach(consumer -> {
            consumer.shutdown();
        });
    }

    public void setQueueMetaDataConsumer(DefaultMQPullConsumer queueMetaDataConsumer) {
        this.queueMetaDataConsumer = queueMetaDataConsumer;
    }

}
