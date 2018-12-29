package com.xiaojukeji.carrera.cproxy.actions;

import com.xiaojukeji.carrera.cproxy.config.ConsumerGroupConfig;
import com.xiaojukeji.carrera.cproxy.utils.ConfigUtils;


public class ActionBuilder {
    private static final String FormParams = "FormParams";
    private static final String FormParams2 = "FormParams2";
    private static final String QueryParams = "QueryParams";
    private static final String TRANSIT = "Transit";
    private static final String JSON = "Json";
    private static final String ASYNC = "Async";
    public static final String GROOVY = "Groovy";
    public static final String ASYNC_HTTP = "AsyncHttp";
    public static final String PULL_SERVER = "PullServer";
    public static final String REDIS = "Redis";
    public static final String HDFS = "Hdfs";
    public static final String HBASE = "Hbase";
    private static final String NONBLOCKASYNC = "NonBlockAsync";

    public static Action newAction(ConsumerGroupConfig config, String actionName) {
        if (ConfigUtils.isRmqMQCluster(config.getcProxyConfig(), config.getBrokerCluster())) {
            boolean isSatisfyNewRmqConsumer = ConfigUtils.satisfyNewRmqConsumer(config.getGroupConfig());
            if (ASYNC.equals(actionName) && isSatisfyNewRmqConsumer) {
                actionName = NONBLOCKASYNC;
            } else if (NONBLOCKASYNC.equals(actionName) && !isSatisfyNewRmqConsumer) {
                actionName = ASYNC;
            }
        }
        switch (actionName) {
            case FormParams:
                return new FormParamsExtractAction();
            case FormParams2:
                return new FormParamsExtractAction2();
            case QueryParams:
                return new QueryParamsExtractAction();
            case TRANSIT:
                return new TransitAction();
            case JSON:
                return new JsonAction();
            case ASYNC:
                return new AsyncAction(config);
            case ASYNC_HTTP:
                return new AsyncHttpAction(config);
            case GROOVY:
                return new GroovyScriptAction();
            case PULL_SERVER:
                return new PullServerBufferAction(config);
            case REDIS:
                return new RedisAction(config);
            case HBASE:
                return new HBaseAction(config);
            case HDFS:
                return new HDFSAction(config);
            case NONBLOCKASYNC:
                return new NonBlockAsyncAction(config);
            default:
                return null;
        }
    }
}