package com.xiaojukeji.carrera.consumer.thrift.client;

import com.xiaojukeji.carrera.consumer.thrift.Context;
import com.xiaojukeji.carrera.consumer.thrift.Message;


public interface MessageProcessor extends BaseMessageProcessor {
    Result process(Message message, Context context);
}