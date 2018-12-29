package com.xiaojukeji.carrera.producer;


public class CarreraReturnCode {

    public static final int OK = 0;

    public static final int FAIL_ILLEGAL_MSG = 11;
    public static final int FAIL_TIMEOUT = 12;
    public static final int FAIL_TOPIC_NOT_ALLOWED = 13;
    public static final int FAIL_TOPIC_NOT_EXIST = 14;
    public static final int FAIL_REFUSED_BY_RATE_LIMITER = 15;

    public static final int CLIENT_EXCEPTION = 101;
    public static final int MISSING_PARAMETERS = 102;
    public static final int UNKNOWN_EXCEPTION = 103;
    public static final int NO_MORE_HEALTHY_NODE = 104;
    public static final int CHARSET_ENCODING_EXCEPTION = 105;

    public static final int THRIFT_EXCEPTION = 110;
    public static final int THRIFT_NETWORK_EXCEPTION = 111;
}