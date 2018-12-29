package com.xiaojukeji.carrera.cproxy.actions.http;


public class HttpParam {

    public enum HttpParamType {
        HEADER, QUERY, FORM
    }

    public HttpParamType type;
    public String key;
    public String value;

    public HttpParam(HttpParamType type, String key, String value) {
        this.type = type;
        this.key = key;
        this.value = value;
    }

    @Override
    public String toString() {
        return type + "{" + key + "=" + value + '}';
    }
}