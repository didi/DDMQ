package com.xiaojukeji.carrera.consumer.thrift.client;

import com.xiaojukeji.carrera.consumer.thrift.ConsumerService;
import com.xiaojukeji.carrera.consumer.thrift.client.util.VersionUtils;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public abstract class BaseCarreraConsumer<REQ, RES> {
    protected static final Logger LOGGER = LoggerFactory.getLogger(BaseCarreraConsumer.class);
    protected static final long MIN_RETRY_INTERVAL = 5;

    protected final String host;
    protected final int port;
    protected final String topic;
    protected final CarreraConfig config;

    protected volatile boolean isRunning;
    protected volatile ConsumerService.Client client;
    protected REQ request;
    protected TTransport transport;

    private long lastErrorIntervalStart = System.currentTimeMillis();
    private int errorCount = 0;
    private static final long ERROR_IGNORE_INTERVAL_MS = 5000;
    private int ERROR_IGNORE_COUNT;//跟重试间隔和ERROR_IGNORE_INTERVAL_MS相关

    protected String type; // 用于区分上层消费实例的类型，如carrera/storm/jstorm/flink等。

    /**
     * 单线程消费一个consumer proxy的客户端。config.servers只能指定一个server。
     *
     * @param config
     */
    public BaseCarreraConsumer(CarreraConfig config) {
        this(config, null);
    }

    /**
     * 只消费group中指定topic的消息。
     *
     * @param config 消费的配置
     * @param topic  指定消费的topic。null表示不指定。
     */
    public BaseCarreraConsumer(CarreraConfig config, String topic) {
        config.validate(true);
        try {
            String[] tokens = config.getServers().split(":");
            this.host = tokens[0];
            this.port = Integer.parseInt(tokens[1]);
        } catch (Exception e) {
            throw new RuntimeException("server format error, server=" + config.getServers());
        }
        this.topic = topic;
        this.config = config;

        long retryInterval = Math.max(config.getRetryInterval(), MIN_RETRY_INTERVAL);
        ERROR_IGNORE_COUNT = (int) ((1000 / retryInterval) * (Math.max(ERROR_IGNORE_INTERVAL_MS / 1000 - 1, 1)));
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getTopic() {
        return topic;
    }

    public CarreraConfig getConfig() {
        return config;
    }

    private synchronized void init() {
        if (client != null) {
            return;
        }
        TSocket socket = new TSocket(host, port, config.getTimeout());
        transport = new TFramedTransport(socket);
        TProtocol protocol = new TCompactProtocol(transport);
        client = new ConsumerService.Client(protocol);

        initRequest();
    }

    protected abstract void initRequest();

    protected String getVersion() {
        return type + '@' + VersionUtils.getVersion();
    }

    /**
     * This will block current thread!
     *
     * @param processor
     * @throws InterruptedException
     */
    public void startConsume(BaseMessageProcessor processor) throws InterruptedException {
        init();
        isRunning = true;
        LOGGER.info("start consume group:{},server:{},topic:{}", config.getGroupId(), config.getServers(), topic);
        try {
            while (isRunning) {
                RES response = pullMessage();
                if (response == null) { //no new message
                    doRetrySleep();
                } else {
                    processResponse(response, processor);
                }
            }
        } finally {
            close();
        }
        LOGGER.info("consume group[{}] finished!", config.getGroupId());
    }

    public synchronized void close() {
        try {
            doClose();
        } catch (Exception e) {
            LOGGER.error("Exception on close", e);
        } finally {
            if (transport != null && transport.isOpen()) {
                transport.close();
            }
        }
    }

    protected abstract void doClose() throws Exception;

    public synchronized RES pullMessage() {
        try {
            ensureConnection();
            return doPullMessage();
        } catch (TTransportException e) {
            LOGGER.warn("TTransportException, consumer=" + this.toString(), e);
            transport.close();
        } catch (TException e) {
            LOGGER.warn("TException, consumer=" + this.toString(), e);
        }
        return null;
    }

    public boolean isNeedLogError() {
        boolean isTimeout = System.currentTimeMillis() - lastErrorIntervalStart > ERROR_IGNORE_INTERVAL_MS;
        if (!isTimeout && ++errorCount > ERROR_IGNORE_COUNT) {
            lastErrorIntervalStart = System.currentTimeMillis();
            errorCount = 0;
            return true;
        } else if (isTimeout) {
            lastErrorIntervalStart = System.currentTimeMillis();
            errorCount = 0;
        }

        return false;
    }


    protected abstract RES doPullMessage() throws TException;

    protected abstract void doProcessMessage(RES response, MessageProcessor processor);

    protected void doProcessMessage(RES response, BatchMessageProcessor processor) throws InterruptedException {
        throw new InterruptedException("batch processor not implements");
    }

    protected void processResponse(RES response, BaseMessageProcessor processor) throws InterruptedException {
        if (processor instanceof MessageProcessor) {
            doProcessMessage(response, (MessageProcessor) processor);
        } else {
            doProcessMessage(response, (BatchMessageProcessor) processor);
        }
    }

    protected void doRetrySleep() {
        try {
            Thread.sleep(Math.max(config.getRetryInterval(), MIN_RETRY_INTERVAL));
        } catch (InterruptedException e) {
            LOGGER.error("retry sleep InterruptedException", e);
        }
    }

    protected void ensureConnection() throws TTransportException {
        if (client == null) {
            init();
        }
        if (!transport.isOpen()) {
            transport.open();
        }
    }

    public void stop() {
        isRunning = false;
    }

    public boolean isStopped() {
        return !isRunning;
    }

    @Override
    public String toString() {
        return " host='" + host + '\'' +
                ", port=" + port +
                ", topic='" + topic + '\'' +
                ", config=" + config +
                ", type='" + type + '\'';
    }
}