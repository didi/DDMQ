package com.xiaojukeji.carrera.producer;

import com.xiaojukeji.carrera.config.CarreraConfig;
import com.xiaojukeji.carrera.thrift.Message;
import com.xiaojukeji.carrera.thrift.Result;
import com.xiaojukeji.carrera.utils.VersionUtils;
import com.xiaojukeji.carrera.thrift.producerProxyConstants;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;

import com.xiaojukeji.carrera.utils.ConstUtils;
import com.xiaojukeji.carrera.utils.RandomKeyUtils;


public class MessageBuilder {
    private CarreraProducer producer = null;
    private Message message = null;

    public MessageBuilder(CarreraProducer producer) {
        this.producer = producer;
        this.message = new Message();
        this.message.setVersion(VersionUtils.getVersion());
        this.message.setProperties(new HashMap<String, String>());
        this.message.partitionId = ConstUtils.PARTITION_RAND;
    }

    public MessageBuilder setTopic(String topic) {
        this.message.setTopic(topic);
        return this;
    }

    public MessageBuilder setPartitionId(int partitionId) {
        this.message.setPartitionId(partitionId);
        return this;
    }

    public MessageBuilder setRandomPartition() {
        this.message.setPartitionId(ConstUtils.PARTITION_RAND);
        return this;
    }

    public MessageBuilder setHashId(long hashId) {
        this.message.setPartitionId(ConstUtils.PARTITION_HASH);
        this.message.setHashId(hashId);
        return this;
    }

    public MessageBuilder setBody(byte[] body) {
        this.message.setBody(body);
        return this;
    }

    public MessageBuilder setBody(String body) {
        this.message.setBody(body.getBytes());
        return this;
    }

    public MessageBuilder setBody(String body, String charsetName) throws UnsupportedEncodingException {
        if (body != null && charsetName != null) {
            this.message.setBody(body.getBytes(charsetName));
        }

        return this;
    }

    public MessageBuilder setKey(String key) {
        if (key != null)
            this.message.setKey(key);
        return this;
    }

    public MessageBuilder setTags(String tag) {
        if (tag != null)
            this.message.setTags(tag);
        return this;
    }

    public MessageBuilder setTraceId(String traceId) {
        this.message.properties.put(producerProxyConstants.TRACE_ID, traceId);
        return this;
    }

    public MessageBuilder setSpanId(String spanId) {
        this.message.properties.put(producerProxyConstants.SPAN_ID, spanId);
        return this;
    }


    public MessageBuilder setPressureTraffic(boolean isOpen) {
        if (isOpen) {
            this.message.properties.put(producerProxyConstants.PRESSURE_TRAFFIC_KEY, producerProxyConstants.PRESSURE_TRAFFIC_ENABLE);
        } else {
            this.message.properties.put(producerProxyConstants.PRESSURE_TRAFFIC_KEY, producerProxyConstants.PRESSURE_TRAFFIC_DISABLE);
        }
        return this;
    }

    public MessageBuilder addProperty(String key, String value) {
        if (key != null && !key.equals(producerProxyConstants.PRESSURE_TRAFFIC_KEY)) {
            this.message.properties.put(key, value);
        }
        return this;
    }

    public Result send() {
        if (this.message.key == null) {
            this.setKey(RandomKeyUtils.randomKey(CarreraConfig.RANDOM_KEY_SIZE));
        }
        return this.producer.sendMessage(this.message);
    }
}