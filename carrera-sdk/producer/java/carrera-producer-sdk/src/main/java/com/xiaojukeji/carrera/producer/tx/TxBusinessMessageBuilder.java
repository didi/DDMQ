package com.xiaojukeji.carrera.producer.tx;

import com.xiaojukeji.carrera.producer.MessageBuilder;
import com.xiaojukeji.carrera.thrift.Result;


public class TxBusinessMessageBuilder {

    private MessageBuilder messageBuilder;

    public TxBusinessMessageBuilder(MessageBuilder messageBuilder) {
        this.messageBuilder = messageBuilder;
    }

    public Result send() {
        return this.messageBuilder.send();
    }
}