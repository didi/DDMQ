package com.xiaojukeji.carrera.cproxy.server;

import com.xiaojukeji.carrera.thrift.consumer.ConsumeResult;
import com.xiaojukeji.carrera.thrift.consumer.PullRequest;
import com.xiaojukeji.carrera.thrift.consumer.PullResponse;
import com.xiaojukeji.carrera.cproxy.utils.TimeUtils;

import java.util.concurrent.atomic.AtomicInteger;


public class PullStats {
    volatile long lastStatTimeStamp = TimeUtils.getCurTime();
    public AtomicInteger requestCnt = new AtomicInteger();
    public AtomicInteger pulledMsg = new AtomicInteger();
    public AtomicInteger ackSuccess = new AtomicInteger();
    public AtomicInteger ackFail = new AtomicInteger();

    public void stat(PullRequest request, PullResponse response) {
        lastStatTimeStamp = TimeUtils.getCurTime();
        requestCnt.incrementAndGet();
        for (ConsumeResult r = request.getResult(); r != null; r = r.nextResult) {
            stat(r);
        }
        pulledMsg.addAndGet(response.getMessagesSize());
    }

    public void stat(ConsumeResult consumeResult) {
        lastStatTimeStamp = TimeUtils.getCurTime();
        if (consumeResult == null) return;
        ackSuccess.addAndGet(consumeResult.getSuccessOffsetsSize());
        ackFail.addAndGet(consumeResult.getFailOffsetsSize());
    }

    public boolean isNotEmpty() {
        return requestCnt.get() != 0
                || pulledMsg.get() != 0
                || ackSuccess.get() != 0
                || ackFail.get() != 0;
    }


    public void addAll(int req, int pulled, int success, int fail) {
        requestCnt.addAndGet(req);
        pulledMsg.addAndGet(pulled);
        ackSuccess.addAndGet(success);
        ackFail.addAndGet(fail);
    }
}