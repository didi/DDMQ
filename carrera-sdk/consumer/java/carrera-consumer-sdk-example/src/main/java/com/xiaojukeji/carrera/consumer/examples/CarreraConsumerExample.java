package com.xiaojukeji.carrera.consumer.examples;

import com.xiaojukeji.carrera.consumer.thrift.Context;
import com.xiaojukeji.carrera.consumer.thrift.Message;
import com.xiaojukeji.carrera.consumer.thrift.client.CarreraConfig;
import com.xiaojukeji.carrera.consumer.thrift.client.CarreraConsumer;
import com.xiaojukeji.carrera.consumer.thrift.client.MessageProcessor;
import org.apache.logging.log4j.LogManager;
import org.apache.thrift.transport.TTransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;


public class CarreraConsumerExample {
    private static final Logger LOGGER = LoggerFactory.getLogger(CarreraConsumerExample.class);

    public static void simpleExample(long consumeTime) throws InterruptedException {
        CarreraConfig config = getConfig("cg_your_groupName", "127.0.0.1:9713");
        final CarreraConsumer consumer = new CarreraConsumer(config);

        consumer.startConsume(new MessageProcessor() {
            @Override
            public Result process(Message message, Context context) {
                LOGGER.info("process key:{}, value.length:{}, offset:{}, context:{}", message.getKey(),
                        message.getValue().length, message.getOffset(), context);
                return Result.SUCCESS;
            }
        }, 5); // 每台server有2个线程，额外的一个随机分配。

        Thread.sleep(consumeTime); // consume for 10 seconds
        consumer.stop();
    }

    /**
     * 如果某些topic中消息流量特别大，可以使用独立的线程消费该topic。
     *
     * @param consumeTime 消费的时间
     * @throws InterruptedException
     */
    public static void exampleWithExtraThreadsForSomeTopic(long consumeTime) throws InterruptedException {
        CarreraConfig config = getConfig("cg_your_groupName", "127.0.0.1:9713");
        final CarreraConsumer consumer = new CarreraConsumer(config);
        Map<String/*Topic*/, Integer/*该topic额外的线程数*/> extraThreads = new HashMap<String, Integer>();
        extraThreads.put("test-0", 2);
        //该配置下有6个消费线程，有4个消费test-0和test-1的消息， 另外2个线程只消费test-0.
        consumer.startConsume(new MessageProcessor() {
            @Override
            public Result process(Message message, Context context) {
                LOGGER.info("process key:{}, value.length:{}, offset:{}, context:{}", message.getKey(),
                        message.getValue().length, message.getOffset(), context);
                return Result.SUCCESS;
            }
        }, 4, extraThreads);
        Thread.sleep(consumeTime);
        consumer.stop();
    }

    public static void exampleConsumeStatsFetch() throws InterruptedException {
        CarreraConfig config = new CarreraConfig("cg_your_groupName", "127.0.0.1:9713");
        final CarreraConsumer consumer = new CarreraConsumer(config);
        try {
            System.out.println(consumer.getConsumeStats());
            System.out.println(consumer.getConsumeStats("test_topic_name"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        consumer.stop();
    }

    public static void main(String[] args) throws TTransportException, InterruptedException {
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                LogManager.shutdown(); //shutdown log4j2.
            }
        }));

        LOGGER.info("start simpleExample...");
        simpleExample(60 * 1000);

        LOGGER.info("start exampleWithExtraThreadsForSomeTopic...");
        exampleWithExtraThreadsForSomeTopic(60 * 1000);

        exampleConsumeStatsFetch();
    }

    public static CarreraConfig getConfig(String groupName, String ipList) {
        CarreraConfig config = new CarreraConfig(groupName, ipList);
        config.setRetryInterval(500); // 拉取消息重试的间隔时间，单位ms。
        config.setMaxBatchSize(8);  //一次拉取的消息数量。
        return config;
    }
}