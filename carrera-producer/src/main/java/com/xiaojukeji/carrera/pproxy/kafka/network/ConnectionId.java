package com.xiaojukeji.carrera.pproxy.kafka.network;

public class ConnectionId {
    String id;
    String localHost;
    int localPort;
    String remoteHost;
    int remotePort;
    int connectionIndex;

    public ConnectionId(String localHost, int localPort, String remoteHost, int remotePort, int nextConnectionIndex) {
        this.localHost = localHost;
        this.localPort = localPort;
        this.remoteHost = remoteHost;
        this.remotePort = remotePort;
        this.connectionIndex = nextConnectionIndex;
        id = localHost + ":" + localPort + "-" + remoteHost + ":" + remotePort + "-" + nextConnectionIndex;
    }

    public static ConnectionId fromString(String connectionIdStr) {
        String[] connArray = connectionIdStr.split("-");
        Address localAddress = BrokerEndPoint.parseHostPort(connArray[0]);
        Address remoteAddress = BrokerEndPoint.parseHostPort(connArray[1]);
        int connectionIdIndex = Integer.valueOf(connArray[2]);
        return new ConnectionId(localAddress.host, localAddress.port, remoteAddress.host, remoteAddress.port, connectionIdIndex);
    }

    public String getId() {
        return id;
    }

    public String getLocalHost() {
        return localHost;
    }

    public int getLocalPort() {
        return localPort;
    }

    public String getRemoteHost() {
        return remoteHost;
    }

    public int getRemotePort() {
        return remotePort;
    }

    public int getConnectionIndex() {
        return connectionIndex;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setLocalHost(String localHost) {
        this.localHost = localHost;
    }

    public void setLocalPort(int localPort) {
        this.localPort = localPort;
    }

    public void setRemoteHost(String remoteHost) {
        this.remoteHost = remoteHost;
    }

    public void setRemotePort(int remotePort) {
        this.remotePort = remotePort;
    }

    public void setConnectionIndex(int connectionIndex) {
        this.connectionIndex = connectionIndex;
    }
}
