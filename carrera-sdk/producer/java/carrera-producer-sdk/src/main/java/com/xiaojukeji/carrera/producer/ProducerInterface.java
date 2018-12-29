package com.xiaojukeji.carrera.producer;

import com.xiaojukeji.carrera.thrift.DelayMeta;
import com.xiaojukeji.carrera.thrift.DelayResult;
import com.xiaojukeji.carrera.thrift.Message;
import com.xiaojukeji.carrera.thrift.Result;

import java.util.List;


public interface ProducerInterface {

    void start() throws Exception;

    void shutdown();

    Result sendMessage(Message message);

    Result send(String topic, byte[] body);

    Result send(String topic, String body);

    Result sendByCharset(String topic, String body, String charsetName);

    Result send(String topic, String body, String key, String... tags);

    Result send(String topic, byte[] body, String key, String... tags);

    Result sendByCharset(String topic, String body, String charsetName, String key, String... tags);

    Result sendWithHashId(String topic, long hashId, String body, String key, String... tags);

    Result sendWithHashId(String topic, long hashId, byte[] body, String key, String... tags);

    Result sendWithHashIdByCharset(String topic, long hashId, String body, String charsetName, String key, String[] tags);

    Result sendWithPartition(String topic, int partitionId, long hashId, byte[] body, String key, String... tags);

    Result sendWithPartition(String topic, int partitionId, long hashId, String body, String key, String... tags);

    Result sendWithPartitionByCharset(String topic, int partitionId, long hashId, String body, String charsetName, String key, String[] tags);

    Result sendBatchConcurrently(List<Message> messages);

    Result sendBatchOrderly(List<Message> messages);

    DelayResult sendDelay(String topic, byte[] body, DelayMeta delayMeta);

    DelayResult sendDelay(String topic, String body, DelayMeta delayMeta);

    DelayResult sendDelayByCharset(String topic, String body, DelayMeta delayMeta, String charsetName);

    DelayResult sendDelay(String topic, String body, DelayMeta delayMeta, String... tags);

    DelayResult sendDelay(String topic, byte[] body, DelayMeta delayMeta, String... tags);

    DelayResult sendDelayByCharset(String topic, String body, DelayMeta delayMeta, String charsetName, String... tags);

    DelayResult cancelDelay(String topic, String uniqDelayMsgId);

    DelayResult cancelDelay(String topic, String uniqDelayMsgId, String... tags);

    Result sendBatchSync(List<Message> messages);
}