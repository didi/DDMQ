package com.xiaojukeji.carrera.pproxy.kafka;

import org.apache.kafka.common.network.ListenerName;
import org.apache.kafka.common.security.auth.SecurityProtocol;
import org.apache.kafka.common.utils.Utils;

public class EndPoint {

    String connectionString;
    ListenerName listenerName;
    String host;
    int port;


    public EndPoint(String host, int port, ListenerName listenerName, SecurityProtocol securityProtocol) {
        this.host = host;
        this.port = port;
        this.listenerName = listenerName;
        String hostport = null == host ? ":" + port : Utils.formatAddress(host, port);
        this.connectionString = listenerName.value() + "://" + hostport;
    }

    public int getPort() {
        return port;
    }

    public String getHost() {
        return host;
    }

    public ListenerName getListenerName() {
        return listenerName;
    }
}
