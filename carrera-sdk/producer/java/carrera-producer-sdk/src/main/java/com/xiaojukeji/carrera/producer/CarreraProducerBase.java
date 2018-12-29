package com.xiaojukeji.carrera.producer;

import com.alibaba.fastjson.JSON;
import com.eaio.uuid.UUID;
import com.xiaojukeji.carrera.config.CarreraConfig;
import com.xiaojukeji.carrera.exception.CarreraException;
import com.xiaojukeji.carrera.nodemgr.Node;
import com.xiaojukeji.carrera.nodemgr.NodeManager;
import com.xiaojukeji.carrera.nodemgr.connection.CarreraConnection;
import com.xiaojukeji.carrera.thrift.DelayMessage;
import com.xiaojukeji.carrera.thrift.DelayMeta;
import com.xiaojukeji.carrera.thrift.DelayResult;
import com.xiaojukeji.carrera.thrift.Message;
import com.xiaojukeji.carrera.thrift.Result;
import com.xiaojukeji.carrera.utils.RandomKeyUtils;
import com.xiaojukeji.carrera.utils.TimeUtils;
import com.xiaojukeji.carrera.utils.VersionUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;

import static com.xiaojukeji.carrera.producer.CarreraReturnCode.CHARSET_ENCODING_EXCEPTION;
import static com.xiaojukeji.carrera.producer.CarreraReturnCode.CLIENT_EXCEPTION;
import static com.xiaojukeji.carrera.producer.CarreraReturnCode.FAIL_ILLEGAL_MSG;
import static com.xiaojukeji.carrera.producer.CarreraReturnCode.FAIL_REFUSED_BY_RATE_LIMITER;
import static com.xiaojukeji.carrera.producer.CarreraReturnCode.FAIL_TIMEOUT;
import static com.xiaojukeji.carrera.producer.CarreraReturnCode.FAIL_TOPIC_NOT_ALLOWED;
import static com.xiaojukeji.carrera.producer.CarreraReturnCode.FAIL_TOPIC_NOT_EXIST;
import static com.xiaojukeji.carrera.producer.CarreraReturnCode.NO_MORE_HEALTHY_NODE;
import static com.xiaojukeji.carrera.producer.CarreraReturnCode.OK;
import static com.xiaojukeji.carrera.producer.CarreraReturnCode.UNKNOWN_EXCEPTION;


public abstract class CarreraProducerBase implements ProducerInterface {
    private static final Logger LOGGER = LoggerFactory.getLogger(CarreraProducerBase.class);
    private static final Logger DROP_LOGGER = LoggerFactory.getLogger("DROP_LOG");

    private static final int DELAY_ACTIONS_ADD = 1;
    private static final int DELAY_ACTIONS_CANCEL = 2;
    private static final String TAGS_SEPARATOR = "||";
    private volatile boolean isRunning = false;
    protected NodeManager nodeMgr;
    protected CarreraConfig config;
    private ExecutorService executor;

    public CarreraProducerBase(CarreraConfig config) {
        this.config = config;
    }

    public synchronized void start() throws Exception {
        if (!config.validate()) {
            throw new CarreraException("carrera config is illegal, config=" + config.toString());
        }
        if (!isRunning) {
            this.init();
            isRunning = true;
        }
    }

    private void init() throws Exception {
        this.initNodeMgr();
        this.initBatchSender();
    }

    public CarreraConfig getConfig() {
        return config;
    }

    protected abstract void initNodeMgr() throws Exception;

    protected void initBatchSender() {
        if (executor != null) {
            executor.shutdownNow();
        }
        executor = Executors.newFixedThreadPool(config.getBatchSendThreadNumber(), new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, "CarreraBatchSender");
            }
        });
    }

    public synchronized void shutdown() {
        if (isRunning) {
            if (executor != null && !executor.isShutdown()) {
                executor.shutdown();
            }
            if (nodeMgr != null) {
                nodeMgr.shutdown();
            }
            isRunning = false;
        }
    }

    public Result sendMessage(Message message) {
        Result result = new Result(UNKNOWN_EXCEPTION, "unknown exception");
        if (!isRunning) {
            result.setCode(CLIENT_EXCEPTION);
            result.setMsg("please execute the start() method before sending the message");
            return result;
        }
        int retryCnt = 0;
        long start, used = 0;
        long begin = TimeUtils.getCurTime();
        String proxyAddress = null;
        do {
            CarreraConnection connection = null;
            try {
                connection = nodeMgr.borrowConnection(config.getCarreraClientTimeout());
                if (connection == null) {
                    if (result.getCode() == UNKNOWN_EXCEPTION) {
                        result.setCode(NO_MORE_HEALTHY_NODE);
                        result.setMsg("no more healthy node");
                    }
                    delay(config.getCarreraClientTimeout());
                    continue;
                }
                proxyAddress = connection.getNode().toString();
                start = TimeUtils.getCurTime();
                result = connection.send(message, this.config.getCarreraProxyTimeout());
                used = TimeUtils.getElapseTime(start);
                //nodeMgr.returnConnection(connection);
                result.setKey(message.getKey());

                if (!isNeedRetry(result, connection.getNode(), start)) {
                    break;
                }
            } catch (Exception e) {
                LOGGER.warn("sendMessage failed, retry count:" + retryCnt + ", topic:" + message.topic + ", key:" + message.key, e);
                result.setCode(CLIENT_EXCEPTION);
                result.setMsg(e.toString());
            } finally {
                if (connection != null) {
                    nodeMgr.returnConnection(connection);
                }
            }
        } while (retryCnt++ < this.config.getCarreraClientRetry());

        if (result.getCode() > OK) {
            LOGGER.error("send msg result:{}; msg[ip:{},topic:{},key:{},partition:{},hashId:{},len:{},used:{},retryCount:{},ret.Code:{},ret.Msg:{}]",
                    resultToString(result), proxyAddress, message.getTopic(), message.getKey(), message.getPartitionId(), message.getHashId(),
                    ArrayUtils.getLength(message.getBody()), TimeUtils.getElapseTime(begin), retryCnt, result.getCode(), result.getMsg());
            String reason = JSON.toJSONString(result);
            String dropMsg = JSON.toJSONString(message);
            DROP_LOGGER.info("REASON:{},CARRERA_MESSAGE:{}", reason, dropMsg);
        } else {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("send msg result:{}; msg[ip:{},topic:{},key:{},partition:{},hashId:{},len:{},used:{},retryCount:{}]",
                        resultToString(result), proxyAddress, message.getTopic(), message.getKey(), message.getPartitionId(), message.getHashId(),
                        ArrayUtils.getLength(message.getBody()), used, retryCnt);
            }
        }
        return result;
    }

    private boolean isNeedRetry(Result result, Node node, long start) {
        boolean ret = true;
        if (result.getCode() > OK) {
            switch (result.getCode()) {
                case FAIL_ILLEGAL_MSG:
                case FAIL_TOPIC_NOT_ALLOWED:
                case FAIL_TOPIC_NOT_EXIST:
                case FAIL_TIMEOUT:
                case FAIL_REFUSED_BY_RATE_LIMITER:
                    delay(Math.max(this.config.getCarreraClientTimeout() - TimeUtils.getElapseTime(start), 0));
                    break;
                default:
                    nodeMgr.unhealthyNode(node);
                    delay(Math.max(this.config.getCarreraClientTimeout() - TimeUtils.getElapseTime(start), 0));
                    break;
            }
        } else {
            ret = false;
        }

        return ret;
    }

    public Result send(String topic, byte[] body) {
        return sendMessage(buildMessage(topic, CarreraConfig.PARTITION_RAND, 0, body, randomKey()));
    }

    public Result send(String topic, String body) {
        return sendMessage(buildMessage(topic, CarreraConfig.PARTITION_RAND, 0, body.getBytes(), randomKey()));
    }

    public Result sendByCharset(String topic, String body, String charsetName) {
        try {
            return sendMessage(buildMessage(topic, CarreraConfig.PARTITION_RAND, 0, body.getBytes(charsetName), randomKey()));
        } catch (UnsupportedEncodingException e) {
            return new Result(CHARSET_ENCODING_EXCEPTION, e.getMessage());
        }
    }

    public Result send(String topic, String body, String key, String... tags) {
        return sendMessage(buildMessage(topic, CarreraConfig.PARTITION_RAND, 0, body.getBytes(), key, tags));
    }

    public Result send(String topic, byte[] body, String key, String... tags) {
        return sendMessage(buildMessage(topic, CarreraConfig.PARTITION_RAND, 0, body, key, tags));
    }

    public Result sendByCharset(String topic, String body, String charsetName, String key, String... tags) {
        try {
            return sendMessage(buildMessage(topic, CarreraConfig.PARTITION_RAND, 0, body.getBytes(charsetName), key, tags));
        } catch (UnsupportedEncodingException e) {
            return new Result(CHARSET_ENCODING_EXCEPTION, e.getMessage());
        }
    }

    public Result sendWithHashId(String topic, long hashId, String body, String key, String... tags) {
        return sendMessage(buildMessage(topic, CarreraConfig.PARTITION_HASH, hashId, body.getBytes(), key, tags));
    }

    public Result sendWithHashId(String topic, long hashId, byte[] body, String key, String... tags) {
        return sendMessage(buildMessage(topic, CarreraConfig.PARTITION_HASH, hashId, body, key, tags));
    }

    public Result sendWithHashIdByCharset(String topic, long hashId, String body, String charsetName, String key, String[] tags) {
        try {
            return sendMessage(buildMessage(topic, CarreraConfig.PARTITION_HASH, hashId, body.getBytes(charsetName), key, tags));
        } catch (UnsupportedEncodingException e) {
            return new Result(CHARSET_ENCODING_EXCEPTION, e.getMessage());
        }
    }

    public Result sendWithPartition(String topic, int partitionId, long hashId, byte[] body, String key, String... tags) {
        return sendMessage(buildMessage(topic, partitionId, hashId, body, key, tags));
    }

    public Result sendWithPartition(String topic, int partitionId, long hashId, String body, String key, String... tags) {
        return sendMessage(buildMessage(topic, partitionId, hashId, body.getBytes(), key, tags));
    }

    public Result sendWithPartitionByCharset(String topic, int partitionId, long hashId, String body, String charsetName, String key, String[] tags) {
        try {
            return sendMessage(buildMessage(topic, partitionId, hashId, body.getBytes(charsetName), key, tags));
        } catch (UnsupportedEncodingException e) {
            return new Result(CHARSET_ENCODING_EXCEPTION, e.getMessage());
        }
    }

    public Result sendBatchConcurrently(List<Message> messages) {
        Result result = new Result();
        if (messages == null || messages.size() == 0) {
            result.setCode(OK);
            result.setMsg("empty messages.");
            return result;
        }
        Map<Long, List<Message>> msgMap = new HashMap<>();
        for (Message msg : messages) {
            List<Message> msgList = msgMap.get(msg.getHashId() % config.getBatchSendThreadNumber());
            if (msgList == null) {
                msgList = new ArrayList<>();
                msgMap.put(msg.getHashId(), msgList);
            }
            msgList.add(msg);
        }

        List<Future<Result>> futures = new ArrayList<>();
        for (List<Message> msgList : msgMap.values()) {
            futures.add(executor.submit(new CarreraProducerBase.SendTask(msgList)));
        }

        for (Future<Result> future : futures) {
            try {
                Result ret = future.get();
                if (ret.code > OK) {
                    return ret;
                } else if (ret.code > result.code || !result.isSetCode()) {
                    result = ret;
                }
            } catch (Exception e) {
                if (e instanceof InterruptedException) {
                    Thread.currentThread().interrupt();
                }
                LOGGER.warn("sendBatchConcurrently failed", e);
                result.setCode(CLIENT_EXCEPTION);
                result.setMsg(e.getClass().getSimpleName() + "-" + e.getMessage());
                return result;
            }
        }
        return result;
    }

    public Result sendBatchOrderly(List<Message> messages) {
        Result result = new Result();
        if (messages == null || messages.size() == 0) {
            result.setCode(OK);
            result.setMsg("empty messages.");
            return result;
        }
        for (Message msg : messages) {
            Result ret = sendMessage(msg);
            ret.setKey(msg.getKey());
            if (ret.code > OK) {
                return ret;
            } else if (ret.code > result.code || !result.isSetCode()) {
                result = ret;
            }
        }
        return result;
    }

    public Result sendBatchSync(List<Message> messages) {
        Result result = new Result(UNKNOWN_EXCEPTION, "unknown exception");
        if (messages == null || messages.size() == 0) {
            result.setCode(OK);
            result.setMsg("empty messages.");
            return result;
        }

        if (!isRunning) {
            result.setCode(CLIENT_EXCEPTION);
            result.setMsg("please execute the start() method before sending the message");
            return result;
        }
        int retryCnt = 0;
        long start, used = 0;
        long begin = TimeUtils.getCurTime();
        String proxyAddress = null;

        do {
            CarreraConnection connection = null;
            try {
                connection = nodeMgr.borrowConnection(config.getCarreraClientTimeout());
                if (connection == null) {
                    if (result.getCode() == UNKNOWN_EXCEPTION) {
                        result.setCode(NO_MORE_HEALTHY_NODE);
                        result.setMsg("no more healthy node");
                    }
                    delay(config.getCarreraClientTimeout());
                    continue;
                }
                proxyAddress = connection.getNode().toString();
                start = TimeUtils.getCurTime();
                result = connection.sendBatchSync(messages);
                used = TimeUtils.getElapseTime(start);
                result.setKey(null);

                if (!isNeedRetry(result, connection.getNode(), start)) {
                    break;
                }
            } catch (Exception e) {
                LOGGER.warn("sendBatchSync failed, retry count:" + retryCnt, e);
                result.setCode(CLIENT_EXCEPTION);
                result.setMsg(e.toString());
            } finally {
                if (connection != null) {
                    nodeMgr.returnConnection(connection);
                }
            }
        } while (retryCnt++ < this.config.getCarreraClientRetry());


        if (result.getCode() > OK) {
            LOGGER.error("send msg batch result:{}; msg[ip:{}, used:{},retryCount:{},ret.Code:{},ret.Msg:{}]",
                    resultToString(result), proxyAddress, TimeUtils.getElapseTime(begin), retryCnt, result.getCode(), result.getMsg());
            String reason = JSON.toJSONString(result);
            String dropMsgs = JSON.toJSONString(messages);
            DROP_LOGGER.info("REASON:{},CARRERA_MESSAGE:{}", reason, dropMsgs);
        } else {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("send msg result:{}; msg[ip:{}, used:{},retryCount:{}]", resultToString(result), proxyAddress, used, retryCnt);
            }
        }
        return result;
    }


    public static Message buildMessage(String topic, int partitionId, long hashId, String body, String key, String... tags) {
        return buildMessage(topic, partitionId, hashId, body.getBytes(), key, tags);
    }

    public static Message buildMessageByCharset(String topic, int partitionId, long hashId, String body, String charsetName, String key, String... tags) throws UnsupportedEncodingException {
        return buildMessage(topic, partitionId, hashId, body.getBytes(charsetName), key, tags);
    }

    public static Message buildMessage(String topic, int partitionId, long hashId, byte[] body, String key, String... tags) {
        Message message = new Message();
        message.setTopic(topic);
        message.setPartitionId(partitionId);
        message.setHashId(hashId);
        message.setBody(body);
        message.setKey(key);
        message.setVersion(VersionUtils.getVersion());
        if (ArrayUtils.isNotEmpty(tags)) {
            message.setTags(StringUtils.join(tags, TAGS_SEPARATOR));
        }
        return message;
    }


    private String randomKey() {
        return RandomKeyUtils.randomKey(CarreraConfig.RANDOM_KEY_SIZE);
    }

    class SendTask implements Callable<Result> {
        private List<Message> msgList;

        SendTask(List<Message> msgList) {
            this.msgList = msgList;
        }

        @Override
        public Result call() throws Exception {
            return sendBatchOrderly(msgList);
        }
    }

    private void delay(long delayTime) {
        try {
            Thread.sleep(delayTime);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private String resultToString(Result result) {
        if (result != null) {
            if (result.getCode() == OK) {
                return "success";
            } else {
                return "failure";
            }
        }
        return null;
    }

    private String resultToString(DelayResult result) {
        if (result != null) {
            if (result.getCode() == OK) {
                return "success";
            } else {
                return "failure";
            }
        }
        return null;
    }

    public DelayResult sendDelay(String topic, byte[] body, DelayMeta delayMeta) {
        return sendDelayMessage(buildDelayMessage4Add(topic, body, delayMeta, randomKey()));
    }

    public DelayResult sendDelay(String topic, String body, DelayMeta delayMeta) {
        return sendDelayMessage(buildDelayMessage4Add(topic, body.getBytes(), delayMeta, randomKey()));
    }

    public DelayResult sendDelayByCharset(String topic, String body, DelayMeta delayMeta, String charsetName) {
        try {
            return sendDelayMessage(buildDelayMessage4Add(topic, body.getBytes(charsetName), delayMeta));
        } catch (UnsupportedEncodingException e) {
            return new DelayResult(CHARSET_ENCODING_EXCEPTION, e.getMessage(), "");
        }
    }

    public DelayResult sendDelay(String topic, String body, DelayMeta delayMeta, String... tags) {
        return sendDelayMessage(buildDelayMessage4Add(topic, body.getBytes(), delayMeta, tags));
    }

    public DelayResult sendDelay(String topic, byte[] body, DelayMeta delayMeta, String... tags) {
        return sendDelayMessage(buildDelayMessage4Add(topic, body, delayMeta, tags));
    }

    public DelayResult sendDelayByCharset(String topic, String body, DelayMeta delayMeta, String charsetName, String... tags) {
        try {
            return sendDelayMessage(buildDelayMessage4Add(topic, body.getBytes(charsetName), delayMeta, tags));
        } catch (UnsupportedEncodingException e) {
            return new DelayResult(CHARSET_ENCODING_EXCEPTION, e.getMessage(), "");
        }
    }

    public DelayResult cancelDelay(String topic, String uniqDelayMsgId) {
        return sendDelayMessage(buildDelayMessage4Cancel(topic, uniqDelayMsgId, randomKey()));
    }

    public DelayResult cancelDelay(String topic, String uniqDelayMsgId, String... tags) {
        return sendDelayMessage(buildDelayMessage4Cancel(topic, uniqDelayMsgId, tags));
    }


    private DelayMessage buildDelayMessage4Add(String topic, String body, DelayMeta delayMeta, String... tags) {
        return buildDelayMessage4Add(topic, body.getBytes(), delayMeta, tags);
    }

    private DelayMessage buildDelayMessageByCharset4Add(String topic, String body, String charsetName, DelayMeta delayMeta, String... tags) throws UnsupportedEncodingException {
        return buildDelayMessage4Add(topic, body.getBytes(charsetName), delayMeta, tags);
    }

    private DelayMessage buildDelayMessage4Add(String topic, byte[] body, DelayMeta delayMeta, String... tags) {
        DelayMessage delayMessage = new DelayMessage();
        delayMessage.setTopic(topic);
        delayMessage.setBody(body);
        delayMessage.setAction(DELAY_ACTIONS_ADD);
        delayMessage.setTimestamp(delayMeta.getTimestamp());
        delayMessage.setDmsgtype(delayMeta.getDmsgtype());
        delayMessage.setInterval(delayMeta.getInterval());
        delayMessage.setExpire(delayMeta.getExpire());
        delayMessage.setTimes(delayMeta.getTimes());
        delayMessage.setUuid(new UUID().toString());
        delayMessage.setVersion(VersionUtils.getVersion());
        if (null != delayMeta.getProperties() && delayMeta.getProperties().size() > 0) {
            delayMessage.setProperties(delayMeta.getProperties());
        }

        if (ArrayUtils.isNotEmpty(tags)) {
            delayMessage.setTags(StringUtils.join(tags, TAGS_SEPARATOR));
        }

        return delayMessage;
    }

    private DelayMessage buildDelayMessage4Cancel(String topic, String uniqDelayMsgId, String... tags) {
        DelayMessage delayMessage = new DelayMessage();
        delayMessage.setTopic(topic);
        delayMessage.setUniqDelayMsgId(uniqDelayMsgId);
        delayMessage.setAction(DELAY_ACTIONS_CANCEL);
        delayMessage.setVersion(VersionUtils.getVersion());
        delayMessage.setBody("c".getBytes()); // if body is null, new String(message.getBody()) will throw NullPointerException

        if (ArrayUtils.isNotEmpty(tags)) {
            delayMessage.setTags(StringUtils.join(tags, TAGS_SEPARATOR));
        }

        return delayMessage;
    }


    // delay 没有 dropLog
    private DelayResult sendDelayMessage(DelayMessage message) {
        DelayResult result = new DelayResult(UNKNOWN_EXCEPTION, "unknown exception", "");
        if (!isRunning) {
            result.setCode(CLIENT_EXCEPTION);
            result.setMsg("please execute the start() method before sending the message");
            return result;
        }
        int retryCnt = 0;
        long start, used = 0;
        long begin = TimeUtils.getCurTime();
        String proxyAddress = null;
        do {
            CarreraConnection connection = null;
            try {
                connection = nodeMgr.borrowConnection(config.getCarreraClientTimeout());
                if (connection == null) {
                    if (result.getCode() == UNKNOWN_EXCEPTION) {
                        result.setCode(NO_MORE_HEALTHY_NODE);
                        result.setMsg("no more healthy node");
                    }
                    delay(config.getCarreraClientTimeout());
                    continue;
                }
                proxyAddress = connection.getNode().toString();
                start = TimeUtils.getCurTime();
                result = connection.sendDelay(message, this.config.getCarreraProxyTimeout());
                used = TimeUtils.getElapseTime(start);

                if (result.getCode() > OK) {
                    switch (result.getCode()) {
                        case FAIL_ILLEGAL_MSG:
                        case FAIL_TOPIC_NOT_ALLOWED:
                        case FAIL_TOPIC_NOT_EXIST:
                        case FAIL_TIMEOUT:
                        case FAIL_REFUSED_BY_RATE_LIMITER:
                            delay(Math.max(this.config.getCarreraClientTimeout() - TimeUtils.getElapseTime(start), 0));
                            break;
                        default:
                            nodeMgr.unhealthyNode(connection.getNode());
                            delay(Math.max(this.config.getCarreraClientTimeout() - TimeUtils.getElapseTime(start), 0));
                            break; //break switch
                    }
                } else {
                    break; //break loop
                }
            } catch (Exception e) {
                LOGGER.warn("sendMessage failed, retry count:" + retryCnt + ", topic:" + message.topic + ", key:" + message.uniqDelayMsgId, e);
                result.setCode(CLIENT_EXCEPTION);
                result.setMsg(e.toString());
            } finally {
                if (connection != null) {
                    nodeMgr.returnConnection(connection);
                }
            }
        } while (retryCnt++ < this.config.getCarreraClientRetry());

        if (result.getCode() > OK) {
            LOGGER.error("send delay msg result:{}; msg[ip:{},topic:{},uuid:{},uniqDelayMsgId:{},len:{},used:{},retryCount:{},ret.Code:{},ret.Msg:{}]",
                    resultToString(result), proxyAddress, message.getTopic(), message.getUuid(), message.getUniqDelayMsgId(),
                    StringUtils.length(new String(message.getBody())), TimeUtils.getElapseTime(begin), retryCnt, result.getCode(), result.getMsg());
        } else {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("send delay msg result:{}; msg[ip:{},topic:{},uniqDelayMsgId:{},len:{},used:{},retryCount:{}]",
                        resultToString(result), proxyAddress, message.getTopic(), result.getUniqDelayMsgId(),
                        StringUtils.length(new String(message.getBody())), used, retryCnt);
            }
        }
        return result;
    }
}