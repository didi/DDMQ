package com.xiaojukeji.carrera.pproxy.kafka.server;

import com.xiaojukeji.carrera.pproxy.kafka.AbstractServerThread;
import com.xiaojukeji.carrera.pproxy.kafka.CoreUtils;
import com.xiaojukeji.carrera.pproxy.kafka.EndPoint;
import com.xiaojukeji.carrera.pproxy.kafka.KafkaAdapterException;
import org.apache.kafka.common.network.Selectable;
import org.apache.kafka.common.utils.KafkaThread;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.xiaojukeji.carrera.pproxy.kafka.LoggerUtils.KafkaAdapterLog;
import static java.lang.String.format;

public class Acceptor extends AbstractServerThread {

    private Selector nioSelector;
    private ServerSocketChannel serverSocketChannel;
    private int sendBufferSize;
    private int recvBufferSize;
    private List<Processor> processors = new ArrayList<>();
    private AtomicBoolean processorsStarted = new AtomicBoolean();
    private EndPoint endPoint;

    public Acceptor(EndPoint endPoint, int sendBufferSize, int recvBufferSize, ConnectionQuotas connectionQuotas) {
        super(connectionQuotas);
        this.endPoint = endPoint;
        this.sendBufferSize = sendBufferSize;
        this.recvBufferSize = recvBufferSize;
        this.nioSelector = getSelector();
        this.serverSocketChannel = openServerSocket(endPoint.getHost(), endPoint.getPort());
    }

    @Override
    public void run() {
        try {
            serverSocketChannel.register(nioSelector, SelectionKey.OP_ACCEPT);
        } catch (ClosedChannelException e) {
            throw new KafkaAdapterException("serverSocketChannel register exception ",e);
        }
        startupComplete();
        try {
            int currentProcessor = 0;
            while (isRunning()) {
                try {
                    int ready = nioSelector.select(500);
                    if (ready <= 0) {
                        continue;
                    }
                    Iterator<SelectionKey> iterator = nioSelector.selectedKeys().iterator();
                    while (iterator.hasNext() && isRunning()) {
                        SelectionKey key = iterator.next();
                        iterator.remove();
                        if (!key.isAcceptable()) {
                            throw new IllegalStateException("Unrecognized key state for acceptor thread.");
                        }
                        Processor processor;
                        synchronized (this) {
                            currentProcessor = currentProcessor % processors.size();
                            processor = processors.get(currentProcessor);
                        }
                        accept(key, processor);
                        currentProcessor++;
                    }
                } catch (Throwable e) {
                    KafkaAdapterLog.error("Error while accepting connection", e);
                }
            }
        } finally {
            KafkaAdapterLog.debug("Closing server socket and selector.");
            CoreUtils.close(serverSocketChannel);
            CoreUtils.close(nioSelector);
            shutdownComplete();
        }

    }

    private void accept(SelectionKey key, Processor processor) throws IOException {
        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
        SocketChannel socketChannel = serverSocketChannel.accept();
        connectionQuotas.inc(socketChannel.socket().getInetAddress());
        socketChannel.configureBlocking(false);
        socketChannel.socket().setTcpNoDelay(true);
        socketChannel.socket().setKeepAlive(true);
        if (sendBufferSize != Selectable.USE_DEFAULT_BUFFER_SIZE) {
            socketChannel.socket().setSendBufferSize(sendBufferSize);
        }
        KafkaAdapterLog.debug(format("Accepted connection from %s on %s and assigned it to processor", socketChannel.socket().getRemoteSocketAddress(),
                socketChannel.socket().getLocalAddress(), processor.getId()));
        processor.accept(socketChannel);
    }

    private ServerSocketChannel openServerSocket(String host, int port){
        InetSocketAddress inetSocketAddress;
        if (null == host || host.trim().isEmpty()) {
            inetSocketAddress = new InetSocketAddress(port);
        } else {
            inetSocketAddress = new InetSocketAddress(host,port);
        }
        try {
            ServerSocketChannel serverChannel =ServerSocketChannel.open();
            serverChannel.configureBlocking(false);
            if (recvBufferSize != Selectable.USE_DEFAULT_BUFFER_SIZE ) {
                serverChannel.socket().setReceiveBufferSize(recvBufferSize);
            }
            serverChannel.socket().bind(inetSocketAddress);
            KafkaAdapterLog.info(format("Awaiting socket connections on %s:%d",inetSocketAddress.getHostString(), inetSocketAddress.getPort()));
            return serverChannel;
        } catch (Exception e) {
            throw new KafkaAdapterException("openServerSocket exception", e);
        }
    }

    public void startProcessors(){
        if (!processorsStarted.getAndSet(true)) {
            startProcessors(processors);
        }
    }

    private void startProcessors(List<Processor> processors) {
        for (Processor processor : processors) {
            KafkaThread.nonDaemon(format("kafka-network-thread-%s-%d",endPoint.getListenerName(),processor.getId()),processor).start();
        }
    }

    private Selector getSelector() {
        if (null == nioSelector) {
            synchronized (this) {
                if (null == nioSelector) {
                    try {
                        nioSelector = Selector.open();
                    } catch (IOException e) {
                        throw new IllegalStateException("Selector.open IOException ", e);
                    }
                }
            }
        }
        return nioSelector;
    }

    public void addProcessors(List<Processor> listenerProcessors) {
        this.processors.addAll(listenerProcessors);
    }
}
