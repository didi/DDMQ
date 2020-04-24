package com.didi.carrera.console.common.offset;

import com.google.common.collect.Maps;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.admin.ConsumeStats;
import org.apache.rocketmq.common.message.MessageQueue;
import org.apache.rocketmq.common.protocol.body.AllMaxOffset;
import org.apache.rocketmq.common.protocol.body.ClusterInfo;
import org.apache.rocketmq.common.protocol.route.BrokerData;
import org.apache.rocketmq.tools.admin.DefaultMQAdminExt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class RocketMQProduceOffsetFetcher {

    private static final Logger LOGGER = LoggerFactory.getLogger(RocketMQProduceOffsetFetcher.class);

    private DefaultMQAdminExt defaultMQAdminExt;

    public RocketMQProduceOffsetFetcher(String namesvrAddr) {
        this.defaultMQAdminExt = new DefaultMQAdminExt();
        defaultMQAdminExt.setNamesrvAddr(namesvrAddr);
        defaultMQAdminExt.setInstanceName("admin-" + System.currentTimeMillis());
    }

    public void start() throws MQClientException {
        defaultMQAdminExt.start();
    }

    public void shutdown() {
        defaultMQAdminExt.shutdown();
    }

    public Collection<BrokerData> getBrokers() {
        try {
            ClusterInfo clusterInfo = defaultMQAdminExt.examineBrokerClusterInfo();
            Map<String, BrokerData> brokerAddrTable = clusterInfo.getBrokerAddrTable();
            return brokerAddrTable.values();
        } catch (Exception e) {
            LOGGER.error("GetBrokers Exception", e);
        }
        return Collections.emptyList();
    }

    public Map<MessageQueue, Long> getMaxOffset(Collection<BrokerData> brokers) {
        HashMap<MessageQueue, Long> offsetTable = Maps.newHashMap();
        if (CollectionUtils.isNotEmpty(brokers)) {
            for (BrokerData broker : brokers) {
                try {
                    AllMaxOffset allMaxOffset = defaultMQAdminExt.getAllMaxOffset(broker.selectBrokerAddr());
                    for (Map.Entry<String, ConcurrentHashMap<Integer, Long>> entry : allMaxOffset.getOffsetTable().entrySet()) {
                        for (Map.Entry<Integer, Long> entry1 : entry.getValue().entrySet()) {
                            MessageQueue mq = new MessageQueue(entry.getKey(), broker.getBrokerName(), entry1.getKey());
                            offsetTable.put(mq, entry1.getValue());
                        }
                    }
                } catch (Exception e) {
                    LOGGER.error("GetMaxOffset Exception", e);
                }
            }
        }
        return offsetTable;
    }

    public ConsumeStats getConsumeStats(String group, String topic) throws Exception {
        return defaultMQAdminExt.examineConsumeStats(group, topic);
    }
}