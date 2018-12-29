package com.xiaojukeji.carrera.consumer.thrift.client;

import com.xiaojukeji.carrera.consumer.thrift.Context;
import com.xiaojukeji.carrera.consumer.thrift.Message;

import java.util.List;
import java.util.Map;


public interface BatchMessageProcessor extends BaseMessageProcessor {
    Map<MessageProcessor.Result, List<Long>> process(List<Message> messages, Context context);
}