package com.xiaojukeji.carrera.pproxy.producer;

import com.xiaojukeji.carrera.thrift.Message;
import org.apache.thrift.async.AsyncMethodCallback;

import java.util.concurrent.atomic.AtomicInteger;


public class BatchCarreraRequest extends CarreraRequest {

    private BatchCarreraRequest next = null;

    private AtomicInteger globalRequestCounter;

    public BatchCarreraRequest(ProducerPool producerPool, Message message, @SuppressWarnings("rawtypes") AsyncMethodCallback resultHandler, AtomicInteger globalRequestCounter) {
        super(producerPool, message, resultHandler);
        this.globalRequestCounter = globalRequestCounter;
    }

    @Override
    public synchronized void onFinish(ProxySendResult result) {
        int oldCounter = globalRequestCounter.decrementAndGet();

        if (result == ProxySendResult.OK && oldCounter > 0) {
            if (next != null) {
                next.process();
            }
        } else {
            int ov = globalRequestCounter.get();
            while (!globalRequestCounter.compareAndSet(ov, 0) && (ov = globalRequestCounter.get()) > 0) ;
            if (ov > 0) {
                oldCounter = 0;
            }
        }
        if (oldCounter == 0) {
            super.onFinish(result);
        }
    }

    public BatchCarreraRequest getNext() {
        return next;
    }

    public void setNext(BatchCarreraRequest next) {
        this.next = next;
    }

}