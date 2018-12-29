package com.xiaojukeji.carrera.cproxy.consumer.handler;

import com.xiaojukeji.carrera.cproxy.consumer.CommonMessage;
import com.xiaojukeji.carrera.cproxy.consumer.ConsumeContext;
import com.xiaojukeji.carrera.cproxy.consumer.ResultCallBack;


public interface AsyncMessageHandler {

    void process(CommonMessage message, ConsumeContext context, ResultCallBack resultCallBack) throws InterruptedException;
}