package com.xiaojukeji.carrera.pproxy.kafka;

import com.xiaojukeji.carrera.thrift.Message;
import com.xiaojukeji.carrera.pproxy.producer.ProducerPool;
import com.xiaojukeji.carrera.pproxy.producer.ProxySendResult;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.protocol.Errors;
import org.apache.kafka.common.requests.ProduceResponse;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;

public class KafkaBatchRequestBuilder<T> {

    private ProducerPool producerPool;
    private List<KafkaRequest> kafkaRequestList = new ArrayList<>();
    private Consumer callable;
    Map<TopicPartition, ProduceResponse.PartitionResponse> response = new ConcurrentHashMap<>();
    private CountDownLatch latch;
    private ExecutorService executorService;
    private int timeout = 50;

    public KafkaBatchRequestBuilder(ExecutorService executorService) {
        this.executorService = executorService;
    }

    public void appendMessages(List<Message> messages) {
        latch = new CountDownLatch(messages.size());
        LoggerUtils.KafkaAdapterLog.debug("message size is {}", messages.size());
        messages.forEach(message -> {
           KafkaRequest request = new KafkaRequest(producerPool, message, (Consumer<KafkaSendResult>) sendResult -> {
                TopicPartition topicPartition = new TopicPartition(message.getTopic(), message.getPartitionId());//todo 同一topic，不同broker的queueId可能相同，需要做排序并转换。
                ProduceResponse.PartitionResponse partitionResponse;
                if (sendResult.getSendResult() != null && sendResult.getSendResult().getSendStatus() == SendStatus.SEND_OK) {
                    LoggerUtils.KafkaAdapterLog.info("send ok, offset:{}" , sendResult.getSendResult().getQueueOffset());
                    partitionResponse = new ProduceResponse.PartitionResponse(Errors.NONE,sendResult.getSendResult().getQueueOffset(),System.currentTimeMillis(),-1);//todo logStartOffset再处理
                } else {
                    LoggerUtils.KafkaAdapterLog.debug("send error:{}", sendResult.getProxySendResult().getResult());
                    partitionResponse = new ProduceResponse.PartitionResponse(Errors.RECORD_LIST_TOO_LARGE);//暂时没找到更合适的返回码
                }
                response.put(topicPartition, partitionResponse);
                latch.countDown();
                if (latch.getCount() == 0 ) {
                    LoggerUtils.KafkaAdapterLog.debug("send message finish , total size {}", messages.size());
                    callable.accept(response);
                }
            }, timeout);
            this.kafkaRequestList.add(request);
        });
    }

    public void sendAsync() {
        this.kafkaRequestList.forEach(request -> {
            executorService.execute(() -> request.run());
        });
    }

    public KafkaBatchRequestBuilder setProducerPool(ProducerPool producerPool) {
        this.producerPool = producerPool;
        return this;
    }

    public KafkaBatchRequestBuilder setCallback(Consumer<T> callable) {
        this.callable = callable;
        return this;
    }

    public KafkaBatchRequestBuilder setTimeout(int timeout) {
        this.timeout = timeout;
        return this;
    }


}
