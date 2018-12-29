package com.xiaojukeji.carrera.consumer.thrift.client;

import com.xiaojukeji.carrera.consumer.thrift.ConsumeResult;
import com.xiaojukeji.carrera.consumer.thrift.ConsumeStats;
import com.xiaojukeji.carrera.consumer.thrift.ConsumeStatsRequest;
import com.xiaojukeji.carrera.consumer.thrift.Context;
import com.xiaojukeji.carrera.consumer.thrift.Message;
import com.xiaojukeji.carrera.consumer.thrift.PullException;
import com.xiaojukeji.carrera.consumer.thrift.PullRequest;
import com.xiaojukeji.carrera.consumer.thrift.PullResponse;
import org.apache.thrift.TException;
import org.apache.thrift.transport.TTransportException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class SimpleCarreraConsumer extends BaseCarreraConsumer<PullRequest, PullResponse> {
    public static final String CARRERA_CONSUMER_TYPE = "simple";

    private ConsumeStatsRequest consumeStatsRequest;

    private Map<Context, ConsumeResult> resultMap;

    /**
     * 单线程消费一个consumer proxy的客户端。config.servers只能指定一个server。
     *
     * @param config
     */
    public SimpleCarreraConsumer(CarreraConfig config) {
        this(config, null);
    }

    /**
     * 只消费group中指定topic的消息。
     *
     * @param config 消费的配置
     * @param topic  指定消费的topic。null表示不指定。
     */
    public SimpleCarreraConsumer(CarreraConfig config, String topic) {
        super(config, topic);

        this.type = CARRERA_CONSUMER_TYPE;
        resultMap = new HashMap<>();
    }

    @Override
    protected void initRequest() {
        request = new PullRequest(config.getGroupId());
        request.setVersion(getVersion());
        request.setMaxBatchSize(config.getMaxBatchSize());
        request.setMaxLingerTime(config.getMaxLingerTime());
        request.setTopic(topic);

        consumeStatsRequest = new ConsumeStatsRequest(config.getGroupId());
        consumeStatsRequest.setVersion(getVersion());
        consumeStatsRequest.setTopic(topic);
    }

    private void doSubmit() throws InterruptedException {
        ConsumeResult consumeResult = buildConsumeResult();
        if (consumeResult == null) {
            LOGGER.debug("empty consumeResult, abort submitting.");
            return;
        }
        for (int i = 0; i < config.getSubmitMaxRetries(); i++) {
            try {
                ensureConnection();
                if (client.submit(consumeResult)) {
                    LOGGER.debug("Client.submit={}", consumeResult);
                    clearResult(consumeResult);
                    return;
                } else {
                    LOGGER.warn("doSubmit failed!, retryCnt=" + i);
                }
            } catch (TTransportException e) {
                LOGGER.error("submit transport error, retryCnt=" + i, e);
                transport.close();
            } catch (TException e) {
                LOGGER.error("submit error, retryCnt=" + i, e);
            }
            doRetrySleep();
        }
    }

    @Override
    protected void doClose() throws Exception {
        doSubmit();
    }

    private void clearResult(ConsumeResult result) {
        for (ConsumeResult r = result; r != null; r = r.nextResult) {
            r.getFailOffsets().clear();
            r.getSuccessOffsets().clear();
        }
    }

    private ConsumeResult buildConsumeResult() {
        ConsumeResult ret = null;
        if (resultMap == null) {
            return ret;
        }

        for (ConsumeResult r : resultMap.values()) {
            if (r.getFailOffsetsSize() > 0 || r.getSuccessOffsetsSize() > 0) {
                r.nextResult = ret;
                ret = r;
            }
        }
        return ret;
    }

    @Override
    protected PullResponse doPullMessage() throws TException {
        ConsumeResult curResult = buildConsumeResult();
        try {
            request.setResult(curResult);
            PullResponse response = client.pull(request);
            clearResult(curResult);

            LOGGER.debug("Client Request for {}:{}, Response:{}", host, request, response);
            if (response == null || response.getMessages() == null || response.getMessages().size() == 0) {
                LOGGER.debug("retry in {}ms, response={}", config.getRetryInterval(), response);
                return null;
            } else {
                return response;
            }
        } catch (PullException e) {
            LOGGER.error("pull exception, code={}, message={}", e.code, e.message);
        }

        return null;
    }

    /**
     * @return
     * @deprecated instead of using {@link BaseCarreraConsumer#pullMessage()}.
     */
    @Deprecated
    public synchronized PullResponse submitAndPull() {
        return pullMessage();
    }

    @Override
    protected void doProcessMessage(PullResponse response, MessageProcessor processor) {
        Context context = response.getContext();

        for (Message msg : response.getMessages()) {
            MessageProcessor.Result pResult = MessageProcessor.Result.FAIL;
            try {
                pResult = processor.process(msg, context);
                LOGGER.debug("ProcessResult:{},msg.key={},group={},topic={},qid={}，offset={}", pResult,
                        msg.getKey(), context.getGroupId(), context.getTopic(), context.getQid(), msg.getOffset());
            } catch (Throwable e) {
                LOGGER.error("exception when processing message, msg=" + msg + ",context=" + context, e);
            }
            switch (pResult) {
                case SUCCESS:
                    ack(context, msg.getOffset());
                    break;
                case FAIL:
                    fail(context, msg.getOffset());
                    break;
            }
        }
    }

    @Override
    protected void doProcessMessage(PullResponse response, BatchMessageProcessor processor) {
        Context context = response.getContext();
        Map<BaseMessageProcessor.Result, List<Long>> processRet = processor.process(response.getMessages(), context);
        if (processRet == null || processRet.isEmpty()) {
            return;
        }
        for (Map.Entry<BaseMessageProcessor.Result, List<Long>> entry : processRet.entrySet()) {
            BaseMessageProcessor.Result ret = entry.getKey();
            List<Long> offsetList = entry.getValue();

            switch (ret) {
                case SUCCESS:
                    for (Long offset : offsetList) {
                        ack(context, offset);
                    }
                    break;
                case FAIL:
                    for (Long offset : offsetList) {
                        fail(context, offset);
                    }
                    break;
            }
        }
    }

    public synchronized void ack(Context context, long offset) {
        getResult(context).getSuccessOffsets().add(offset);
    }

    private ConsumeResult getResult(Context context) {
        ConsumeResult consumeResult = resultMap.get(context);
        if (consumeResult == null) {
            consumeResult = new ConsumeResult();
            consumeResult.setContext(context);
            consumeResult.setSuccessOffsets(new ArrayList<Long>());
            consumeResult.setFailOffsets(new ArrayList<Long>());
            resultMap.put(context, consumeResult);
        }
        return consumeResult;
    }

    public synchronized void fail(Context context, long offset) {
        getResult(context).getFailOffsets().add(offset);
    }

    public List<ConsumeStats> getAllConsumeStats() throws TException {
        return getConsumeStats(null);
    }

    public List<ConsumeStats> getConsumeStats() throws TException {
        return getConsumeStats(topic);
    }

    public List<ConsumeStats> getConsumeStats(String topic) throws TException {
        ensureConnection();
        consumeStatsRequest.setTopic(topic);
        return client.getConsumeStats(consumeStatsRequest);
    }

    @Override
    public String toString() {
        return "SimpleCarreraConsumer{" +
                super.toString() +
                '}';
    }
}