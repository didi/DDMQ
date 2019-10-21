package com.xiaojukeji.carrera.pproxy.kafka.server;

import com.xiaojukeji.carrera.pproxy.kafka.AbstractServerThread;
import com.xiaojukeji.carrera.pproxy.kafka.KafkaConfig;
import com.xiaojukeji.carrera.pproxy.kafka.network.*;
import org.apache.kafka.common.metrics.Metrics;
import org.apache.kafka.common.network.*;
import org.apache.kafka.common.requests.RequestContext;
import org.apache.kafka.common.requests.RequestHeader;
import org.apache.kafka.common.security.auth.SecurityProtocol;
import org.apache.kafka.common.utils.LogContext;
import org.apache.kafka.common.utils.Time;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingDeque;

import static com.xiaojukeji.carrera.pproxy.kafka.LoggerUtils.KafkaAdapterLog;
import static java.lang.String.format;

public class Processor extends AbstractServerThread{
    private int id;
    private Queue<SocketChannel> newConnections = new ConcurrentLinkedQueue();
    private Map<String,Response> inflightResponses = new ConcurrentHashMap<>();
    private LinkedBlockingDeque<Response> responseQueue = new LinkedBlockingDeque<>();
    private int maxReqeustSize;
    private int connectionsMaxIdlesMs;
    private int failedAuthenticationDelayMs;
    private ListenerName listenerName;
    private SecurityProtocol securityProtocol;
    private KafkaConfig config;
    private Selector selector;
    private Time time;
    private RequestChannel requestChannel;
    private int nextConnectionIndex = 0;

    public Processor(int id,
                     Time time,
                     int maxReqeustSize,
                     RequestChannel requestChannel,
                     ConnectionQuotas connectionQuotas,
                     int connectionsMaxIdlesMs,
                     ListenerName listenerName,
                     SecurityProtocol securityProtocol,
                     KafkaConfig kafkaConfig) {
        super(connectionQuotas);
        this.id = id;
        this.time = time;
        this.maxReqeustSize = maxReqeustSize;
        this.connectionsMaxIdlesMs = connectionsMaxIdlesMs;
        this.failedAuthenticationDelayMs = 10000;
        this.listenerName = listenerName;
        this.securityProtocol = securityProtocol;
        this.config = kafkaConfig;
        this.requestChannel = requestChannel;
        this.selector = createSelector();
    }


    @Override
    public void run() {
        startupComplete();
        try {
            while (isRunning()) {
                try {
                    configureNewConnections();
                    processNewResponses();
                    poll();
                    processCompletedReceives();
                    processCompletedSends();
                    processDisconnected();
                } catch (Throwable e) {
                    KafkaAdapterLog.info("process exception ", e);
                }
            }
        } finally {

        }
    }

    private void configureNewConnections() {
        while (!newConnections.isEmpty()) {
            SocketChannel channel = newConnections.poll();
            try {
                selector.register(connectionId(channel.socket()), channel);
            } catch (Throwable e) {
                SocketAddress remoteAddress = channel.socket().getRemoteSocketAddress();
                close(channel);
                KafkaAdapterLog.error("Processor {} closed connection from {}", id , remoteAddress, e);
            }
        }
    }

    private void processNewResponses() {
        Response currentResponse = null;
        while ((currentResponse = dequeueResponse()) != null) {
            String channelId = currentResponse.getRequest().getRequestContext().connectionId;
            try {
                if (currentResponse instanceof NoOpResponse) {
                    //todo
                }
                if (currentResponse instanceof SendResponse) {
                    sendResponse(currentResponse, ((SendResponse) currentResponse).getSend());
                }
            } catch (Exception e) {
                processChannelException(channelId,format("Exception while processing response for %s", channelId), e);
            }
        }
    }

    private void sendResponse(Response response, Send responseSend) {
        String connectionId = response.getRequest().getRequestContext().connectionId;
        if (null != openOrClosingChannel(connectionId)) {
            selector.send(responseSend);
            inflightResponses.put(connectionId, response);
        }
    }

    private void poll() {
        try {
            selector.poll(300);
        } catch (IllegalStateException | IOException e) {
            KafkaAdapterLog.error("Processor {} poll failed",id ,e );
        }
    }

    private void processCompletedReceives() throws InterruptedException {
        for(NetworkReceive receive : selector.completedReceives()) {
            KafkaChannel channel = openOrClosingChannel(receive.source());
            if (null == channel) {
                throw new IllegalStateException(format("channel {} removed from selector before processing completed receive",receive.source()));
            }
            RequestHeader header = RequestHeader.parse(receive.payload());
            String connectionId = receive.source();
            RequestContext context = new RequestContext(header, connectionId, channel.socketAddress(), channel.principal(), listenerName, securityProtocol);
            Request request = new Request(id, context, time.nanoseconds(),receive.payload());
            requestChannel.sendReqeust(request);
            selector.mute(connectionId);
            handleChannelMuteEvent(connectionId, KafkaChannel.ChannelMuteEvent.REQUEST_RECEIVED);
        }
    }

    private void handleChannelMuteEvent(String connectionId, KafkaChannel.ChannelMuteEvent event) {
        openOrClosingChannel(connectionId).handleChannelMuteEvent(event);
    }

    private void processCompletedSends() {
        selector.completedSends().forEach(send -> {
            Response response = inflightResponses.remove(send.destination());
            if (null == response) {
                throw new IllegalStateException(format("Send for {} completed, but not in `inflightResponses`",send.destination()));
            }
            response.onComplete(send);
            handleChannelMuteEvent(send.destination(), KafkaChannel.ChannelMuteEvent.RESPONSE_SENT);
            tryUnmuteChannel(send.destination());
        });
    }

    private void tryUnmuteChannel(String connectionId) {
        KafkaChannel channel = openOrClosingChannel(connectionId);
        selector.unmute(channel.id());
    }

    private void processDisconnected() {
        selector.disconnected().keySet().forEach(connectionId -> {
            String remoteHost = ConnectionId.fromString(connectionId).getRemoteHost();
            inflightResponses.remove(connectionId);
            try {
                connectionQuotas.dec(InetAddress.getByName(remoteHost));
            } catch (UnknownHostException e) {
                KafkaAdapterLog.error("Exception while processing disconnection of connectionId {}",connectionId);
            }
        });
    }

    private String connectionId(Socket socket) {
        String localHost = socket.getLocalAddress().getHostAddress();
        int localPort = socket.getLocalPort();
        String remoteHost = socket.getInetAddress().getHostAddress();
        int remotePort = socket.getPort();
        ConnectionId connectionId = new ConnectionId(localHost, localPort, remoteHost, remotePort, nextConnectionIndex);
        return connectionId.getId();
    }

    private void processChannelException(String channelId, String errorMessage, Throwable throwable) {

    }

    public int getId() {
        return id;
    }

    private KafkaChannel openOrClosingChannel(String connectionId) {
        KafkaChannel kafkaChannel = null;
        kafkaChannel = selector.channel(connectionId);
        if (null == kafkaChannel) {
            kafkaChannel = selector.closingChannel(connectionId);
        }
        return kafkaChannel;
    }

    public void enqueueResponse(Response response){
        try {
            responseQueue.put(response);
        } catch (InterruptedException e) {
            throw new RuntimeException("put responseQueue exception", e);
        }
        wakeup();
    }

    private Response dequeueResponse() {
        Response response = responseQueue.poll();
        if (null != response) {
            response.getRequest().setResponseDequeueTimeNanos(Time.SYSTEM.nanoseconds());
        }
        return response;
    }

    public void accept(SocketChannel socketChannel) {
        newConnections.add(socketChannel);
        wakeup();
    }

    public void wakeup() {
        selector.wakeup();
    }

    private String getMetricTags() {
        return listenerName.value();
    }

    private Selector createSelector() {
        ChannelBuilder channelBuilder = ChannelBuilders.serverChannelBuilder(listenerName,
                false,
                securityProtocol,
                config,
                null,
                null);
        return new Selector(
                maxReqeustSize,
                failedAuthenticationDelayMs,
                new Metrics(),
                Time.SYSTEM,
                getMetricTags(),
                new HashMap<>(),
                false,
                channelBuilder,
                new LogContext("Processor-[" + id + "]")
                );
    }
}
