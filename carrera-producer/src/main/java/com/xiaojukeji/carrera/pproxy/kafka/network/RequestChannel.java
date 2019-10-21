package com.xiaojukeji.carrera.pproxy.kafka.network;

import com.xiaojukeji.carrera.pproxy.kafka.server.Processor;

import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import static com.xiaojukeji.carrera.pproxy.kafka.LoggerUtils.KafkaAdapterLog;

public class RequestChannel {

    private ArrayBlockingQueue<BaseRequest> requestQueue;
    private Map<Integer, Processor> processors = new ConcurrentHashMap();

    private int maxQueuedRequests;
    
    public RequestChannel(int queueSize) {
        requestQueue = new ArrayBlockingQueue<BaseRequest>(queueSize);
    }

    public void addProcessor(Processor processor) {
        if (processors.putIfAbsent(processor.getId(), processor) != null) {
            KafkaAdapterLog.warn("Unexpected processor with processorId {}", processor.getId());
        }
    }

    public void removeProcessor(int processorId) {
        processors.remove(processorId);
    }

    public void sendReqeust(Request request) throws InterruptedException {
        requestQueue.put(request);
    }

    public BaseRequest receiveRequest(long timeout) throws InterruptedException {
        return requestQueue.poll(timeout, TimeUnit.MILLISECONDS);
    }

    public BaseRequest receiveRequest() throws InterruptedException {
        return requestQueue.take();
    }

    public void sendResponse(Response response) {
        Processor processor = processors.get(response.getProcessor());
        if (null != processor) {
            processor.enqueueResponse(response);
        }
    }
}
