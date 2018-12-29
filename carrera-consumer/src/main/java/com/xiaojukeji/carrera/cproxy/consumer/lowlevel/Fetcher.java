package com.xiaojukeji.carrera.cproxy.consumer.lowlevel;


import com.xiaojukeji.carrera.thrift.consumer.AckResult;
import com.xiaojukeji.carrera.thrift.consumer.FetchRequest;
import com.xiaojukeji.carrera.thrift.consumer.FetchResponse;


public interface Fetcher {

    boolean start();

    void shutdown();

    FetchResponse fetch(FetchRequest request);

    boolean ack(AckResult result);

    long getLastFetchTimestamp();

    void logMetrics();
}