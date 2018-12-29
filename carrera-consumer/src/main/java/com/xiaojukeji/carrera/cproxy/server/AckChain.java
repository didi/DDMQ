package com.xiaojukeji.carrera.cproxy.server;

import com.xiaojukeji.carrera.thrift.consumer.AckResult;
import org.apache.thrift.async.AsyncMethodCallback;

import static org.slf4j.LoggerFactory.getLogger;


public class AckChain extends AbstractLowlevelRequestChain<AckResult, Boolean>{
    public static final org.slf4j.Logger LOGGER = getLogger(AckChain.class);

    public AckChain(AckResult request, AsyncMethodCallback<Boolean> resultHandler) {
        super(request.getGroupId(), request, true, resultHandler);
    }

    @Override
    public void doAction() {
        try {
            iter.next().ack(this);
        } catch (Exception e) {
            resultHandler.onError(e);
            LOGGER.error("[lowlevel] error while doNext in ackChain. group:{}, err.msg:{}.", getRequest().getGroupId(), e.getMessage(), e);
        }
    }

    @Override
    public void saveResponse(Boolean res) {
        if(!res) {
            response = false;
        }
    }
}