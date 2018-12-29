package com.xiaojukeji.carrera.producer;

import com.xiaojukeji.carrera.thrift.DelayResult;


public class CancelDelayMessageBuilder {
    private CarreraProducer producer = null;
    private String topic = null;
    private String uniqDelayMsgId = null;
    private String tags = null;

    public CancelDelayMessageBuilder(CarreraProducer producer) {
        this.producer = producer;
    }

    public CancelDelayMessageBuilder setTopic(String topic) {
        this.topic = topic;
        return this;
    }

    public CancelDelayMessageBuilder setUniqDelayMsgId(String uniqDelayMsgId) {
        this.uniqDelayMsgId = uniqDelayMsgId;
        return this;
    }

    public String getUniqDelayMsgId() {
        return uniqDelayMsgId;
    }

    public CancelDelayMessageBuilder setTags(String tag) {
        this.tags = tag;
        return this;
    }

    public DelayResult send() {
        if (this.tags == null)
            return this.producer.cancelDelay(this.topic, this.uniqDelayMsgId);

        return this.producer.cancelDelay(this.topic, this.uniqDelayMsgId, this.tags);
    }
}