package com.xiaojukeji.carrera.monitor.broker;

import org.apache.commons.lang.StringUtils;
import org.apache.rocketmq.client.consumer.DefaultMQPullConsumer;
import org.apache.rocketmq.client.consumer.PullResult;
import org.apache.rocketmq.client.consumer.PullStatus;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.impl.consumer.DefaultMQPullConsumerImpl;
import org.apache.rocketmq.client.impl.factory.MQClientInstance;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.apache.rocketmq.common.constant.PermName;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageQueue;
import org.apache.rocketmq.common.protocol.route.BrokerData;
import org.apache.rocketmq.common.protocol.route.QueueData;
import org.apache.rocketmq.common.protocol.route.TopicRouteData;
import org.apache.rocketmq.remoting.exception.RemotingException;
import org.apache.rocketmq.tools.admin.DefaultMQAdminExt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.apache.rocketmq.client.producer.SendStatus.SLAVE_NOT_AVAILABLE;

public class Utils {
    private static final Logger logger = LoggerFactory.getLogger(Utils.class);

    private static final Map<String, DefaultMQAdminExt> mqAdminExtMap = new ConcurrentHashMap<>();
    private static final Map<String, DefaultMQPullConsumer> nameSvrCheckMap = new ConcurrentHashMap<>();
    private static final Map<String, DefaultMQPullConsumer> brokerReceiveCheckMap = new ConcurrentHashMap<>();
    private static final Map<String, DefaultMQProducer> brokerSendCheckMap = new ConcurrentHashMap<>();

    private static DefaultMQAdminExt getMQAdminExt(String addr, String cluster) {
        String key = Thread.currentThread().getName() + "_" + cluster + "_" + addr;
        return mqAdminExtMap.computeIfAbsent(key, s -> {
            try {
                DefaultMQAdminExt defaultMQAdminExt = new DefaultMQAdminExt(key.replaceAll(":", "_").replaceAll("\\.", "_"));
                defaultMQAdminExt.setNamesrvAddr(addr);
                defaultMQAdminExt.setVipChannelEnabled(false);
                defaultMQAdminExt.setInstanceName(String.valueOf(System.currentTimeMillis()));
                defaultMQAdminExt.start();
                return defaultMQAdminExt;
            } catch (MQClientException e) {
                logger.error("[DefaultMQAdminExt] start mqAdminExt error, nameServer:{}, cluster:{}", addr, cluster, e);
                throw new RuntimeException("[DefaultMQAdminExt] start mqAdminExt error, nameServer:" + addr);
            }
        });
    }

    private static DefaultMQPullConsumer getNameSvrCheckConsumer(String nameSvr, String cluster) {
        String key = cluster + "_" + nameSvr;
        return nameSvrCheckMap.computeIfAbsent(key, s -> getDefaultMQPullConsumer(nameSvr, cluster, key));
    }

    private static DefaultMQPullConsumer getReceiveCheckConsumer(String nameSvr, String cluster, String broker) {
        String key = cluster + "_" + nameSvr + "_" + broker;
        return brokerReceiveCheckMap.computeIfAbsent(key, s -> getDefaultMQPullConsumer(nameSvr, cluster, key));
    }

    private static DefaultMQProducer getSendCheckProducer(String nameSvr, String cluster, String broker) {
        String key = cluster + "_" + nameSvr + "_" + broker;
        return brokerSendCheckMap.computeIfAbsent(key, s -> {
            try {
                DefaultMQProducer producer = new DefaultMQProducer("monitor_pusher_" + key.replaceAll(":", "_").replaceAll("\\.", "_"));
                producer.setNamesrvAddr(nameSvr);
                producer.setVipChannelEnabled(false);
                producer.setInstanceName(String.valueOf(System.currentTimeMillis()));
                producer.start();
                return producer;
            } catch (MQClientException e) {
                logger.error("[DefaultMQProducer] start mqAdminExt error, nameServer:{}, cluster:{} broker:{}", nameSvr, cluster, broker, e);
                throw new RuntimeException("[DefaultMQProducer] start mqAdminExt error, nameServer:" + nameSvr);
            }
        });
    }

    private static DefaultMQPullConsumer getDefaultMQPullConsumer(String nameSvr, String cluster, String key) {
        try {
            DefaultMQPullConsumer consumer = new DefaultMQPullConsumer("monitor_consumer_" + key.replaceAll(":", "_").replaceAll("\\.", "_"));
            consumer.setInstanceName(String.valueOf(System.currentTimeMillis()));
            consumer.setNamesrvAddr(nameSvr);
            consumer.setVipChannelEnabled(false);
            consumer.start();
            return consumer;
        } catch (MQClientException e) {
            logger.error("[DefaultMQPullConsumer] start DefaultMQPullConsumer error, nameServer:{}, cluster:{}, key:{}", nameSvr, cluster, key, e);
            throw new RuntimeException("[DefaultMQPullConsumer] start DefaultMQPullConsumer error, nameServer:" + nameSvr);
        }
    }

    public static void checkNameSvr(String nameSvr, String cluster) throws MQClientException, InterruptedException {
        getNameSvrCheckConsumer(nameSvr, cluster).getDefaultMQPullConsumerImpl().fetchPublishMessageQueues("SELF_TEST_TOPIC");
    }

    /**
     * 检查消费
     *
     * @param nameSvr
     * @param address
     * @throws MQClientException
     * @throws NoSuchFieldException
     * @throws SecurityException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     * @throws InterruptedException
     * @throws RemotingException
     * @throws MQBrokerException
     */
    public static long checkReceive(String cluster, String nameSvr, String address)
            throws MQClientException, NoSuchFieldException, SecurityException, IllegalArgumentException,
            IllegalAccessException, InterruptedException, RemotingException, MQBrokerException {

        DefaultMQPullConsumer consumer = getReceiveCheckConsumer(nameSvr, cluster, address);
        Field f1 = DefaultMQPullConsumerImpl.class.getDeclaredField("mQClientFactory");
        f1.setAccessible(true);

        MQClientInstance instance = (MQClientInstance) f1.get(consumer.getDefaultMQPullConsumerImpl());

        Field f = MQClientInstance.class.getDeclaredField("brokerAddrTable");
        f.setAccessible(true);

        Field f2 = MQClientInstance.class.getDeclaredField("scheduledExecutorService");
        f2.setAccessible(true);

        ScheduledExecutorService service = (ScheduledExecutorService) f2.get(instance);
        service.shutdown();
        service.awaitTermination(1000, TimeUnit.SECONDS);

        ConcurrentHashMap<String, HashMap<Long, String>> map = (ConcurrentHashMap<String, HashMap<Long, String>>) f.get(instance);
        HashMap<Long, String> addresses = new HashMap<>();
        addresses.put(0L, address);
        map.put("rmqmonitor_" + address, addresses);

        MessageQueue queue = new MessageQueue("SELF_TEST_TOPIC", "rmqmonitor_" + address, 0);

        boolean pullOk = false;
        long maxOffset = -1;
        for (int i = 0; i < 2; ++i) {
            try {
                maxOffset = consumer.getDefaultMQPullConsumerImpl().maxOffset(queue);
                PullResult result = consumer.pull(queue, "*", maxOffset > 100 ? maxOffset - 10 : 0, 1);
                if (result.getPullStatus() == PullStatus.FOUND) {
                    pullOk = true;
                    break;
                } else if(result.getPullStatus() == PullStatus.NO_NEW_MSG) {
                    checkSend(cluster, nameSvr, address);
                    continue;
                }

                logger.warn("pull result failed, PullResult={}, cluster={}, namesvr={}, address={}", result, cluster, nameSvr, address);
            } catch (Throwable e) {
                logger.error("pull exception, cluster={}, namesvr={}, address={}", cluster, nameSvr, address, e);
            }
            Thread.sleep(1000);
        }
        if (!pullOk) {
            logger.error(String.format("[AlarmPullErr] cluster=%s, broker=%s", cluster, address));
        } else {
            logger.info("AlarmPullCheck cluster={}, broker={}", cluster, address);
        }
        return maxOffset;
    }

    public static boolean isBrokerTopicWritable(String cluster, String nameSvr, String address/* broker Address */) {

        try {
            DefaultMQAdminExt adminExt = getMQAdminExt(nameSvr, cluster);
            String brokerPermission = adminExt.getBrokerConfig(address).getProperty("brokerPermission");
            if (!PermName.isWriteable(Integer.parseInt(brokerPermission))) {
                logger.info("skip send because of broker brokerPermission={}. cluster={}, namesvr={}, address={}",
                        brokerPermission, cluster, nameSvr, address);
                return false;
            }
            TopicRouteData topicRouteInfo = adminExt.examineTopicRouteInfo("SELF_TEST_TOPIC");

            String brokerName = null;
            for (BrokerData brokerData : topicRouteInfo.getBrokerDatas()) {
                if (StringUtils.equals(brokerData.getBrokerAddrs().get(0L), address)) {
                    brokerName = brokerData.getBrokerName();
                    break;
                }
            }
            if (brokerName != null) {
                for (QueueData queueData : topicRouteInfo.getQueueDatas()) {
                    if (StringUtils.equals(queueData.getBrokerName(), brokerName)) {
                        if (queueData.getWriteQueueNums() == 0 || !PermName.isWriteable(queueData.getPerm())) {
                            logger.info("skip send because of queue.Permission={},queue.writeQNuum={}. cluster={}, namesvr={}, address={}",
                                    queueData.getPerm(), queueData.getWriteQueueNums(), cluster, nameSvr, address);
                            return false;
                        }
                        break;
                    }
                }
            }
        } catch (Exception e) {
            logger.error("exception in isBrokerTopicWritable, cluster={}, namesvr={}, address={}", cluster, nameSvr, address, e);
        }
        return true;
    }

    public static void checkSend(String cluster, String nameSvr, String address) throws MQClientException, NoSuchFieldException,
            SecurityException, InterruptedException, IllegalArgumentException, IllegalAccessException, UnsupportedEncodingException, MQBrokerException, RemotingException {

        if (!isBrokerTopicWritable(cluster, nameSvr, address)) {
            return;
        }

        DefaultMQProducer producer = getSendCheckProducer(nameSvr, cluster, address);
        MQClientInstance instance = producer.getDefaultMQProducerImpl().getmQClientFactory();
        Field f = MQClientInstance.class.getDeclaredField("brokerAddrTable");
        f.setAccessible(true);

        Field f2 = MQClientInstance.class.getDeclaredField("scheduledExecutorService");
        f2.setAccessible(true);

        ScheduledExecutorService service = (ScheduledExecutorService) f2.get(instance);
        service.shutdown();
        service.awaitTermination(1000, TimeUnit.SECONDS);

        ConcurrentHashMap<String, HashMap<Long, String>> map = (ConcurrentHashMap<String, HashMap<Long, String>>) f
                .get(instance);
        HashMap<Long, String> addresses = new HashMap<>();
        addresses.put(0L, address);
        map.put("rmqmonitor_" + address, addresses);

        MessageQueue queue = new MessageQueue("SELF_TEST_TOPIC", "rmqmonitor_" + address, 0);
        boolean sendOk = false;
        SendResult sendResult = null;
        for (int i = 0; i < 2; i++) {
            try {
                Message msg = new Message("SELF_TEST_TOPIC", // topic
                        "TagA", // tag
                        ("Hello RocketMQ " + i).getBytes()// body
                );
                sendResult = producer.send(msg, queue);
                if (sendResult.getSendStatus() == SendStatus.SEND_OK || sendResult.getSendStatus() == SLAVE_NOT_AVAILABLE) {
                    sendOk = true;
                    break;
                }

                logger.warn("send result failed, SendResult={}, cluster={}, namesvr={}, address={}", sendResult, cluster, nameSvr, address);
            } catch (Exception e) {
                logger.error("send exception, cluster={}, namesvr={}, address={}", cluster, nameSvr, address, e);
            }
            Thread.sleep(1000);
        }

        // 报警
        if (!sendOk) {
            logger.error(String.format("[AlarmSendErr] cluster=%s, broker=%s, result=%s", cluster, address, sendResult == null ? "null" : sendResult.toString()));
        } else {
            logger.info("AlarmSendCheck cluster={}, broker={}, result={}", cluster, address, sendResult.toString());
        }
    }
}
