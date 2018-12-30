package com.didi.carrera.console.service.impl;

import com.didi.carrera.console.common.offset.RocketMQProduceOffsetFetcher;
import com.didi.carrera.console.dao.dict.IsDelete;
import com.didi.carrera.console.dao.dict.MqServerType;
import com.didi.carrera.console.dao.model.Cluster;
import com.didi.carrera.console.dao.model.ConsumeGroup;
import com.didi.carrera.console.dao.model.ConsumeSubscription;
import com.didi.carrera.console.dao.model.MqServer;
import com.didi.carrera.console.dao.model.Topic;
import com.didi.carrera.console.dao.model.TopicConf;
import com.didi.carrera.console.service.ClusterService;
import com.didi.carrera.console.service.ConsumeGroupService;
import com.didi.carrera.console.service.ConsumeSubscriptionService;
import com.didi.carrera.console.service.exception.MqException;
import com.didi.carrera.console.service.MqServerService;
import com.didi.carrera.console.service.OffsetManagerService;
import com.didi.carrera.console.service.RmqAdminService;
import com.didi.carrera.console.service.TopicConfService;
import com.didi.carrera.console.service.TopicService;
import com.didi.carrera.console.web.controller.bo.ConsumeSubscriptionOrderBo;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.rocketmq.client.exception.MQClientException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service("didiOffsetManagerServiceImpl")
public class OffsetManagerServiceImpl implements OffsetManagerService {
    private static final Logger LOGGER = LoggerFactory.getLogger(OffsetManagerServiceImpl.class);

    @Resource(name = "didiClusterServiceImpl")
    private ClusterService clusterService;

    @Resource(name = "didiConsumeGroupServiceImpl")
    private ConsumeGroupService consumeGroupService;

    @Resource(name = "didiTopicConfServiceImpl")
    private TopicConfService topicConfService;

    @Resource(name = "didiTopicServiceImpl")
    private TopicService topicService;

    @Resource(name = "didiMqServerServiceImpl")
    private MqServerService mqServerService;

    @Resource(name = "didiRmqAdminServiceImpl")
    private RmqAdminService rmqAdminService;

    @Resource(name = "didiConsumeSubscriptionServiceImpl")
    private ConsumeSubscriptionService consumeSubscriptionService;

    private ConcurrentMap<Long, RocketMQProduceOffsetFetcher> rocketMQProduceOffsetFetcherMap = new ConcurrentHashMap<>();

    @Override
    public void resetOffsetToLatest(Long clusterId, Long groupId, Long topicId) throws Exception {
        Cluster cluster = clusterService.findById(clusterId);
        if (cluster == null || cluster.getIsDelete() == IsDelete.YES.getIndex()) {
            throw new MqException(clusterId + " cluster not found");
        }

        ConsumeGroup group = consumeGroupService.findById(groupId);
        if (group == null || group.getIsDelete() == IsDelete.YES.getIndex()) {
            throw new MqException(groupId + " consumerGroup not found");
        }

        Topic topic = topicService.findById(topicId);
        if (topic == null || topic.getIsDelete() == IsDelete.YES.getIndex()) {
            throw new MqException(topicId + " Topic not found");
        }

        List<ConsumeSubscription> subList = consumeSubscriptionService.findByNotNullGroupClusterTopicId(groupId, clusterId, topicId);
        if (CollectionUtils.isEmpty(subList)) {
            throw new MqException("subscription not found");
        }

        ConsumeSubscription sub = subList.get(0);
        if (MapUtils.isNotEmpty(sub.getSubExtraParams()) && sub.getSubExtraParams().containsKey(ConsumeSubscriptionOrderBo.SUB_FLAG_EXTREA_PARAMS_MQ_CLUSTER)) {
            String tmpCluster = sub.getSubExtraParams().get(ConsumeSubscriptionOrderBo.SUB_FLAG_EXTREA_PARAMS_MQ_CLUSTER);
            String[] arr = tmpCluster.split(";");
            for (String _cluster : arr) {
                MqServer mqServer = mqServerService.findByName(_cluster);
                if (mqServer == null) {
                    throw new MqException("subId<" + sub.getId() + ">'s cluster info <" + _cluster + "> error");
                }

                if (mqServer.getType() == MqServerType.ROCKETMQ.getIndex()) {
                    rmqAdminService.resetOffsetToLatest(mqServer.getAddr(), group.getGroupName(), topic.getTopicName());
                }

            }
            return;
        }

        List<TopicConf> confList = topicConfService.findByTopicClusterId(topicId, clusterId);
        if (CollectionUtils.isEmpty(confList)) {
            throw new MqException("Topic<" + topicId + ">not found in cluster <" + clusterId + ">");
        }

        for (TopicConf conf : confList) {
            MqServer mqServer = mqServerService.findById(conf.getMqServerId());
            if (mqServer == null || mqServer.getIsDelete() == IsDelete.YES.getIndex()) {
                throw new MqException(conf.getMqServerId() + " MqServer not found");
            }

            if (mqServer.getType() == MqServerType.ROCKETMQ.getIndex()) {
                rmqAdminService.resetOffsetToLatest(mqServer.getAddr(), group.getGroupName(), topic.getTopicName());
            }
        }
    }

    @Override
    public void resetOffsetByTime(Long clusterId, Long groupId, Long topicId, Date time) throws Exception {
        Cluster cluster = clusterService.findById(clusterId);
        if (cluster == null || cluster.getIsDelete() == IsDelete.YES.getIndex()) {
            throw new MqException(clusterId + " cluster not found");
        }

        ConsumeGroup group = consumeGroupService.findById(groupId);
        if (group == null || group.getIsDelete() == IsDelete.YES.getIndex()) {
            throw new MqException(groupId + " consumerGroup not found");
        }

        Topic topic = topicService.findById(topicId);
        if (topic == null || topic.getIsDelete() == IsDelete.YES.getIndex()) {
            throw new MqException(topicId + " Topic not found");
        }

        List<TopicConf> confList = topicConfService.findByTopicClusterId(topicId, clusterId);
        if (CollectionUtils.isEmpty(confList)) {
            throw new MqException("Topic<" + topicId + ">not found in cluster<" + clusterId + ">");
        }

        List<ConsumeSubscription> subList = consumeSubscriptionService.findByNotNullGroupClusterTopicId(groupId, clusterId, topicId);
        if (CollectionUtils.isEmpty(subList)) {
            throw new MqException("subscription not found");
        }

        ConsumeSubscription sub = subList.get(0);
        if (MapUtils.isNotEmpty(sub.getSubExtraParams()) && sub.getSubExtraParams().containsKey(ConsumeSubscriptionOrderBo.SUB_FLAG_EXTREA_PARAMS_MQ_CLUSTER)) {
            String tmpCluster = sub.getSubExtraParams().get(ConsumeSubscriptionOrderBo.SUB_FLAG_EXTREA_PARAMS_MQ_CLUSTER);
            String[] arr = tmpCluster.split(";");
            for (String _cluster : arr) {
                MqServer mqServer = mqServerService.findByName(_cluster);
                if (mqServer == null) {
                    throw new MqException("subId<" + sub.getId() + ">'s cluster info <" + _cluster + ">error");
                }

                if (mqServer.getType() == MqServerType.ROCKETMQ.getIndex()) {
                    rmqAdminService.resetOffsetByTime(mqServer.getAddr(), group.getGroupName(), topic.getTopicName(), time);
                } else {
                    throw new MqException("Kafka do not support reset Offset by timestamp");
                }
            }
            return;
        }

        for (TopicConf conf : confList) {
            MqServer mqServer = mqServerService.findById(conf.getMqServerId());
            if (mqServer == null || mqServer.getIsDelete() == IsDelete.YES.getIndex()) {
                throw new MqException(conf.getMqServerId() + " MqServer not found");
            }

            if (mqServer.getType() == MqServerType.ROCKETMQ.getIndex()) {
                rmqAdminService.resetOffsetByTime(mqServer.getAddr(), group.getGroupName(), topic.getTopicName(), time);
            } else {
                throw new MqException("Kafka do not support reset Offset by timestamp");
            }
        }
    }

    @Override
    public void resetOffsetByOffset(Long clusterId, Long groupId, Long topicId, String qid, long offset) throws Exception {
        Cluster cluster = clusterService.findById(clusterId);
        if (cluster == null || cluster.getIsDelete() == IsDelete.YES.getIndex()) {
            throw new MqException(clusterId + " cluster not found");
        }

        ConsumeGroup group = consumeGroupService.findById(groupId);
        if (group == null || group.getIsDelete() == IsDelete.YES.getIndex()) {
            throw new MqException(groupId + " consumerGroup not found");
        }

        Topic topic = topicService.findById(topicId);
        if (topic == null || topic.getIsDelete() == IsDelete.YES.getIndex()) {
            throw new MqException(topicId + " Topic not found");
        }

        List<ConsumeSubscription> subList = consumeSubscriptionService.findByNotNullGroupClusterTopicId(groupId, clusterId, topicId);
        if (CollectionUtils.isEmpty(subList)) {
            throw new MqException("subscription not found");
        }

        ConsumeSubscription sub = subList.get(0);
        if (MapUtils.isNotEmpty(sub.getSubExtraParams()) && sub.getSubExtraParams().containsKey(ConsumeSubscriptionOrderBo.SUB_FLAG_EXTREA_PARAMS_MQ_CLUSTER)) {
            String tmpCluster = sub.getSubExtraParams().get(ConsumeSubscriptionOrderBo.SUB_FLAG_EXTREA_PARAMS_MQ_CLUSTER);
            String[] arr = tmpCluster.split(";");
            for (String _cluster : arr) {
                MqServer mqServer = mqServerService.findByName(_cluster);
                if (mqServer == null) {
                    throw new MqException("subId<" + sub.getId() + ">'s cluster info<" + _cluster + ">error");
                }

                if (mqServer.getType() == MqServerType.ROCKETMQ.getIndex()) {
                    throw new MqException("RocketMQ do not support reset by offset value");
                } else {

                }
            }
            return;
        }

        List<TopicConf> confList = topicConfService.findByTopicClusterId(topicId, clusterId);
        if (CollectionUtils.isEmpty(confList)) {
            throw new MqException("Topic<" + topicId + ">not found in cluster<" + clusterId + ">");
        }

        for (TopicConf conf : confList) {
            MqServer mqServer = mqServerService.findById(conf.getMqServerId());
            if (mqServer == null || mqServer.getIsDelete() == IsDelete.YES.getIndex()) {
                throw new MqException(conf.getMqServerId() + " MqServer not found");
            }

            if (mqServer.getType() == MqServerType.ROCKETMQ.getIndex()) {
                throw new MqException("RocketMQ do not support reset by offset value");
            } else {

            }
        }
    }

    @Override
    public Map<String, Map<String, Long>> getProduceOffset(Long clusterId, Long topicId) throws Exception {
        List<TopicConf> topicConfList = topicConfService.findByTopicClusterId(topicId, clusterId);
        if (CollectionUtils.isEmpty(topicConfList)) throw new MqException("unexist topic");

        return getProduceOffsetMap(topicConfList.get(0).getTopicName(), topicConfList.get(0));
    }

    @Override
    public Map<String, Map<String, Long>> getConsumeOffset(Long clusterId, Long groupId, Long topicId) throws Exception {
        List<TopicConf> topicConfList = topicConfService.findByTopicClusterId(topicId, clusterId);
        if (CollectionUtils.isEmpty(topicConfList)) throw new MqException("unexist topic");

        ConsumeGroup group = consumeGroupService.findById(groupId);
        if (group == null) {
            throw new MqException("unexist groupId");
        }

        return getConsumeOffsetMap(group.getGroupName(), topicConfList.get(0).getTopicName(), topicConfList);
    }

    private Map<String/*rmq/kafka*/, Map<String/*qid*/, Long>> getProduceOffsetMap(String topic, TopicConf topicConf) throws MqException {
        Map<String, Map<String, Long>> returnMap = new HashMap<>();

        MqServer mqServer = mqServerService.findById(topicConf.getMqServerId());
        if (mqServer == null) {
            throw new MqException("unexist mqserver");
        }

        if (mqServer.getType() == MqServerType.ROCKETMQ.getIndex()) {
            RocketMQProduceOffsetFetcher fetcher = getRocketMQProduceOffsetFetcher(mqServer);

            Map<String, Long> maxOffsets = new HashMap<>();
            fetcher.getMaxOffset(fetcher.getBrokers()).entrySet().stream().filter(entry -> entry.getKey().getTopic().equals(topic))
                    .forEach(entry -> maxOffsets.put(RmqAdminServiceImpl.getNewQid(entry.getKey()), entry.getValue()));
            returnMap.put(MqServerType.ROCKETMQ.getName(), maxOffsets);
        } else {

        }
        return returnMap;
    }

    private Map<String, Map<String, Long>> getConsumeOffsetMap(String group, String topic, List<TopicConf> topicConfList) throws Exception {
        Map<String, Map<String, Long>> returnMap = new HashMap<>();

        for (TopicConf topicConf : topicConfList) {

            MqServer mqServer = mqServerService.findById(topicConf.getMqServerId());
            if (mqServer == null) throw new MqException("unexist mqserver");

            if (mqServer.getType() == MqServerType.ROCKETMQ.getIndex()) {
                RocketMQProduceOffsetFetcher fetcher = getRocketMQProduceOffsetFetcher(mqServer);

                Map<String, Long> consumeOffsets = new HashMap<>();
                fetcher.getConsumeStats(group, topic).getOffsetTable().forEach((mq, offset) -> consumeOffsets.put(RmqAdminServiceImpl.getNewQid(mq), offset.getConsumerOffset()));
                returnMap.put(MqServerType.ROCKETMQ.getName(), consumeOffsets);
            } else {

            }
        }
        return returnMap;
    }

    private RocketMQProduceOffsetFetcher getRocketMQProduceOffsetFetcher(MqServer mqServer) {
        return rocketMQProduceOffsetFetcherMap.computeIfAbsent(mqServer.getId(), mqserverId -> {
            try {
                RocketMQProduceOffsetFetcher fetcher = new RocketMQProduceOffsetFetcher(mqServer.getAddr());
                fetcher.start();
                return fetcher;
            } catch (MQClientException e) {
                LOGGER.error("start rocketmq produce fetcher error, mqserverId:" + mqserverId, e);
                throw new RuntimeException("start RocketMQProduceOffsetFetcher error, name=" + mqServer.getName() + ",nameServer:" + mqServer.getAddr());
            }
        });
    }

    @PreDestroy
    public void destory() {
        LOGGER.info("OffsetManagerServiceImpl shutdown start");
        rocketMQProduceOffsetFetcherMap.forEach((k, v) -> v.shutdown());
        LOGGER.info("OffsetManagerServiceImpl shutdown end");
    }

}