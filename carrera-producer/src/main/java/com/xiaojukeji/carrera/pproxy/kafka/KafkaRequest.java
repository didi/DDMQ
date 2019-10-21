package com.xiaojukeji.carrera.pproxy.kafka;

import com.xiaojukeji.carrera.thrift.Message;
import com.xiaojukeji.carrera.pproxy.producer.*;
import org.apache.rocketmq.client.producer.SendResult;
import java.util.function.Consumer;
public class KafkaRequest extends CarreraRequest implements Runnable{

    Consumer callable;
    SendResult sendResult;

    public KafkaRequest(ProducerPool producerPool, Message message, Consumer callable, int timeout) {
        super(producerPool, message, timeout,null);
        this.callable = callable;
    }

    @Override
    protected void sendResult(ProxySendResult result) {
        super.sendResult(result);
        callable.accept(new KafkaSendResult(result, sendResult));
    }

    @Override
    public void onSuccess(SendResult sendResult) {
        this.sendResult = sendResult;
        super.onSuccess(sendResult);
    }

    @Override
    public void run() {
        super.process();
    }

    public SendResult getSendResult() {
        return sendResult;
    }
}
