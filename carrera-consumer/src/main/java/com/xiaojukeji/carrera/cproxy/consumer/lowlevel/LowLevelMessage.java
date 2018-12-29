package com.xiaojukeji.carrera.cproxy.consumer.lowlevel;

import com.xiaojukeji.carrera.cproxy.consumer.CommonMessage;
import com.xiaojukeji.carrera.cproxy.consumer.ConsumeContext;


public class LowLevelMessage {

    private CommonMessage message;

    private ConsumeContext context;

    public LowLevelMessage(CommonMessage message, ConsumeContext context) {
        this.message = message;
        this.context = context;
    }

    public CommonMessage getMessage() {
        return message;
    }

    public ConsumeContext getContext() {
        return context;
    }
}