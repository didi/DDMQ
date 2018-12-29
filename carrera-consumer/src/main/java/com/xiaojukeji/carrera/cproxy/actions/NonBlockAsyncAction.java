package com.xiaojukeji.carrera.cproxy.actions;

import com.xiaojukeji.carrera.cproxy.config.ConsumerGroupConfig;
import com.xiaojukeji.carrera.cproxy.utils.LogUtils;


public class NonBlockAsyncAction extends OrderAction {

    NonBlockAsyncAction(ConsumerGroupConfig config) {
        super(config);
        LogUtils.logMainInfo("NonBlockAsyncAction, group:{}", config.getGroupBrokerCluster());
    }
}