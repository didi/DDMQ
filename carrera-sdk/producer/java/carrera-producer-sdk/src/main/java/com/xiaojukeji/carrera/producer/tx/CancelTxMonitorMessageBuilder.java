package com.xiaojukeji.carrera.producer.tx;

import com.xiaojukeji.carrera.producer.CancelDelayMessageBuilder;
import com.xiaojukeji.carrera.thrift.DelayResult;


public class CancelTxMonitorMessageBuilder {

    private CancelDelayMessageBuilder cancelDelayMessageBuilder;

    public CancelTxMonitorMessageBuilder(CancelDelayMessageBuilder cancelDelayMessageBuilder) {
        this.cancelDelayMessageBuilder = cancelDelayMessageBuilder;
    }

    public DelayResult send() {
        return this.cancelDelayMessageBuilder.send();
    }
}