package com.xiaojukeji.carrera.config.v4.cproxy;

import com.xiaojukeji.carrera.config.ConfigurationValidator;
import com.xiaojukeji.carrera.utils.PropertyUtils;


public class ConsumeServerConfiguration implements ConfigurationValidator, Cloneable {
    private int port = 9713;
    private int selectorThreads = 16;
    private int workerThreads = 128;
    private int acceptQueueSizePerThread = 100;
    private int maxReadBufferBytes = 5000000;
    private int backlog = 50;
    private int workerQueueSize = 20000;
    private int maxRecvMsgsLength = 1024 * 1024 * 4; //4M

    @Override
    public boolean validate() throws ConfigException {
        if (selectorThreads <= 0) {
            throw new ConfigException("[ConsumeServerConfig] selectorThreads <= 0");
        } else if (workerThreads <= 0) {
            throw new ConfigException("[ConsumeServerConfig] workerThreads <= 0");
        } else if (acceptQueueSizePerThread <= 0) {
            throw new ConfigException("[ConsumeServerConfig] acceptQueueSizePerThread <= 0");
        } else if (maxReadBufferBytes <= 0) {
            throw new ConfigException("[ConsumeServerConfig] maxReadBufferBytes <= 0");
        } else if (backlog <= 0) {
            throw new ConfigException("[ConsumeServerConfig] backlog <= 0");
        } else if (workerQueueSize <= 0) {
            throw new ConfigException("[ConsumeServerConfig] workerQueueSize <= 0");
        } else if (maxRecvMsgsLength <= 0) {
            throw new ConfigException("[ConsumeServerConfig] maxRecvMsgsLength <= 0");
        }
        return true;
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

    public int getMaxReadBufferBytes() {
        return maxReadBufferBytes;
    }

    public void setMaxReadBufferBytes(int maxReadBufferBytes) {
        this.maxReadBufferBytes = maxReadBufferBytes;
    }

    public int getBacklog() {
        return backlog;
    }

    public void setBacklog(int backlog) {
        this.backlog = backlog;
    }

    public int getWorkerQueueSize() {
        return workerQueueSize;
    }

    public void setWorkerQueueSize(int workerQueueSize) {
        this.workerQueueSize = workerQueueSize;
    }

    public int getMaxRecvMsgsLength() {
        return maxRecvMsgsLength;
    }

    public void setMaxRecvMsgsLength(int maxRecvMsgsLength) {
        this.maxRecvMsgsLength = maxRecvMsgsLength;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    @Override
    protected ConsumeServerConfiguration clone() {
        ConsumeServerConfiguration config = new ConsumeServerConfiguration();
        PropertyUtils.copyNonNullProperties(config, this);
        return config;
    }

    @Override
    public String toString() {
        return "ConsumeServerConfiguration{" +
                "port=" + port +
                ", selectorThreads=" + selectorThreads +
                ", workerThreads=" + workerThreads +
                ", acceptQueueSizePerThread=" + acceptQueueSizePerThread +
                ", maxReadBufferBytes=" + maxReadBufferBytes +
                ", backlog=" + backlog +
                ", workerQueueSize=" + workerQueueSize +
                ", maxRecvMsgsLength=" + maxRecvMsgsLength +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ConsumeServerConfiguration that = (ConsumeServerConfiguration) o;

        if (port != that.port) return false;
        if (selectorThreads != that.selectorThreads) return false;
        if (workerThreads != that.workerThreads) return false;
        if (acceptQueueSizePerThread != that.acceptQueueSizePerThread) return false;
        if (maxReadBufferBytes != that.maxReadBufferBytes) return false;
        if (backlog != that.backlog) return false;
        if (workerQueueSize != that.workerQueueSize) return false;
        return maxRecvMsgsLength == that.maxRecvMsgsLength;
    }

    @Override
    public int hashCode() {
        int result = port;
        result = 31 * result + selectorThreads;
        result = 31 * result + workerThreads;
        result = 31 * result + acceptQueueSizePerThread;
        result = 31 * result + maxReadBufferBytes;
        result = 31 * result + backlog;
        result = 31 * result + workerQueueSize;
        result = 31 * result + maxRecvMsgsLength;
        return result;
    }
}