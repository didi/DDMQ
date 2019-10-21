package com.xiaojukeji.carrera.pproxy.kafka.server;

import com.xiaojukeji.carrera.pproxy.kafka.KafkaRequestHandler;
import com.xiaojukeji.carrera.pproxy.kafka.network.RequestChannel;
import org.apache.kafka.common.utils.KafkaThread;

import java.util.ArrayList;
import java.util.List;

public class KafkaRequestHandlerPool {

    private List<KafkaRequestHandler> runnables;
    private int numIoThreads;
    private RequestChannel requestChannel;
    private KafkaApis apis;

    public KafkaRequestHandlerPool(RequestChannel requestChannel, KafkaApis apis, int numIoThreads) {
        this.requestChannel = requestChannel;
        this.numIoThreads = numIoThreads;
        this.apis = apis;
        runnables = new ArrayList<>(numIoThreads);
        for (int i = 0; i < numIoThreads; i++) {
            createHandler(i);
        }
    }

    private void createHandler(int id) {
        runnables.add(new KafkaRequestHandler(id, numIoThreads, requestChannel,apis ));
        KafkaThread.daemon("kafka-request-handler-" + id, runnables.get(id)).start();
    }
}
