package com.xiaojukeji.carrera.pproxy.kafka;

import com.xiaojukeji.carrera.pproxy.kafka.server.ConnectionQuotas;
import org.slf4j.event.Level;

import java.nio.channels.SocketChannel;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class AbstractServerThread implements Runnable {

    public ConnectionQuotas connectionQuotas;

    public AbstractServerThread(ConnectionQuotas connectionQuotas) {
        this.connectionQuotas = connectionQuotas;
    }

    private CountDownLatch startupLatch = new CountDownLatch(1);

    private volatile CountDownLatch shutdownLatch = new CountDownLatch(0);

    private AtomicBoolean alive = new AtomicBoolean(true);

    public void wakeup() {
        throw new IllegalStateException("need implement wakeup");
    }

    public void shutdown() {
        if (alive.getAndSet(false)) {
            wakeup();
        }
        try {
            shutdownLatch.await();
        } catch (InterruptedException e) {
            throw new IllegalStateException("shutdown exception ", e);
        }
    }

    public void awaitStartup() {
        try {
            startupLatch.await();
        } catch (InterruptedException e) {
            throw new IllegalStateException("startupLatch exception",e);
        }
    }

    protected void startupComplete() {
        shutdownLatch = new CountDownLatch(1);
        startupLatch.countDown();
    }

    protected void shutdownComplete() {
        shutdownLatch.countDown();
    }

    protected boolean isRunning() {
        return alive.get();
    }

    public void close(SocketChannel channel) {
        if (null != channel) {
            connectionQuotas.dec(channel.socket().getInetAddress());
            CoreUtils.close(channel.socket(), Level.WARN);
        }
    }
}
