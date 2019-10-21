package com.xiaojukeji.carrera.pproxy.kafka;

public interface LifeCycle {

    /**
     * 启动服务
     */
    void start() throws LifeCycleException;

    /**
     * 关闭服务
     */
    void stop();
}
