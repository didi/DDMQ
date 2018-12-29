package com.xiaojukeji.carrera.cproxy.consumer.offset;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import kafka.api.*;
import kafka.client.ClientUtils;
import kafka.cluster.Broker;
import kafka.common.TopicAndPartition;
import kafka.consumer.ConsumerConfig;
import kafka.consumer.SimpleConsumer;
import kafka.utils.ZkUtils;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.exception.ZkMarshallingError;
import org.I0Itec.zkclient.serialize.ZkSerializer;
import org.apache.commons.codec.binary.StringUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import scala.Predef;
import scala.collection.JavaConverters;
import scala.collection.Seq;

import java.lang.reflect.Method;
import java.util.*;


public class KafkaProduceOffsetFetcher {

    private ZkClient zkClient;
    private Map<Broker, SimpleConsumer> consumerMap = Maps.newHashMap();
    private Map<Integer, Broker> brokerIndex = Maps.newHashMap();
    private static final int SESSION_TIMEOUT = 5000;
    private static final int CONNECTION_TIMEOUT = 5000;

    public KafkaProduceOffsetFetcher(String zkHost) {
        this.zkClient = new ZkClient(zkHost, SESSION_TIMEOUT, CONNECTION_TIMEOUT, new ZkSerializer() {
            @Override
            public byte[] serialize(Object o) throws ZkMarshallingError {
                return ((String) o).getBytes();
            }

            @Override
            public Object deserialize(byte[] bytes) throws ZkMarshallingError {
                if (bytes == null) {
                    return null;
                } else {
                    return StringUtils.newStringUtf8(bytes);
                }
            }
        });
    }

    public Map<Integer, Map<String, List<Integer>>> getMetadata(Set<String> topics) {
        if (CollectionUtils.isEmpty(topics)) {
            return Collections.emptyMap();
        }
        Seq<Broker> brokers = ZkUtils.getAllBrokersInCluster(zkClient);
        for (Broker broker : JavaConverters.asJavaListConverter(brokers).asJava()) {
            brokerIndex.put(broker.id(), broker);
        }
        TopicMetadataResponse response = ClientUtils.fetchTopicMetadata(JavaConverters.asScalaSetConverter(topics).asScala(), brokers, "GetMetadataClient", 10000, 0);
        return parseMetadataResponse(response);
    }

    private Map<Integer, Map<String, List<Integer>>> parseMetadataResponse(TopicMetadataResponse response) {
        Map<Integer/*broker id*/, Map<String/*topic*/, List<Integer>/*partition id*/>> metadata = Maps.newHashMap();
        Seq<TopicMetadata> topicMetadatas = response.topicsMetadata();
        for (TopicMetadata topicMetadata : JavaConverters.asJavaListConverter(topicMetadatas).asJava()) {
            List<PartitionMetadata> partitionsMetadata = JavaConverters.asJavaListConverter(topicMetadata.partitionsMetadata()).asJava();
            String topic = topicMetadata.topic();
            for (PartitionMetadata partitionMetadata : partitionsMetadata) {
                int partitionId = partitionMetadata.partitionId();
                int brokerId = partitionMetadata.leader().get().id();
                if (!metadata.containsKey(brokerId)) {
                    metadata.put(brokerId, Maps.newHashMap());
                }
                if (!metadata.get(brokerId).containsKey(topic)) {
                    metadata.get(brokerId).put(topic, Lists.newArrayList());
                }
                metadata.get(brokerId).get(topic).add(partitionId);
            }
        }
        return metadata;
    }

    @SuppressWarnings("unchecked")
    public Map<String, Map<Integer, Long>> getMaxOffset(Map<Integer, Map<String, List<Integer>>> metadata) {
        Map<String/*topic*/, Map<Integer/*partition id*/, Long/*offset*/>> ret = Maps.newHashMap();
        if (MapUtils.isNotEmpty(metadata)) {
            PartitionOffsetRequestInfo partitionOffsetRequestInfo = new PartitionOffsetRequestInfo(OffsetRequest.LatestTime(), 1);
            for (Map.Entry<Integer, Map<String, List<Integer>>> entry : metadata.entrySet()) {
                int bid = entry.getKey();
                SimpleConsumer consumer = getSimpleConsumer(bid);
                if (consumer != null) {
                    scala.collection.mutable.Map<TopicAndPartition, PartitionOffsetRequestInfo> map = new scala.collection.mutable.HashMap<>();
                    for (Map.Entry<String, List<Integer>> entry1 : entry.getValue().entrySet()) {
                        String topic = entry1.getKey();
                        for (Integer pid : entry1.getValue()) {
                            map.put(new TopicAndPartition(topic, pid), partitionOffsetRequestInfo);
                        }
                    }

                    scala.collection.immutable.Map<TopicAndPartition, PartitionOffsetRequestInfo> data = null;//map.toMap(Predef.conforms()); //this has error in IDEA 2016.3
                    try {
                        Method method = scala.collection.TraversableOnce.class.getDeclaredMethod("toMap", Predef.$less$colon$less.class);
                        Object obj = method.invoke(map, Predef.conforms());
                        data = (scala.collection.immutable.Map<TopicAndPartition, PartitionOffsetRequestInfo>) obj;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    OffsetRequest offsetRequest = new OffsetRequest(data, OffsetRequest.CurrentVersion(), 0, OffsetRequest.DefaultClientId(), Request.OrdinaryConsumerId());
                    OffsetResponse offsetResponse = consumer.getOffsetsBefore(offsetRequest);
                    scala.collection.immutable.Map<TopicAndPartition, PartitionOffsetsResponse> offsets = offsetResponse.partitionErrorAndOffsets();
                    for (Map.Entry<TopicAndPartition, PartitionOffsetsResponse> entry1 : JavaConverters.asJavaMapConverter(offsets).asJava().entrySet()) {
                        String topic = entry1.getKey().topic();
                        int partitionId = entry1.getKey().partition();
                        @SuppressWarnings("rawtypes")
                        Seq seq = entry1.getValue().offsets();
                        long offset = (Long) seq.head();
                        if (!ret.containsKey(topic)) {
                            ret.put(topic, Maps.newHashMap());
                        }
                        ret.get(topic).put(partitionId, offset);
                    }
                }
            }
        }
        return ret;
    }

    private SimpleConsumer getSimpleConsumer(int bid) {
        if (!brokerIndex.containsKey(bid)) {
            return null;
        }
        Broker broker = brokerIndex.get(bid);
        if (consumerMap.containsKey(broker)) {
            return consumerMap.get(broker);
        } else {
            SimpleConsumer consumer = new SimpleConsumer(broker.host(), broker.port(), ConsumerConfig.SocketTimeout(), ConsumerConfig.SocketBufferSize(), "ConsumerOffsetChecker");
            consumerMap.put(broker, consumer);
            return consumer;
        }
    }

    public Map<String, Long> getConsumeOffset(String group, String topic) throws Exception {
        Map<String/* qid */, Long/* consume offset */> ret = new HashMap<>();
        StringBuilder sbConsumeOffsetDir = new StringBuilder();
        sbConsumeOffsetDir.append(ZkUtils.ConsumersPath()).append("/").append(group).append("/offsets/").append(topic);
        String consumeOffsetDir = sbConsumeOffsetDir.toString();

        if (!ZkUtils.pathExists(zkClient, consumeOffsetDir)) {
            return ret;
        }

        for (String id : JavaConverters.asJavaListConverter(ZkUtils.getChildren(zkClient, consumeOffsetDir)).asJava()) {
            try {
                ret.put(id, Long.parseLong(ZkUtils.readData(zkClient, consumeOffsetDir + "/" + id)._1()));
            } catch (Exception e) {
                ret.put(id, -1L);
            }
        }

        return ret;
    }

    public void shutdown() {
        zkClient.close();
        if (MapUtils.isNotEmpty(consumerMap)) {
            consumerMap.values().forEach(SimpleConsumer::close);
        }
    }
}