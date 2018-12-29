package com.xiaojukeji.carrera.producer;

import com.xiaojukeji.carrera.config.CarreraConfig;
import com.xiaojukeji.carrera.producer.tx.CancelTxMonitorMessageBuilder;
import com.xiaojukeji.carrera.producer.tx.TxBusinessMessageBuilder;
import com.xiaojukeji.carrera.producer.tx.AddTxMonitorMessageBuilder;
import com.xiaojukeji.carrera.thrift.DelayMeta;
import com.xiaojukeji.carrera.thrift.DelayResult;
import com.xiaojukeji.carrera.thrift.Message;
import com.xiaojukeji.carrera.thrift.Result;

import java.io.UnsupportedEncodingException;
import java.util.List;


public class CarreraProducer implements ProducerInterface {
    private ProducerInterface producer;
    private CarreraConfig config;

    public CarreraProducer(CarreraConfig config) {
        producer = new LocalCarreraProducer(config);
        this.config = config;
    }

    public static CarreraProducer newCarreraProducer(CarreraConfig config) throws Exception {
        return new CarreraProducer(config);
    }

    public MessageBuilder messageBuilder() {
        return new MessageBuilder(this);
    }

    public AddDelayMessageBuilder addDelayMessageBuilder() {
        return new AddDelayMessageBuilder(this);
    }

    public CancelDelayMessageBuilder cancelDelayMessageBuilder() {
        return new CancelDelayMessageBuilder(this);
    }

    public AddTxMonitorMessageBuilder addTxMonitorMessageBuilder(AddDelayMessageBuilder addDelayMessageBuilder) {
        return new AddTxMonitorMessageBuilder(addDelayMessageBuilder);
    }

    public CancelTxMonitorMessageBuilder cancelTxMonitorMessageBuilder(CancelDelayMessageBuilder cancelDelayMessageBuilder) {
        return new CancelTxMonitorMessageBuilder(cancelDelayMessageBuilder);
    }

    public TxBusinessMessageBuilder txBusinessMessageBuilder(MessageBuilder messageBuilder) {
        return new TxBusinessMessageBuilder(messageBuilder);
    }

    @Override
    public void start() throws Exception {
        producer.start();
    }

    @Override
    public void shutdown() {
        producer.shutdown();
    }

    @Override
    public Result sendMessage(Message message) {
        return producer.sendMessage(message);
    }

    @Override
    public Result send(String topic, byte[] body) {
        return producer.send(topic, body);
    }

    @Override
    public Result send(String topic, String body) {
        return producer.send(topic, body);
    }

    @Override
    public Result sendByCharset(String topic, String body, String charsetName) {
        return producer.send(topic, body, charsetName);
    }

    @Override
    public Result send(String topic, String body, String key, String... tags) {
        return producer.send(topic, body, key, tags);
    }

    @Override
    public Result send(String topic, byte[] body, String key, String... tags) {
        return producer.send(topic, body, key, tags);
    }

    @Override
    public Result sendByCharset(String topic, String body, String charsetName, String key, String... tags) {
        return producer.sendByCharset(topic, body, charsetName, key, tags);
    }

    @Override
    public Result sendWithHashId(String topic, long hashId, String body, String key, String... tags) {
        return producer.sendWithHashId(topic, hashId, body, key, tags);
    }

    @Override
    public Result sendWithHashId(String topic, long hashId, byte[] body, String key, String... tags) {
        return producer.sendWithHashId(topic, hashId, body, key, tags);
    }

    @Override
    public Result sendWithHashIdByCharset(String topic, long hashId, String body, String charsetName, String key, String[] tags) {
        return producer.sendWithHashIdByCharset(topic, hashId, body, charsetName, key, tags);
    }

    @Override
    public Result sendWithPartition(String topic, int partitionId, long hashId, byte[] body, String key, String... tags) {
        return producer.sendWithPartition(topic, partitionId, hashId, body, key, tags);
    }

    @Override
    public Result sendWithPartition(String topic, int partitionId, long hashId, String body, String key, String... tags) {
        return producer.sendWithPartition(topic, partitionId, hashId, body, key, tags);
    }

    @Override
    public Result sendWithPartitionByCharset(String topic, int partitionId, long hashId, String body, String charsetName, String key, String[] tags) {
        return producer.sendWithPartitionByCharset(topic, partitionId, hashId, body, charsetName, key, tags);
    }

    @Override
    public Result sendBatchConcurrently(List<Message> messages) {
        return producer.sendBatchConcurrently(messages);
    }

    @Override
    public Result sendBatchOrderly(List<Message> messages) {
        return producer.sendBatchOrderly(messages);
    }

    @Override
    public DelayResult sendDelay(String topic, byte[] body, DelayMeta delayMeta) {
        return producer.sendDelay(topic, body, delayMeta);
    }

    @Override
    public DelayResult sendDelay(String topic, String body, DelayMeta delayMeta) {
        return producer.sendDelay(topic, body, delayMeta);
    }

    @Override
    public DelayResult sendDelayByCharset(String topic, String body, DelayMeta delayMeta, String charsetName) {
        return producer.sendDelayByCharset(topic, body, delayMeta, charsetName);
    }

    @Override
    public DelayResult sendDelay(String topic, String body, DelayMeta delayMeta, String... tags) {
        return producer.sendDelay(topic, body, delayMeta, tags);
    }

    @Override
    public DelayResult sendDelay(String topic, byte[] body, DelayMeta delayMeta, String... tags) {
        return producer.sendDelay(topic, body, delayMeta, tags);
    }

    @Override
    public DelayResult sendDelayByCharset(String topic, String body, DelayMeta delayMeta, String charsetName, String... tags) {
        return producer.sendDelayByCharset(topic, body, delayMeta, charsetName, tags);
    }

    @Override
    public DelayResult cancelDelay(String topic, String uniqDelayMsgId) {
        return producer.cancelDelay(topic, uniqDelayMsgId);
    }

    @Override
    public DelayResult cancelDelay(String topic, String uniqDelayMsgId, String... tags) {
        return producer.cancelDelay(topic, uniqDelayMsgId, tags);
    }

    @Override
    public Result sendBatchSync(List<Message> messages) {
        return producer.sendBatchSync(messages);
    }

    public static Message buildMessage(String topic, int partitionId, long hashId, String body, String key, String... tags) {
        return buildMessage(topic, partitionId, hashId, body.getBytes(), key, tags);
    }

    public static Message buildMessageByCharset(String topic, int partitionId, long hashId, String body, String charsetName, String key, String... tags) throws UnsupportedEncodingException {
        return buildMessage(topic, partitionId, hashId, body.getBytes(charsetName), key, tags);
    }

    public static Message buildMessage(String topic, int partitionId, long hashId, byte[] body, String key, String... tags) {
        return CarreraProducerBase.buildMessage(topic, partitionId, hashId, body, key, tags);
    }

    public CarreraConfig getConfig() {
        return config.clone();
    }
}