package com.xiaojukeji.carrera.pproxy.kafka;

import com.xiaojukeji.carrera.pproxy.kafka.network.BaseRequest;
import com.xiaojukeji.carrera.pproxy.kafka.network.Request;
import com.xiaojukeji.carrera.pproxy.kafka.network.RequestChannel;
import com.xiaojukeji.carrera.pproxy.kafka.server.KafkaApis;

import java.util.concurrent.CountDownLatch;

import static com.xiaojukeji.carrera.pproxy.kafka.LoggerUtils.KafkaAdapterLog;

public class KafkaRequestHandler implements Runnable{

    private volatile  boolean stopped = false;
    private CountDownLatch shutdownComplete = new CountDownLatch(1);
    private RequestChannel requestChannel;
    private KafkaApis apis;

    public KafkaRequestHandler(int id, int totalHandlerThreads, RequestChannel requestChannel, KafkaApis apis) {
        this.requestChannel = requestChannel;
        this.apis = apis;
    }

    @Override
    public void run() {
        while (!stopped) {
            try {
                BaseRequest request = requestChannel.receiveRequest(300);
                //todo shutdown request
                if (request instanceof Request) {
                   apis.handle((Request) request);
                }
            } catch (InterruptedException e) {
                KafkaAdapterLog.error("",e);
            }
        }
        shutdownComplete.countDown();
    }

    public void stop () {
        stopped = true;
    }

}
