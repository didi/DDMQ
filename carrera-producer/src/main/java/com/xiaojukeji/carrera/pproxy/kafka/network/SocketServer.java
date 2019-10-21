package com.xiaojukeji.carrera.pproxy.kafka.network;

import com.xiaojukeji.carrera.pproxy.kafka.EndPoint;
import com.xiaojukeji.carrera.pproxy.kafka.KafkaConfig;
import com.xiaojukeji.carrera.pproxy.kafka.server.Acceptor;
import com.xiaojukeji.carrera.pproxy.kafka.server.ConnectionQuotas;
import com.xiaojukeji.carrera.pproxy.kafka.server.Processor;
import org.apache.kafka.common.network.ListenerName;
import org.apache.kafka.common.utils.KafkaThread;
import org.apache.kafka.common.utils.Time;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static com.xiaojukeji.carrera.pproxy.kafka.LoggerUtils.KafkaAdapterLog;
import static java.lang.String.format;
import static org.apache.kafka.common.security.auth.SecurityProtocol.PLAINTEXT;

public class SocketServer {

    private int maxQueuedRequests = 10000;
    private Map<Integer,Processor> processors = new ConcurrentHashMap();
    ConnectionQuotas connectionQuotas;
    KafkaConfig config;
    private Map<EndPoint,Acceptor> acceptors = new ConcurrentHashMap<>();
    private RequestChannel requestChannel = new RequestChannel(maxQueuedRequests);
    private Time time;

    public SocketServer(KafkaConfig kafkaConfig) {
        this.config = kafkaConfig;
        this.time = Time.SYSTEM;
    }

    public void startup() {
        synchronized (this) {
            connectionQuotas = new ConnectionQuotas();
            createAcceptorAndProcessors(config.getNumNetworkThreads());
            startProcessors();
        }
    }

    private void startProcessors() {
        for(Acceptor acceptor : acceptors.values()) {
            acceptor.startProcessors();
        }
        KafkaAdapterLog.info("started processors for {} acceptors",acceptors.size());
    }

    private void createAcceptorAndProcessors(int processorsPerListener) {
        Set<EndPoint> endPoints = new HashSet<>();//todo instead mock endPoint
        ListenerName listenerName = new ListenerName("PLAINTEXT");
        endPoints.add(new EndPoint(null, config.getPort(), listenerName, PLAINTEXT));
        int sendBufferSize = config.getSocketSendBufferBytes();
        int recvBufferSize = config.getSocketReceiveBufferBytes();
        for (EndPoint endPoint: endPoints) {
            Acceptor acceptor = new Acceptor(endPoint,sendBufferSize,recvBufferSize,connectionQuotas);
            addProcessors(acceptor, endPoint, processorsPerListener);
            KafkaThread.nonDaemon(format("kafka-socket-acceptor-%s-%s",listenerName,endPoint.getPort()), acceptor).start();
            acceptor.awaitStartup();
            this.acceptors.put(endPoint, acceptor);
        }

    }

    private void addProcessors(Acceptor acceptor, EndPoint endPoint, int newProcessorsPerListener) {
        ListenerName listenerName = endPoint.getListenerName();
        List<Processor> listenerProcessors = new ArrayList<>();
        for (int processorId=0; processorId < newProcessorsPerListener; processorId++) {
            Processor processor = new Processor(processorId, time, config.getSocketRequestMaxBytes(), requestChannel,
                    connectionQuotas, config.getConnectionsMaxIdlesMs(),listenerName, PLAINTEXT, config);
            listenerProcessors.add(processor);
            processors.put(processorId, processor);
            requestChannel.addProcessor(processor);
        }
        acceptor.addProcessors(listenerProcessors);
    }

    public RequestChannel getRequestChannel() {
        return requestChannel;
    }
}
