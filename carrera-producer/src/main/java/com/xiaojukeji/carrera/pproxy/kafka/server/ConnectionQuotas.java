package com.xiaojukeji.carrera.pproxy.kafka.server;

import java.net.InetAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectionQuotas {//todo 连接统计
    private Map<InetAddress,Integer> counts = new ConcurrentHashMap();

    int maxConnectionsPerIp;
    int maxConnectionsPerIpOverrides;

    public void inc(InetAddress address) {
        synchronized (counts) {
            Integer count = counts.putIfAbsent(address, 1);
            counts.put(address,counts.get(address) + 1);
        }
    }

    public void dec(InetAddress address) {

    }

    public int get(InetAddress address) {
        return 1;
    }
}
