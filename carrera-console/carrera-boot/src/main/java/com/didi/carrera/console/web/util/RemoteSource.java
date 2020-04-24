package com.didi.carrera.console.web.util;


public class RemoteSource {
    protected RemoteSource(String ip, String source) {
        this.ip = ip;
        this.source = source;
    }

    public String toString() {
        return "ip=" + ip + "||ipSrc=" + source;
    }

    public final String ip;
    public final String source;
}