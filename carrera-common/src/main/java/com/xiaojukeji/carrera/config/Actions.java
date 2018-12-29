package com.xiaojukeji.carrera.config;

import com.google.common.collect.Lists;

import java.util.HashSet;
import java.util.Set;


public class Actions {
    public static final String FormParams = "FormParams";
    public static final String FormParams2 = "FormParams2";
    public static final String QueryParams = "QueryParams";
    public static final String TRANSIT = "Transit";
    public static final String JSON = "Json";
    public static final String ASYNC = "Async";
    public static final String GROOVY = "Groovy";
    public static final String ASYNC_HTTP = "AsyncHttp";
    public static final String PULL_SERVER = "PullServer";
    public static final String REDIS = "Redis";
    public static final String LowLevel = "LowLevel";
    public static final String NONBLOCKASYNC = "NonBlockAsync";
    public static final String HDFS = "Hdfs";
    public static final String HBASE = "Hbase";

    private static Set<String> ACTION_NAME_SET = new HashSet<>();

    static {
        ACTION_NAME_SET.addAll(Lists.newArrayList(FormParams, FormParams2, QueryParams, TRANSIT, JSON,
                ASYNC, GROOVY, ASYNC_HTTP, PULL_SERVER, REDIS, LowLevel, NONBLOCKASYNC, HDFS, HBASE));
    }

    public static boolean isValidAction(String actionName) {
        return ACTION_NAME_SET.contains(actionName);
    }
}