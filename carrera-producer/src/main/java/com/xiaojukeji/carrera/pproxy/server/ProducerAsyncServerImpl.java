package com.xiaojukeji.carrera.pproxy.server;

import com.xiaojukeji.carrera.pproxy.producer.BatchCarreraRequest;
import com.xiaojukeji.carrera.pproxy.producer.CarreraRequest;
import com.xiaojukeji.carrera.pproxy.producer.CarreraRequestForRMQBatch;
import com.xiaojukeji.carrera.pproxy.producer.ProducerPool;
import com.xiaojukeji.carrera.pproxy.producer.ProxySendResult;
import com.xiaojukeji.carrera.pproxy.producer.TimeOutHandlerMgr;
import com.xiaojukeji.carrera.pproxy.producer.delay.DelayRequest;
import com.xiaojukeji.carrera.pproxy.producer.delay.ProxySendDelayResult;
import com.xiaojukeji.carrera.pproxy.utils.MetricUtils;
import com.xiaojukeji.carrera.pproxy.utils.MsgCheckUtils;
import com.xiaojukeji.carrera.thrift.DelayMessage;
import com.xiaojukeji.carrera.thrift.Message;
import com.xiaojukeji.carrera.thrift.ProducerService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.thrift.TException;
import org.apache.thrift.async.AsyncMethodCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;


public class ProducerAsyncServerImpl implements ProducerService.AsyncIface {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProducerAsyncServerImpl.class);

    private final ProducerPool producerPool;

    public ProducerAsyncServerImpl(ProducerPool producerPool, int timeoutCheckerThreads) {
        this.producerPool = producerPool;
        TimeOutHandlerMgr.init(timeoutCheckerThreads);
        LOGGER.info("scheduler started! timeoutCheckerThreads={}", timeoutCheckerThreads);
    }

    @Override
    public void sendSync(Message message, long timeout, @SuppressWarnings("rawtypes") AsyncMethodCallback resultHandler) throws TException {
        MetricUtils.incRequestCounter(message.topic, MetricUtils.REQUEST_SYNC);
        CarreraRequest request = new CarreraRequest(producerPool, message, timeout, resultHandler);

        if (request.checkValid()) {
            request.registerTimeout(TimeOutHandlerMgr.selectOneTimeOutChecker());
            request.process();
        } else {
            request.onFinish(ProxySendResult.FAIL_ILLEGAL_MSG);
        }
    }

    @Override
    public void sendDelaySync(DelayMessage delayMessage, long timeout, AsyncMethodCallback resultHandler) throws TException {
        MetricUtils.incRequestCounter(delayMessage.getTopic(), MetricUtils.REQUEST_DELAY_SYNC);

        DelayRequest delayRequest = new DelayRequest(producerPool, delayMessage, DelayRequest.buildMessageForCheck(delayMessage),
                timeout, resultHandler);

        //origin msg check
        if(!delayRequest.checkValid()) {
            delayRequest.onFinish(new ProxySendDelayResult(ProxySendResult.FAIL_ILLEGAL_MSG));
            return;
        }

        ProxySendDelayResult proxySendDelayResult = delayRequest.checkDelayMessage();
        if(proxySendDelayResult.getProxySendResult() != ProxySendResult.OK) {
            delayRequest.onFinish(proxySendDelayResult);
            return;
        }

        delayRequest.buildAndUpdateMessage();
        if (delayRequest.checkValid()) {//delay msg check
            delayRequest.registerTimeout(TimeOutHandlerMgr.selectOneTimeOutChecker());
            delayRequest.process();
        } else {
            delayRequest.onFinish(new ProxySendDelayResult(ProxySendResult.FAIL_ILLEGAL_MSG));
        }
    }

    @Override
    public void sendAsync(Message message, @SuppressWarnings("rawtypes") AsyncMethodCallback resultHandler) throws TException {
        MetricUtils.incRequestCounter(message.topic, MetricUtils.REQUEST_ASYNC);
        CarreraRequest request = new CarreraRequest(producerPool, message, resultHandler);
        if (request.checkValid()) {
            request.process();
        } else {
            request.onFinish(ProxySendResult.FAIL_ILLEGAL_MSG);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void sendBatchSync(List<Message> messages, @SuppressWarnings("rawtypes") AsyncMethodCallback resultHandler)
            throws TException {
        if (CollectionUtils.isEmpty(messages)) {
            resultHandler.onComplete(ProxySendResult.OK.getResult());
            return;
        }

        CarreraRequestForRMQBatch rmqBatchRequest = new CarreraRequestForRMQBatch(producerPool, messages, resultHandler);
        if (rmqBatchRequest.checkValid()) {
            rmqBatchRequest.process();
            return;
        } else {
            rmqBatchRequest = null;
        }

        //检查msg的property是否合法，只要有一个不合法，直接返回不合法
        Set<String> topics = new HashSet<>();
        for (Message msg : messages) {
            if (msg != null) {
                topics.add(msg.getTopic());
                String conflictProperty = MsgCheckUtils.checkProperties(msg.getProperties());
                if (conflictProperty != null) {
                    LOGGER.error("illegal properties(Conflicts with reserved attribute keywords):topic={}, {}", msg.getTopic(), conflictProperty);
                    resultHandler.onComplete(ProxySendResult.FAIL_ILLEGAL_MSG.getResult());
                    return;
                }
            }
        }

        for (String topic : topics) {
            MetricUtils.incRequestCounter(topic, MetricUtils.REQUEST_BATCHSYNC);
        }

        AtomicInteger totalMsgCount = new AtomicInteger(messages.size());
        //unordered message
        LinkedList<BatchCarreraRequest> unOrderList = new LinkedList<>();
        //ordered message
        HashMap<String/*topic*/, HashMap<Integer/*pid*/, BatchCarreraRequest[]/*[head,tail]*/>> orderMap = new HashMap<>();
        try {
            messages.forEach(m -> {
                try {
                    String brokerCluster = producerPool.getConfigManager().getTopicConfigManager().getBrokerCluster(m);
                    int partitionSize = producerPool.getPartitionsSize(m.getTopic(), brokerCluster);
                    if (m.getPartitionId() >= 0 || m.getPartitionId() == -1) {

                        int pId = m.getPartitionId() == -1 ?
                                Math.abs((int) (m.getHashId() % partitionSize)) : m.getPartitionId() % partitionSize;
                        HashMap<Integer, BatchCarreraRequest[]> topicMap = orderMap.computeIfAbsent(m.getTopic(), k -> new HashMap<>());
                        BatchCarreraRequest request = new BatchCarreraRequest(producerPool, m, resultHandler,
                                totalMsgCount);
                        request.setBrokerCluster(brokerCluster);
                        BatchCarreraRequest[] pair = topicMap.get(pId);
                        if (pair == null) {
                            pair = new BatchCarreraRequest[2];
                            pair[0] = request;
                            pair[1] = request;
                            topicMap.put(pId, pair);
                        } else {
                            pair[1].setNext(request);
                            pair[1] = request;
                        }
                    } else {
                        unOrderList.add(new BatchCarreraRequest(producerPool, m, resultHandler, totalMsgCount));
                    }
                } catch (Throwable e) {
                    LOGGER.error("[BatchSend] request total num={},error msg topic={} partition={} version={}",
                            messages.size(), m.getTopic(), m.getPartitionId(), m.getVersion());
                    throw e;
                }
            });
        } catch (Throwable e) {
            resultHandler.onComplete(ProxySendResult.FAIL_UNKNOWN.getResult());
            return;
        }

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("[BatchSend]batch request total num={} unorderList.size={} orderSize={}",
                    messages.size(), unOrderList.size(),
                    orderMap.values().stream().mapToInt(map -> map.values().stream().mapToInt(pair -> {
                        int count = 1;
                        BatchCarreraRequest temp = pair[0];
                        for (; (temp = temp.getNext()) != null; count++) ;
                        return count;
                    }).sum()).sum());
        }

        unOrderList.forEach(CarreraRequest::process);
        orderMap.forEach((topic, map) -> map.forEach((pid, req) -> req[0].process()));
    }
}