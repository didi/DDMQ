package com.xiaojukeji.carrera.cproxy.server;

import com.google.common.collect.Lists;
import com.xiaojukeji.carrera.config.v4.GroupConfig;
import com.xiaojukeji.carrera.config.v4.cproxy.UpstreamTopic;
import com.xiaojukeji.carrera.cproxy.consumer.ConfigManager;
import com.xiaojukeji.carrera.cproxy.consumer.ConsumerManager;
import com.xiaojukeji.carrera.cproxy.consumer.LowLevelCarreraConsumer;
import com.xiaojukeji.carrera.cproxy.utils.ConfigUtils;
import org.apache.thrift.async.AsyncMethodCallback;
import org.slf4j.Logger;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static org.slf4j.LoggerFactory.getLogger;


abstract class AbstractLowlevelRequestChain<Request, Response> {
    public static final Logger LOGGER = getLogger(AbstractLowlevelRequestChain.class);

    private Request request;
    Response response;
    AsyncMethodCallback<Response> resultHandler;
    private List<LowLevelCarreraConsumer> consumers;
    Iterator<LowLevelCarreraConsumer> iter;

    AbstractLowlevelRequestChain(String group, Request request, Response response, AsyncMethodCallback<Response> resultHandler) {
        this.request = request;
        this.response = response;
        this.resultHandler = resultHandler;
        consumers = getConsumers(group);
        iter = consumers.iterator();
    }

    public Request getRequest() {
        return request;
    }

    public void doNext() {
        if (iter != null && iter.hasNext()) {
            doAction();
        } else {
            onFinish();
        }
    }

    private void onFinish() {
        resultHandler.onComplete(response);
    }

    public void onError(Exception e) {
        resultHandler.onError(e);
    }

    int getConsumerNum() {
        return consumers.size();
    }

    abstract void doAction();

    abstract void saveResponse(Response res);

    private List<LowLevelCarreraConsumer> getConsumers(String group) {
        List<LowLevelCarreraConsumer> consumers = Lists.newArrayList();
        GroupConfig groupConf = ConfigUtils.findGroupByName(group, ConfigManager.getInstance());

        Set<String> clusters = new HashSet<>();

        if (groupConf != null) {
            for (UpstreamTopic topic : groupConf.getTopics()) {
                clusters.add(topic.getBrokerCluster());
            }
        }
        for(String cluster : clusters) {
            LowLevelCarreraConsumer consumer = ConsumerManager.getInstance()
                    .getLowLevelConsumer(group, cluster);
            if(consumer != null) {
                consumers.add(consumer);
            }
        }
        LOGGER.trace("[lowlevel] group:{}, clusters:{}, consumers:{}.", group, clusters, consumers);
        return consumers;
    }

}