package com.xiaojukeji.carrera.pproxy.producer;


import com.xiaojukeji.carrera.pproxy.utils.LogUtils;
import com.xiaojukeji.carrera.pproxy.utils.RandomUtils;
import org.apache.rocketmq.client.Validators;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.impl.CommunicationMode;
import org.apache.rocketmq.client.impl.producer.TopicPublishInfo;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.MessageQueueSelector;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageQueue;
import org.apache.rocketmq.remoting.exception.RemotingException;


public class RmqSender {

    public static MessageQueueSelector RMQ_MQ_SELECTOR = (mqList, msg, arg) -> {
        CarreraMessage carreraMessage = (CarreraMessage) arg;

        MessageQueue messageQueue = null;

        if (carreraMessage.isRetry()) {
            ConfigManager configManager = ProducerPool.getInstance().getConfigManager();
            boolean strongOrder = configManager.getTopicConfigManager().isStrongOrder(carreraMessage.getTopic());
            //非依赖顺序topic
            if (!strongOrder) {
                int index = RandomUtils.nextInt(mqList.size());
                for (int i = 0; i < mqList.size(); i++) {
                    MessageQueue mq = mqList.get(index);
                    if (messageQueue == null) {
                        messageQueue = mq;
                    }
                    if (!mq.getBrokerName().equals(carreraMessage.lastBrokerName)) {
                        messageQueue = mq;
                        break;
                    }
                    index = (index + 1) % mqList.size();
                }
            }
        } else {
            int pid = carreraMessage.getPartitionId();
            if (pid >= 0) {
                if (pid >= mqList.size()) {
                    LogUtils.logError("RmqSender.MessageQueueSelector", String.format("PartitionId(%d) > Message queue number(%d)!. Msg=%s",
                            pid, mqList.size(), carreraMessage.toShortString()));
                }
                messageQueue = mqList.get(pid % mqList.size());
            } else if (pid == -1) { // use hashId, see producerProxy.thrift
                messageQueue = mqList.get((int) (Math.abs(carreraMessage.getHashId()) % mqList.size()));
            } else {
                messageQueue = RandomUtils.pick(mqList);
            }
        }

        carreraMessage.setLastBrokerName(messageQueue.getBrokerName());

        return messageQueue;
    };

    public static void send(DefaultMQProducer producer,
                            Message msg,
                            MessageQueue mq,
                            SendCallback sendCallback,
                            long timeout)
            throws MQBrokerException, MQClientException, RemotingException, InterruptedException {
        Validators.checkMessage(msg, producer);
        producer.getDefaultMQProducerImpl().sendKernelImpl(msg, mq, CommunicationMode.ASYNC, sendCallback, null, timeout);
    }

    public static void send(DefaultMQProducer producer,
                            CarreraRequest request,
                            SendCallback sendCallback,
                            long timeout)
            throws MQBrokerException, MQClientException, RemotingException, InterruptedException {
        MessageQueue mq = select(producer, request);
        send(producer, request.toRmqMessage(), mq, sendCallback, timeout);
    }

    public static MessageQueue select(DefaultMQProducer producer, CarreraMessage request) {
        TopicPublishInfo tpi = tryToFindTopicPublishInfo(producer, request.getTopic());
        return select(request, tpi);
    }

    public static MessageQueue select(CarreraMessage request, TopicPublishInfo tpi) {
        return RMQ_MQ_SELECTOR.select(tpi.getMessageQueueList(), null, request);
    }

    public static TopicPublishInfo tryToFindTopicPublishInfo(DefaultMQProducer producer, final String topic) {
        TopicPublishInfo topicPublishInfo = producer.getDefaultMQProducerImpl().getTopicPublishInfoTable().get(topic);

        if (null == topicPublishInfo || !topicPublishInfo.ok()) {
            producer.getDefaultMQProducerImpl().getmQClientFactory().updateTopicRouteInfoFromNameServer(topic);
            topicPublishInfo = producer.getDefaultMQProducerImpl().getTopicPublishInfoTable().get(topic);
        }

        if (topicPublishInfo != null && (topicPublishInfo.isHaveTopicRouterInfo() || topicPublishInfo.ok())) {
            return topicPublishInfo;
        } else {
            producer.getDefaultMQProducerImpl().getmQClientFactory()
                    .updateTopicRouteInfoFromNameServer(topic, true, producer);
            topicPublishInfo = producer.getDefaultMQProducerImpl().getTopicPublishInfoTable().get(topic);
            return topicPublishInfo;
        }
    }
}