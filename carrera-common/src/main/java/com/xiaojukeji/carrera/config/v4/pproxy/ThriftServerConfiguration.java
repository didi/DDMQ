package com.xiaojukeji.carrera.config.v4.pproxy;

import com.xiaojukeji.carrera.config.ConfigurationValidator;


public class ThriftServerConfiguration implements ConfigurationValidator {
    private int selectorThreads;
    private int workerThreads;
    private int acceptQueueSizePerThread;
    private long maxReadBufferBytes;
    private int backlog;
    private int clientTimeout;
    private int workerQueueSize;
    private int timeoutCheckerThreads;
    private int port;

    public int getTimeoutCheckerThreads() {
        return timeoutCheckerThreads;
    }

    public void setTimeoutCheckerThreads(int timeoutCheckerThreads) {
        this.timeoutCheckerThreads = timeoutCheckerThreads;
    }

    public int getWorkerQueueSize() {
        return workerQueueSize;
    }

    public void setWorkerQueueSize(int workerQueueSize) {
        this.workerQueueSize = workerQueueSize;
    }

    public int getClientTimeout() {
        return clientTimeout;
    }

    public void setClientTimeout(int clientTimeout) {
        this.clientTimeout = clientTimeout;
    }

    public int getBacklog() {
        return backlog;
    }

    public void setBacklog(int backlog) {
        this.backlog = backlog;
    }

    public int getSelectorThreads() {
        return selectorThreads;
    }

    public void setSelectorThreads(int selectorThreads) {
        this.selectorThreads = selectorThreads;
    }

    public int getWorkerThreads() {
        return workerThreads;
    }

    public void setWorkerThreads(int workerThreads) {
        this.workerThreads = workerThreads;
    }

    public int getAcceptQueueSizePerThread() {
        return acceptQueueSizePerThread;
    }

    public void setAcceptQueueSizePerThread(int acceptQueueSizePerThread) {
        this.acceptQueueSizePerThread = acceptQueueSizePerThread;
    }

    public long getMaxReadBufferBytes() {
        return maxReadBufferBytes;
    }

    public void setMaxReadBufferBytes(long maxReadBufferBytes) {
        this.maxReadBufferBytes = maxReadBufferBytes;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    @Override
    public String toString() {
        return "ThriftServerConfiguration{" +
                "selectorThreads=" + selectorThreads +
                ", workerThreads=" + workerThreads +
                ", acceptQueueSizePerThread=" + acceptQueueSizePerThread +
                ", maxReadBufferBytes=" + maxReadBufferBytes +
                ", backlog=" + backlog +
                ", clientTimeout=" + clientTimeout +
                ", workerQueueSize=" + workerQueueSize +
                ", timeoutCheckerThreads=" + timeoutCheckerThreads +
                ", port=" + port +
                '}';
    }

    @Override
    public boolean validate() {
        return selectorThreads > 0
                && workerThreads >= 0
                && acceptQueueSizePerThread > 0
                && maxReadBufferBytes > 0
                && backlog > 0
                && clientTimeout >= 0
                && workerQueueSize >= 0
                && timeoutCheckerThreads > 0;

    }
}