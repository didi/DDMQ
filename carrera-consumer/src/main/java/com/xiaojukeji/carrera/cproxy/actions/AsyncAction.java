package com.xiaojukeji.carrera.cproxy.actions;

import com.xiaojukeji.carrera.cproxy.config.ConsumerGroupConfig;


public class AsyncAction extends OrderAction {

    AsyncAction(ConsumerGroupConfig config) {
        super(config);
        executor.startBackgroundThreads();
    }
}