package com.xiaojukeji.carrera.pproxy.ratelimit;

import com.xiaojukeji.carrera.pproxy.producer.TopicConfigManager;


public interface IGroupRequestLimiter {

    boolean tryEnter(String group);

    void updateConfig(TopicConfigManager config);

    void updateNodeConfig(double warningRatio, int totalLimit);

    void shutdown();
}