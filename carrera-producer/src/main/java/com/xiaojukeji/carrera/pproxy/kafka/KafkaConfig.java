package com.xiaojukeji.carrera.pproxy.kafka;

import org.apache.kafka.common.config.AbstractConfig;
import org.apache.kafka.common.config.ConfigDef;

import java.util.HashMap;
import java.util.Map;

public class KafkaConfig extends AbstractConfig{
    private int numNetworkThreads = 10;
    private int numIoThreads = 1;
    private int socketSendBufferBytes = 102400;
    private int socketReceiveBufferBytes = 102400;
    private int socketRequestMaxBytes = 1024 * 1024 * 1024;
    private int connectionsMaxIdlesMs = 60000;
    private int port=9092;

    public KafkaConfig (){
        this(new ConfigDef(), new HashMap<>(), false);
    }

    public KafkaConfig(ConfigDef definition, Map<?, ?> originals, boolean doLog) {
        super(definition, originals, doLog);
    }

    public KafkaConfig(ConfigDef definition, Map<?, ?> originals) {
        super(definition, originals);
    }

    public int getNumNetworkThreads() {
        return numNetworkThreads;
    }

    public void setNumNetworkThreads(int numNetworkThreads) {
        this.numNetworkThreads = numNetworkThreads;
    }

    public int getSocketSendBufferBytes() {
        return socketSendBufferBytes;
    }

    public void setSocketSendBufferBytes(int socketSendBufferBytes) {
        this.socketSendBufferBytes = socketSendBufferBytes;
    }

    public int getSocketReceiveBufferBytes() {
        return socketReceiveBufferBytes;
    }

    public void setSocketReceiveBufferBytes(int socketReceiveBufferBytes) {
        this.socketReceiveBufferBytes = socketReceiveBufferBytes;
    }

    public int getSocketRequestMaxBytes() {
        return socketRequestMaxBytes;
    }

    public void setSocketRequestMaxBytes(int socketRequestMaxBytes) {
        this.socketRequestMaxBytes = socketRequestMaxBytes;
    }

    public int getConnectionsMaxIdlesMs() {
        return connectionsMaxIdlesMs;
    }

    public void setConnectionsMaxIdlesMs(int connectionsMaxIdlesMs) {
        this.connectionsMaxIdlesMs = connectionsMaxIdlesMs;
    }

    public int getNumIoThreads() {
        return numIoThreads;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
}
