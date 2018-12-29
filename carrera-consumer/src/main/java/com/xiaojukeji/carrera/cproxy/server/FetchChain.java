package com.xiaojukeji.carrera.cproxy.server;

import com.google.common.collect.Lists;
import com.xiaojukeji.carrera.thrift.consumer.FetchRequest;
import com.xiaojukeji.carrera.thrift.consumer.FetchResponse;
import org.apache.commons.collections.CollectionUtils;
import org.apache.thrift.async.AsyncMethodCallback;
import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;


public class FetchChain extends AbstractLowlevelRequestChain<FetchRequest, FetchResponse> {

    public static final Logger LOGGER = getLogger(FetchChain.class);

    public FetchChain(FetchRequest request, AsyncMethodCallback<FetchResponse> resultHandler) {
        super(request.getGroupId(), request, new FetchResponse(Lists.newArrayList()), resultHandler);
    }

    @Override
    public void doAction() {
        try {
            iter.next().fetch(this);
        } catch (Exception e) {
            resultHandler.onError(e);
            LOGGER.error("[lowlevel] error while doNext in fetchChain. group:{}, err.msg:{}", getRequest().getGroupId(), e.getMessage(), e);
        }
    }

    @Override
    public void saveResponse(FetchResponse res) {
        if (res != null && CollectionUtils.isNotEmpty(res.getResults())) {
            response.getResults().addAll(res.getResults());
        }
    }
}