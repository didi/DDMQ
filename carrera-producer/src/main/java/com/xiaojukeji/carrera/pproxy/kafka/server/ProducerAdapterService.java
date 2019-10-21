package com.xiaojukeji.carrera.pproxy.kafka.server;

import com.xiaojukeji.carrera.pproxy.server.ProducerAsyncServerImpl;
import com.xiaojukeji.carrera.thrift.Message;
import com.xiaojukeji.carrera.pproxy.kafka.KafkaBatchRequestBuilder;
import com.xiaojukeji.carrera.pproxy.producer.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.record.MemoryRecords;
import org.apache.kafka.common.record.Record;
import org.apache.kafka.common.utils.Utils;

import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Consumer;

import static com.xiaojukeji.carrera.pproxy.kafka.LoggerUtils.KafkaAdapterLog;

public class ProducerAdapterService <T> {

    ProducerPool producerPool;
    ProducerAsyncServerImpl serverImpl;
    ExecutorService executorService = new ThreadPoolExecutor(10,100, 60L,TimeUnit.SECONDS, new SynchronousQueue<Runnable>());//todo 超过尺寸阻塞,线程池可配置

    public ProducerAdapterService(ProducerPool producerPool) {
        this.producerPool = producerPool;
    }

    public void send(int timeout, short acks, Map<TopicPartition, MemoryRecords> recordsMap, Consumer<T> callable) {
        List<Message> messages = transMessageFromMemoryRecords(recordsMap);
        if (CollectionUtils.isEmpty(messages)) {
            throw new RuntimeException("message is null");
        }
        KafkaBatchRequestBuilder kafkaBatchRequestBuilder = new KafkaBatchRequestBuilder(executorService).setCallback(callable).setProducerPool(producerPool).setTimeout(timeout);
        kafkaBatchRequestBuilder.appendMessages(messages);
        kafkaBatchRequestBuilder.sendAsync();
    }

    private List<Message> transMessageFromMemoryRecords(Map<TopicPartition, MemoryRecords> recordsMap) {
        List<Message> messageList = new ArrayList<>();
        for (TopicPartition topicPartition: recordsMap.keySet()) {
            int partition = topicPartition.partition();
            String topic = topicPartition.topic();
            MemoryRecords memoryRecords = recordsMap.get(topicPartition);
            Iterator<Record> iterator =  memoryRecords.records().iterator();
            while (iterator.hasNext()) {
                Record record = iterator.next();
                ByteBuffer value = record.value();
                Message message = new Message();
                //todo key需要解析
//                if (record.key() != null ) {
//                    message.setKey(new String(record.key().array()));
//                }

                message.setBody(value);
                message.setTopic(topic);
                message.setPartitionId(partition);
                messageList.add(message);
            }
        }
        return messageList;
    }

}
