package com.xiaojukeji.carrera.pproxy.kafka.network;

import org.apache.kafka.common.network.Send;
import org.apache.kafka.common.utils.Time;

public abstract class Response {
    private Request request;
    private int processor;

    public Response(Request request) {
        this.request = request;
        long nowNs = Time.SYSTEM.nanoseconds();
        request.setResponseCompleteTimeNanos(nowNs);
        if (request.getApiLocalCompleteTimeNanos() == -1L) {
            request.setApiLocalCompleteTimeNanos(nowNs);
        }
        this.processor = request.getProcessor();
    }

    public int getProcessor() {
        return processor;
    }

    public Request getRequest() {
        return request;
    }

    public void setRequest(Request request) {
        this.request = request;
    }

    public abstract void onComplete(Send send);
}
