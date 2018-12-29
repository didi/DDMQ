package com.xiaojukeji.carrera.monitor.lag.offset;


import org.apache.rocketmq.client.consumer.DefaultMQPullConsumer;
import org.apache.rocketmq.client.consumer.PullResult;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.MixAll;
import org.apache.rocketmq.common.admin.ConsumeStats;
import org.apache.rocketmq.common.admin.TopicStatsTable;
import org.apache.rocketmq.common.message.MessageQueue;
import org.apache.rocketmq.remoting.exception.RemotingException;
import org.apache.rocketmq.tools.admin.DefaultMQAdminExt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class RocketMQProduceOffsetFetcher {
    private static final Logger LOGGER = LoggerFactory.getLogger(RocketMQProduceOffsetFetcher.class);

    private DefaultMQAdminExt defaultMQAdminExt;

    private DefaultMQPullConsumer defaultMQPullConsumer;

    private String namesrvAddr;

    public RocketMQProduceOffsetFetcher(String namesrvAddr) {
        this.defaultMQAdminExt = new DefaultMQAdminExt();
        defaultMQAdminExt.setNamesrvAddr(namesrvAddr);
        defaultMQAdminExt.setInstanceName("admin-" + Long.toString(System.currentTimeMillis()));

        this.defaultMQPullConsumer = new DefaultMQPullConsumer(MixAll.TOOLS_CONSUMER_GROUP, null);
        defaultMQPullConsumer.setInstanceName("admin-" + Long.toString(System.currentTimeMillis()));
        defaultMQPullConsumer.setNamesrvAddr(namesrvAddr);
        this.namesrvAddr = namesrvAddr;
    }

    public String getNamesrvAddr() {
        return namesrvAddr;
    }

    public void start() throws MQClientException {
        defaultMQAdminExt.start();
        defaultMQPullConsumer.start();
        defaultMQPullConsumer.getDefaultMQPullConsumerImpl().getPullAPIWrapper().setConnectBrokerByUser(true);
    }

    public void shutdown() {
        defaultMQAdminExt.shutdown();
        defaultMQPullConsumer.shutdown();
    }

    public ConsumeStats getConsumeStats(String group, String topic) throws Exception {
        return defaultMQAdminExt.examineConsumeStats(group, topic);
    }

    public TopicStatsTable getProduceStats(String topic) throws InterruptedException, RemotingException, MQClientException, MQBrokerException {
        return defaultMQAdminExt.examineTopicStats(topic);
    }

    public PullResult queryMsgByOffset(MessageQueue mq, long offset) throws Exception {
        return defaultMQPullConsumer.pull(mq, "*", offset, 1);
    }
}
