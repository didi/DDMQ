package com.xiaojukeji.carrera.pproxy.kafka;

public class LifeCycleException extends RuntimeException {
    public LifeCycleException(String msg) {
        super(msg);
    }

    public LifeCycleException(String message, Throwable cause) {
        super(message, cause);
    }
}
