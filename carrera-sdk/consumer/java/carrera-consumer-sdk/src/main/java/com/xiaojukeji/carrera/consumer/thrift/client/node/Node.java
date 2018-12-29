package com.xiaojukeji.carrera.consumer.thrift.client.node;


public class Node {
    private String ip;
    private int port;

    public Node(String host) {
        if (!host.contains(":")) {
            throw new IllegalArgumentException("illegal host");
        }
        String[] ipAndPort = host.split(":");
        if (ipAndPort.length != 2) {
            throw new IllegalArgumentException("illegal host");
        }
        this.ip = ipAndPort[0].trim();
        try {
            this.port = Integer.parseInt(ipAndPort[1].trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("illegal host");
        }
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String toStrStyle() {
        return ip + ":" + port;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Node node = (Node) o;

        if (port != node.port) return false;
        return ip != null ? ip.equals(node.ip) : node.ip == null;
    }

    @Override
    public int hashCode() {
        int result = ip != null ? ip.hashCode() : 0;
        result = 31 * result + port;
        return result;
    }

    @Override
    public String toString() {
        return "Node{" +
                "ip='" + ip + '\'' +
                ", port=" + port +
                '}';
    }
}