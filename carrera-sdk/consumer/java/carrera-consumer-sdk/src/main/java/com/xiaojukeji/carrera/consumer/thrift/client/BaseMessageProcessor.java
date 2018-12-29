package com.xiaojukeji.carrera.consumer.thrift.client;


public interface BaseMessageProcessor {
    public enum Result {
        SUCCESS, // 表示消费成功，offset将会被持久化
        FAIL,    // 表示消费失败，按照服务端的失败处理逻辑决定是否重新消费
        SKIP     // 表示不将结果提交给服务端，按照服务端的超时处理逻辑决定是否重新消费
    }
}