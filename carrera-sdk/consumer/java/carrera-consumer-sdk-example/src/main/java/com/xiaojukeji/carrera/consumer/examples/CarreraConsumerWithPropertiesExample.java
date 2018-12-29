package com.xiaojukeji.carrera.consumer.examples;

import com.xiaojukeji.carrera.consumer.thrift.Context;
import com.xiaojukeji.carrera.consumer.thrift.Message;
import com.xiaojukeji.carrera.consumer.thrift.client.CarreraConfig;
import com.xiaojukeji.carrera.consumer.thrift.client.CarreraConsumer;
import com.xiaojukeji.carrera.consumer.thrift.client.MessageProcessor;


public class CarreraConsumerWithPropertiesExample {

    public static void main(String[] args) throws Exception {
        CarreraConfig config = CarreraConsumerExample.getConfig("cg_your_groupName", "127.0.0.1:9713");

        CarreraConsumer consumer = new CarreraConsumer(config);

        consumer.startConsume(new MessageProcessor() {
            @Override
            public Result process(Message message, Context context) {
                try {
                    System.out.println(String.format("msg=[%s], body=[%s], properties=[%s]", message.toString(),new String(message.getValue()),message.getProperties()));
                } catch (Exception e) {
                    System.out.println(e);
                }

                return Result.SUCCESS;
            }
        }, 2); // 每台server有2个线程，额外的一个随机分配。

    }
}