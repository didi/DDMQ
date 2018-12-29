package com.xiaojukeji.carrera.cproxy.actions;

import com.alibaba.fastjson.JSONObject;
import com.xiaojukeji.carrera.cproxy.consumer.UpstreamJob;


public interface Action {
    enum Status {
        FAIL, CONTINUE, FINISH, ASYNCHRONIZED
    }

    class UnsupportedDataType extends RuntimeException {
    }

    default Status act(UpstreamJob job) {
        Object data = job.getData();
        if (data instanceof byte[]) {
            return act(job, (byte[]) data);
        } else if (data instanceof JSONObject) {
            return act(job, (JSONObject) data);
        } else {
            throw new UnsupportedDataType();
        }
    }

    default Status act(UpstreamJob job, byte[] bytes) {
        throw new UnsupportedDataType();
    }

    default Status act(UpstreamJob job, JSONObject jsonObject) {
        throw new UnsupportedDataType();
    }

    default void shutdown() {
        // DO NOTHING BY DEFAULT
    }

    default void logMetrics() {
        // DO NOTHING BY DEFAULT
    }
}