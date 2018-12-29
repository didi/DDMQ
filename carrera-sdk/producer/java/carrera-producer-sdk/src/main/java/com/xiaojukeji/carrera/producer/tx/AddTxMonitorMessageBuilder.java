package com.xiaojukeji.carrera.producer.tx;

import com.xiaojukeji.carrera.producer.AddDelayMessageBuilder;
import com.xiaojukeji.carrera.thrift.DelayResult;


public class AddTxMonitorMessageBuilder {

    private AddDelayMessageBuilder addDelayMessageBuilder;

    public AddTxMonitorMessageBuilder(AddDelayMessageBuilder addDelayMessageBuilder) {
        this.addDelayMessageBuilder = addDelayMessageBuilder;
    }

    public DelayResult send() {
        return this.addDelayMessageBuilder.send();
    }
}