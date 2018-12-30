package com.didi.carrera.console.service.impl;

import com.didi.carrera.console.common.util.CodecsUtils;
import com.didi.carrera.console.dao.dict.ClusterMqServerRelationType;
import com.didi.carrera.console.dao.dict.MqServerType;
import com.didi.carrera.console.dao.model.Cluster;
import com.didi.carrera.console.dao.model.ClusterMqserverRelation;
import com.didi.carrera.console.dao.model.MqServer;
import com.didi.carrera.console.service.ClusterMqserverRelationService;
import com.didi.carrera.console.service.ClusterService;
import com.didi.carrera.console.service.exception.MqException;
import com.didi.carrera.console.service.MqServerService;
import com.didi.carrera.console.service.RmqAdminService;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.didi.carrera.console.data.Message;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.consumer.DefaultMQPullConsumer;
import org.apache.rocketmq.client.consumer.PullResult;
import org.apache.rocketmq.client.consumer.PullStatus;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.MixAll;
import org.apache.rocketmq.common.TopicConfig;
import org.apache.rocketmq.common.admin.TopicStatsTable;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.common.message.MessageQueue;
import org.apache.rocketmq.common.protocol.ResponseCode;
import org.apache.rocketmq.common.protocol.body.ClusterInfo;
import org.apache.rocketmq.common.protocol.body.ConsumerConnection;
import org.apache.rocketmq.common.protocol.route.BrokerData;
import org.apache.rocketmq.common.protocol.route.QueueData;
import org.apache.rocketmq.common.protocol.route.TopicRouteData;
import org.apache.rocketmq.remoting.exception.RemotingException;
import org.apache.rocketmq.tools.admin.DefaultMQAdminExt;
import org.apache.rocketmq.tools.admin.MQAdminExt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;


@Service("didiRmqAdminServiceImpl")
public class RmqAdminServiceImpl implements RmqAdminService {
    private static final Logger LOGGER = LoggerFactory.getLogger(RmqAdminServiceImpl.class);

    private static final int DEFAULT_READ_QUEUE_NUMS = 8;
    private static final int DEFAULT_WRITE_QUEUE_NUMS = 8;

    private Map<String, DefaultMQAdminExt> mqAdminExtMap = new ConcurrentHashMap<>();
    private Map<String, DefaultMQPullConsumer> mqPullConsumerMap = new ConcurrentHashMap<>();

    private static final String CONSOLE_CONSUMER_GROUP = "CONSOLE_CONSUMER_GROUP";

    @Resource(name = "didiClusterServiceImpl")
    private ClusterService clusterService;

    @Resource(name = "didiMqServerServiceImpl")
    private MqServerService mqServerService;

    @Autowired
    private ClusterMqserverRelationService clusterMqserverRelationService;

    @Override
    public void createTopic(Long clusterId, String topic) throws Exception {
        createTopic(clusterId, topic, DEFAULT_READ_QUEUE_NUMS, DEFAULT_WRITE_QUEUE_NUMS);
    }

    private void createTopic(Long clusterId, String topic, int readQueueNums, int writeQueueNums) throws Exception {
        Cluster cluster = clusterService.findById(clusterId);
        if (cluster == null) {
            throw new MqException("[RMQ] cluster not found, clusterId=" + clusterId);
        }

        List<ClusterMqserverRelation> relationList = clusterMqserverRelationService.findByClusterId(clusterId, ClusterMqServerRelationType.P_PROXY);
        if (CollectionUtils.isEmpty(relationList)) {
            throw new MqException("[RMQ] cluster not bind to MqServer, clusterId=" + clusterId);
        }

        Map<Long, MqServer> mqServerMap = Maps.newHashMap();
        mqServerService.findAll().forEach(server -> mqServerMap.put(server.getId(), server));

        List<MqServer> bindRmqList = Lists.newArrayList();
        for (ClusterMqserverRelation relation : relationList) {
            if (!mqServerMap.containsKey(relation.getMqServerId())) {
                throw new MqException("[RMQ] MqServer not found, mqServerId=" + relation.getMqServerId());
            }
            if (mqServerMap.get(relation.getMqServerId()).getType() == MqServerType.ROCKETMQ.getIndex()) {
                if (StringUtils.isBlank(mqServerMap.get(relation.getMqServerId()).getAddr())) {
                    throw new MqException("[RMQ] MqServer NameServer info empty, mqServer=" + mqServerMap.get(relation.getMqServerId()).getName());
                }
                bindRmqList.add(mqServerMap.get(relation.getMqServerId()));
            }
        }

        if (bindRmqList.size() != 1) {
            List<String> mqServerNameList = bindRmqList.stream().map(MqServer::getName).collect(Collectors.toList());
            throw new MqException("[RMQ] " + cluster.getName() + "bind multiple RMQ<" + mqServerNameList + ">");
        }

        String rmqCluster = getClusterName(bindRmqList.get(0).getAddr());

        createTopic(rmqCluster, bindRmqList.get(0), topic, readQueueNums, writeQueueNums);
    }


    private Set<String> getMasterBrokerByBrokerName(String nameServer, String rmqClusterName) throws Exception {
        ClusterInfo clusterInfoSerializeWrapper = examineBrokerClusterInfo(nameServer);
        Set<String> brokerNameSet = clusterInfoSerializeWrapper.getClusterAddrTable().get(rmqClusterName);

        if (brokerNameSet == null) {
            throw new RuntimeException("Make sure the specified clusterName exists or the nameserver which connected is correct");
        }

        Set<String> masterSet = Sets.newHashSet();

        for (String brokerName : brokerNameSet) {
            BrokerData brokerData = clusterInfoSerializeWrapper.getBrokerAddrTable().get(brokerName);
            if (brokerData != null) {
                String addr = brokerData.getBrokerAddrs().get(MixAll.MASTER_ID);
                if (addr != null) {
                    masterSet.add(addr);
                }
            }
        }

        return masterSet;
    }

    private void createTopic(String rmqClusterName, MqServer mqServer, String topic, int readQueueNums, int writeQueueNums) throws MqException {
        DefaultMQAdminExt defaultMQAdminExt = getMQAdminExt(mqServer.getAddr());

        if (isTopicExist(topic, defaultMQAdminExt)) {
            LOGGER.info("rmq topic has exist, namesvr={}, topic={}", mqServer.getAddr(), topic);
            return;
        }

        try {
            Set<String> masterSet = getMasterBrokerByBrokerName(mqServer.getAddr(), rmqClusterName);

            TopicConfig topicConfig = new TopicConfig();
            topicConfig.setReadQueueNums(readQueueNums);
            topicConfig.setWriteQueueNums(writeQueueNums);
            topicConfig.setTopicName(topic);
            for (String addr : masterSet) {
                defaultMQAdminExt.createAndUpdateTopicConfig(addr, topicConfig);
            }
        } catch (Exception e) {
            LOGGER.error("[RMQ] createTopic exception, topic:" + topic + ", rmqClusterName:" + rmqClusterName, e);
            throw new MqException("[RMQ] create topic<" + topic + "> error:" + e.getMessage(), e);
        }
    }

    private boolean isTopicExist(String topic, DefaultMQAdminExt defaultMQAdminExt) throws MqException {
        try {
            TopicStatsTable topicStatsTable = defaultMQAdminExt.examineTopicStats(topic);
            if (topicStatsTable == null || MapUtils.isEmpty(topicStatsTable.getOffsetTable())) {
                return false;
            }
            return true;
        } catch (MQClientException e) {
            if (e.getResponseCode() == ResponseCode.TOPIC_NOT_EXIST) {
                return false;
            }
            throw new MqException("get topic status error, " + e.getMessage(), e);
        } catch (InterruptedException | RemotingException | MQBrokerException e) {
            throw new MqException("get topic status error, " + e.getMessage(), e);
        }
    }

    @Override
    public Message queryLatestMessage(String nameServer, String topic) throws Exception {
        DefaultMQAdminExt defaultMQAdminExt = getMQAdminExt(nameServer);

        TopicRouteData topicRouteData = defaultMQAdminExt.examineTopicRouteInfo(topic);
        if (topicRouteData == null) {
            throw new MqException("[RMQ] topic<" + topic + "> route info is null");
        }

        List<QueueData> queueDatas = topicRouteData.getQueueDatas();
        Collections.shuffle(queueDatas);
        for (QueueData queueData : queueDatas) {
            for (int i = 0; i < queueData.getReadQueueNums(); i++) {
                long maxOffsets = defaultMQAdminExt.maxOffset(new MessageQueue(topic, queueData.getBrokerName(), i));
                if (maxOffsets <= 0) {
                    continue;
                }
                Message msg = queryMessageByOffset(nameServer, topic, queueData.getBrokerName(), i, maxOffsets - 1);
                if (msg != null) {
                    return msg;
                }
            }
        }

        throw new MqException("[RMQ] fetch rmq latestmsg error, not exist");
    }

    private Message queryMessageByOffset(String nameServer, String topic, String brokerName, Integer qid, long offset) throws Exception {
        DefaultMQPullConsumer mqPullConsumer = getMqPullConsumer(nameServer);

        MessageQueue mq = new MessageQueue();
        mq.setTopic(topic);
        mq.setBrokerName(brokerName);
        mq.setQueueId(qid);

        PullResult pullResult = mqPullConsumer.pull(mq, "*", offset, 1, 5000);
        if (pullResult == null || pullResult.getPullStatus() != PullStatus.FOUND) {
            throw new MqException(String.format("[RMQ] message not exsit, nsrv:%s, topic:%s, brokerName:%s, qid:%s, offset:%d", nameServer, topic, brokerName, qid, offset));
        }

        MessageExt messageExt = pullResult.getMsgFoundList().get(0);
        if (messageExt.getBody().length == 0) {
            return null;
        }

        String msg;
        if (CodecsUtils.isUtf8(messageExt.getBody())) {
            msg = new String(messageExt.getBody(), "UTF-8");
        } else {
            msg = java.util.Base64.getEncoder().encodeToString(messageExt.getBody());
        }
        return new Message(brokerName + "_" + qid, offset, msg, messageExt.getTags(), messageExt.getKeys(), messageExt.getStoreSize(), messageExt.getBornTimestamp());
    }

    @Override
    public TopicStatsTable queryTopicConsumeState(String nameServer, String topic) throws Exception {
        DefaultMQAdminExt defaultMQAdminExt = getMQAdminExt(nameServer);
        return defaultMQAdminExt.examineTopicStats(topic);
    }


    @Override
    public void resetOffsetToLatest(String nameServer, String group, String topic) throws Exception {
        DefaultMQAdminExt defaultMQAdminExt = getMQAdminExt(nameServer);

        if (isRmqConsumerConnect(defaultMQAdminExt, group, topic)) {
            throw new RuntimeException(String.format("%s订阅%s的消费正在停止中，请稍后再试", group, topic));
        }

        defaultMQAdminExt.resetOffsetByTimestampOld(group, topic, -1, true);
    }

    private boolean isRmqConsumerConnect(MQAdminExt mqAdminExt, String group, String topic) {
        try {
            ConsumerConnection connection = mqAdminExt.examineConsumerConnectionInfo(group);
            if (connection == null || MapUtils.isEmpty(connection.getSubscriptionTable())) {
                return false;
            }
            return connection.getSubscriptionTable().containsKey(topic);
        } catch (Exception e) {
            LOGGER.error("mqAdminExt.examineConsumerConnectionInfo(" + group + ") exception", e);
            return false;
        }
    }

    @Override
    public void resetOffsetByTime(String nameServer, String group, String topic, Date date) throws Exception {
        DefaultMQAdminExt defaultMQAdminExt = getMQAdminExt(nameServer);
        if (isRmqConsumerConnect(defaultMQAdminExt, group, topic)) {
            throw new RuntimeException(String.format("%s订阅%s的消费正在停止中，请稍后再试", group, topic));
        }
        defaultMQAdminExt.resetOffsetByTimestampOld(group, topic, date.getTime(), true);
    }

    @Override
    public ClusterInfo examineBrokerClusterInfo(String nameServer) throws Exception {
        DefaultMQAdminExt defaultMQAdminExt = getMQAdminExt(nameServer);
        return defaultMQAdminExt.examineBrokerClusterInfo();
    }


    @Override
    public String getClusterName(String nameServer) throws Exception {
        ClusterInfo clusterInfo = examineBrokerClusterInfo(nameServer);

        String[] clusterNameArr = clusterInfo.retrieveAllClusterNames();
        if (ArrayUtils.isEmpty(clusterNameArr)) {
            throw new MqException(String.format("can't found cluster info, namesvr=%s", nameServer));
        }

        return clusterNameArr[0];
    }

    private DefaultMQAdminExt getMQAdminExt(String addr) {
        return mqAdminExtMap.computeIfAbsent(addr, s -> {
            try {
                DefaultMQAdminExt defaultMQAdminExt = new DefaultMQAdminExt();
                defaultMQAdminExt.setNamesrvAddr(addr);
                defaultMQAdminExt.setInstanceName(String.valueOf(System.currentTimeMillis()));
                defaultMQAdminExt.start();
                return defaultMQAdminExt;
            } catch (MQClientException e) {
                LOGGER.error("[RMQ] start mqadminext error, nameServer:" + addr, e);
                throw new RuntimeException("[RMQ] start mqAdminExt error, nameServer:" + addr);
            }
        });
    }

    private DefaultMQPullConsumer getMqPullConsumer(String addr) {
        return mqPullConsumerMap.computeIfAbsent(addr, s -> {
            try {
                DefaultMQPullConsumer mqPullConsumer = new DefaultMQPullConsumer(CONSOLE_CONSUMER_GROUP);
                mqPullConsumer.setNamesrvAddr(addr);
                mqPullConsumer.setInstanceName(String.valueOf(System.currentTimeMillis()));
                mqPullConsumer.start();
                return mqPullConsumer;
            } catch (MQClientException e) {
                LOGGER.error("start mqPullConsumer error, addr:" + addr, e);
                throw new RuntimeException("[RMQ] start mqPullConsumer error, nameServer:" + addr);
            }
        });
    }

    public static String getNewQid(MessageQueue mq) {
        return mq.getBrokerName() + "_" + mq.getQueueId();
    }

    @PreDestroy
    public void destory() {
        LOGGER.info("RmqAdminServiceImpl shutdown");
        mqAdminExtMap.values().forEach(DefaultMQAdminExt::shutdown);
        mqPullConsumerMap.values().forEach(DefaultMQPullConsumer::shutdown);
    }
}