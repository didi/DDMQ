package com.xiaojukeji.carrera.pproxy.kafka.server;

import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.protocol.Errors;
import org.apache.kafka.common.record.*;
import org.apache.kafka.common.requests.FetchResponse;
import sun.nio.ch.FileChannelImpl;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.LinkedHashMap;
import java.util.function.Consumer;

import static org.apache.kafka.common.record.RecordBatch.MAGIC_VALUE_V1;
import static org.apache.kafka.common.record.RecordBatch.MAGIC_VALUE_V2;

public class FetchAdapterService {

    long baseOffset = 0;//todo offset如果重复，client就过滤掉了。使用真实的offset

    public void fetch(Consumer<LinkedHashMap<TopicPartition, FetchResponse.PartitionData>> consumer) {
        LinkedHashMap<TopicPartition, FetchResponse.PartitionData> responseData = new LinkedHashMap();
        TopicPartition topicPartition = new TopicPartition( "TestTopicHehe", 0);
        //todo kafka消费时使用的FileRecoreds, Adapter需要采用MemoryRecords
        String body = "this is consumer body";
        ByteBuffer byteBuffer = ByteBuffer.allocate(4 + body.length());
        MemoryRecordsBuilder memoryRecordsBuilder = new MemoryRecordsBuilder(byteBuffer,MAGIC_VALUE_V2, CompressionType.NONE, TimestampType.LOG_APPEND_TIME,
                baseOffset,1,1, (short) 1,1,false,false,0,10);
        memoryRecordsBuilder.append(System.currentTimeMillis(),ByteBuffer.wrap("key".getBytes()), ByteBuffer.wrap(body.getBytes()));
        baseOffset++;
        memoryRecordsBuilder.append(System.currentTimeMillis(),ByteBuffer.wrap("key".getBytes()), ByteBuffer.wrap(body.getBytes()));
        baseOffset++;
        FetchResponse.PartitionData partitionData = new FetchResponse.PartitionData(Errors.NONE,1,-1,0,null,memoryRecordsBuilder.build());
        responseData.put(topicPartition, partitionData);
        consumer.accept(responseData);
    }

}
