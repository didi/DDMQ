package com.xiaojukeji.carrera.cproxy.server;

import com.xiaojukeji.carrera.thrift.consumer.Context;
import com.xiaojukeji.carrera.thrift.consumer.Message;
import com.xiaojukeji.carrera.thrift.consumer.PullRequest;
import com.xiaojukeji.carrera.cproxy.utils.TimeUtils;
import org.apache.thrift.async.AsyncMethodCallback;
import org.apache.thrift.server.ThriftContext;

import java.util.Collections;
import java.util.List;


public class DelayRequest {

    private final PullRequest request;
    private final AsyncMethodCallback resultHandler;

    private volatile boolean finished = false;
    private final Context context;
    private final ThriftContext thriftContext;
    private final long expiredTimestamp;

    public DelayRequest(PullRequest request, Context context, AsyncMethodCallback resultHandler, ThriftContext thriftContext, long timeout) {
        this.request = request;
        this.context = context;
        this.resultHandler = resultHandler;
        this.thriftContext = thriftContext;
        this.expiredTimestamp = TimeUtils.getCurTime() + timeout;
    }

    public synchronized void timeout() {
        if (!finished) {
            finished = true;
            ConsumerServiceImpl.getInstance().setThriftContext(thriftContext);
            ConsumerServiceImpl.getInstance().responsePull(request, resultHandler, context, Collections.emptyList());
        }
    }

    public boolean isExpired() {
        return TimeUtils.getCurTime() > expiredTimestamp;
    }

    public boolean isFinished() {
        return finished;
    }

    public PullRequest getRequest() {
        return request;
    }

    public Context getContext() {
        return context;
    }

    public synchronized void response(List<Message> messages) {
        if (!finished) {
            finished = true;
            ConsumerServiceImpl.getInstance().setThriftContext(thriftContext);
            ConsumerServiceImpl.getInstance().responsePull(request, resultHandler, context, messages);
        }
    }
}