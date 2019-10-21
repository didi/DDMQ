package com.xiaojukeji.carrera.pproxy.kafka;

public class KafkaAdapterException extends RuntimeException{

    public KafkaAdapterException() {
    }

    public KafkaAdapterException(String message) {
        super(message);
    }

    public KafkaAdapterException(String message, Throwable cause) {
        super(message, cause);
    }
}
