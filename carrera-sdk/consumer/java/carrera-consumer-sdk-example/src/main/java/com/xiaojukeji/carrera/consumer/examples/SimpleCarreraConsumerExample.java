package com.xiaojukeji.carrera.consumer.examples;

import com.xiaojukeji.carrera.consumer.thrift.ConsumeStats;
import com.xiaojukeji.carrera.consumer.thrift.Context;
import com.xiaojukeji.carrera.consumer.thrift.Message;
import com.xiaojukeji.carrera.consumer.thrift.client.CarreraConfig;
import com.xiaojukeji.carrera.consumer.thrift.client.MessageProcessor;
import com.xiaojukeji.carrera.consumer.thrift.client.SimpleCarreraConsumer;
import org.apache.logging.log4j.LogManager;
import org.apache.thrift.TException;
import org.apache.thrift.transport.TTransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;


public class SimpleCarreraConsumerExample {
    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleCarreraConsumerExample.class);

    public static void main(String[] args) throws TTransportException, InterruptedException {

        CarreraConfig config =
                new CarreraConfig("cg_test", "127.0.0.1:9713");
        config.setRetryInterval(1000);
        final SimpleCarreraConsumer consumer = new SimpleCarreraConsumer(config);

        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    LOGGER.info("stopping...");
                    consumer.stop();
                } finally {
                    LogManager.shutdown(); //shutdown log4j2.
                }
            }
        }));

        consumer.startConsume(new MessageProcessor() {
            @Override
            public Result process(Message message, Context context) {
                LOGGER.info("process key:{}, value.length:{}, offset:{}, context:{}", message.getKey(),
                        message.getValue().length, message.getOffset(), context);
                return Result.SUCCESS;
            }
        });

        try {
            List<ConsumeStats> consumeStatses = consumer.getConsumeStats("test_group_name");
            System.out.println(consumeStatses);

            consumeStatses = consumer.getConsumeStats("test_topic_name");
            System.out.println(consumeStatses);
        } catch (TException e) {
            e.printStackTrace();
        }
    }
}