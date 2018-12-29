package com.xiaojukeji.carrera.monitor.inspection;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.RateLimiter;
import com.xiaojukeji.carrera.config.CarreraConfig;
import com.xiaojukeji.carrera.consumer.thrift.Context;
import com.xiaojukeji.carrera.consumer.thrift.Message;
import com.xiaojukeji.carrera.consumer.thrift.client.BaseMessageProcessor;
import com.xiaojukeji.carrera.consumer.thrift.client.CarreraConsumer;
import com.xiaojukeji.carrera.consumer.thrift.client.MessageProcessor;
import com.xiaojukeji.carrera.producer.CarreraProducer;
import com.xiaojukeji.carrera.producer.CarreraReturnCode;
import com.xiaojukeji.carrera.thrift.Result;
import com.xiaojukeji.carrera.utils.TimeUtils;
import org.apache.commons.lang.math.RandomUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.common.stats.TimeStats;
import org.slf4j.Logger;

import java.net.InetAddress;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import static org.slf4j.LoggerFactory.getLogger;



public class CarreraDataInspection {

    private static final Logger LOGGER = getLogger(CarreraDataInspection.class);

    private String broker;

    private volatile CarreraProducer producer;

    private volatile CarreraConsumer consumer;

    private ClusterConfig clusterConfig;

    private long startTime = TimeUtils.getCurTime();

    private AtomicInteger dupCnt = new AtomicInteger();

    private AtomicLong msgCnt = new AtomicLong();

    private TimeStats.RolloverTimeStats rtStats = new TimeStats.RolloverTimeStats(50000);

    private ScheduledExecutorService scheduledExecutorService; /* 执行生产消费的线程池 */

    private RateLimiter rateLimiter;

    private ScheduledFuture scheduledFuture; /* log metric periodically */

    private Map<String/* msg key */, com.xiaojukeji.carrera.thrift.Message> messageMap = new ConcurrentHashMap<>(); /* 保存生产的 message */

    private Cache<String, AtomicInteger> messageCache = CacheBuilder.newBuilder().expireAfterAccess(20, TimeUnit.MINUTES).build();

    private static LoadingCache<String, String> host2IPCache = CacheBuilder.newBuilder().maximumSize(1000)
            .expireAfterWrite(20, TimeUnit.MINUTES).build(new CacheLoader<String, String>() {
        @Override
        public String load(String key) throws Exception {
            InetAddress address = InetAddress.getByName(key);
            String hostAddress = address.getHostAddress();
            LOGGER.info("host:{} -> ip:{}", key, hostAddress);
            return hostAddress;
        }
    });

    public ScheduledFuture getScheduledFuture() {
        return scheduledFuture;
    }

    public void setScheduledFuture(ScheduledFuture scheduledFuture) {
        this.scheduledFuture = scheduledFuture;
    }

    public CarreraDataInspection(String broker, ClusterConfig clusterConfig, ScheduledExecutorService scheduledExecutorService) {
        this.broker = broker;
        this.clusterConfig = clusterConfig;
        this.scheduledExecutorService = scheduledExecutorService;
        this.rateLimiter = RateLimiter.create(100);
    }

    public void stop() {
        LOGGER.info("Stop Inspection broker:{}", broker);

        if (producer != null) {
            producer.shutdown();
        }
        producer = null;

        if (consumer != null) {
            consumer.stop();
        }
        consumer = null;

        getScheduledFuture().cancel(true);
        LOGGER.info("Stop Inspection broker:{} done", broker);
    }

    public void start() throws Exception {
        producer = createProducer(clusterConfig);
        consumer = createConsumer(clusterConfig);

        scheduledExecutorService.submit(() -> {
            try {
                LOGGER.info("start consume, broker:{}", broker);
                consumer.startConsume((MessageProcessor) (message, context) -> {
                    processMessage(message, context);
                    return BaseMessageProcessor.Result.SUCCESS;
                }, 1);

                producer.start();
                for (int i = 0; i < clusterConfig.getConcurrentMessages(); i++) {
                    sendMessage();
                }
            } catch (Exception e) {
                LOGGER.error("CarreraDataInspection start exception, broker=" + broker, e);
                stop();
            }
        });
    }

    public void logMetric() {
        LOGGER.info("[METRIC-STAT] broker={}, onFlyMsgCnt={}, dupRate={}",
                broker,
                messageMap.size(),
                1.0 * dupCnt.get() / msgCnt.get());


        LOGGER.info("[METRIC-RT] broker={}, rtStat:{}", broker, rtStats.reportAndReset());
        messageMap.values().forEach(msg -> {
            double t = 1.0 * TimeUtils.getElapseTime(msg.getHashId()) / TimeUnit.MINUTES.toMillis(1);
            if (t > 10) {
                LOGGER.error("[METRIC-ON_FLY_MSG] broker={}, key={}, elapse={}minutes", broker, msg.getKey(), t);
            }
        });
    }

    private void sendMessage() {
        if (producer == null) {
            LOGGER.error("[INVALID_PRODUCER],broker={}, producer is null", broker);
            return;
        }

        rateLimiter.acquire();

        String topic = clusterConfig.getTopic();
        int bodyLen = RandomUtils.nextInt(clusterConfig.getMaxBodyLen()) + 1;
        byte[] body = org.apache.commons.lang3.RandomUtils.nextBytes(bodyLen);

        int keyLen = RandomUtils.nextInt(clusterConfig.getMaxKeyLen()) + 8;
        String key = TimeUtils.getCurTime() + "_" + RandomStringUtils.randomAlphanumeric(keyLen);

        int tagLen = RandomUtils.nextInt(clusterConfig.getMaxTagLen()) + 1;
        String tag = tagLen > 0 ? RandomStringUtils.randomAlphabetic(tagLen) : null;

        com.xiaojukeji.carrera.thrift.Message message = CarreraProducer.buildMessage(topic, -2/* random */, TimeUtils.getCurTime(), body, key, tag);
        messageMap.put(key, message);

        while (true) {
            if (producer == null) {
                LOGGER.error("[INVALID_PRODUCER],broker={}, producer is null", broker);
                return;
            }
            long curTime = TimeUtils.getCurTime();
            Result ret = producer.sendMessage(message);

            if (ret.code == CarreraReturnCode.OK) {
                LOGGER.info("SendMessage success, broker={}, ret={}, cnt={}, sendCost={}", broker, ret, msgCnt.incrementAndGet(), TimeUtils.getElapseTime(curTime));
                return;
            } else if (ret.code == CarreraReturnCode.FAIL_TOPIC_NOT_ALLOWED || ret.code == CarreraReturnCode.FAIL_TOPIC_NOT_EXIST) {
                LOGGER.info("SendMessage failed, broker={}, ret={}, cnt={}", broker, ret, msgCnt.incrementAndGet());
                try {
                    Thread.sleep(60000L);
                } catch (InterruptedException e) {
                    LOGGER.error("Thread.sleep exception", e);
                }
            } else {
                LOGGER.error("SendMessage failed, broker={}, ret={}", broker, ret);
            }
        }
    }

    private void processMessage(Message message, Context context) {
        String key = message.getKey();
        long sendTs;
        try {
            sendTs = Long.parseLong(key.substring(0, key.indexOf('_')));
        } catch (Exception e) {
            LOGGER.error("[INVALID_KEY], broker={}, key={}", broker, key);
            return;
        }
        if (sendTs < startTime) {
            LOGGER.info("[IGNORE_OLD_MSG], broker={}, key={}", broker, message.getKey());
            return;
        }

        long rt = TimeUtils.getElapseTime(sendTs);
        rtStats.add(rt);
        LOGGER.info("process broker:{}, key:{}, value.length:{}, offset:{}, context:{}, RTT={}", broker, message.getKey(),
                message.getValue().length, message.getOffset(), context, rt);

        com.xiaojukeji.carrera.thrift.Message pMessage;
        synchronized (this) {
            pMessage = messageMap.remove(message.getKey());
            if (pMessage == null) {
                AtomicInteger cnt = messageCache.getIfPresent(key);
                if (cnt == null) {
                    LOGGER.error("[WRONG_DATA] unknown message broker={}, key = {}", broker, message.getKey());
                    return;
                } else {
                    LOGGER.warn("[DUPLICATE_DATA] broker={}, key={}, cnt={}, dupTotal={}, msgTotal={}",
                            broker, key, cnt.incrementAndGet(), dupCnt.incrementAndGet(), msgCnt.get());
                    return;
                }
            } else {
                messageCache.put(key, new AtomicInteger(1));
            }
        }

        if (!StringUtils.equals(message.getTag(), pMessage.getTags())) {
            LOGGER.error("[WRONG_DATA] tag not equals, broker={}, key={}, consumeTag={}, sendTag={}",
                    broker, message.getKey(), message.getTag(), pMessage.getTags());
            return;
        }

        if (!Arrays.equals(message.getValue(), pMessage.getBody())) {
            LOGGER.error("[WRONG_DATA] body not equals, broker={}, key={}, consumeValue={}, sendBody={}",
                    broker, message.getKey(), message.getValue(), pMessage.getBody());
            return;
        }

        sendMessage();
    }

    private CarreraConsumer createConsumer(ClusterConfig clusterConfig) {
        com.xiaojukeji.carrera.consumer.thrift.client.CarreraConfig config;
        String servers = clusterConfig.getCproxyServers().stream().map(this::host2IP).filter(s -> StringUtils.isNoneBlank(s)).collect(Collectors.joining(";"));
        LOGGER.info("broker: {}, consumer servers: {}", broker, servers);
        config = new com.xiaojukeji.carrera.consumer.thrift.client.CarreraConfig(clusterConfig.getGroup(), servers);
        config.setRetryInterval(10);
        config.setMaxBatchSize(64);
        config.setMaxLingerTime(50);
        config.setTimeout(10000);

        return new CarreraConsumer(config);
    }

    private CarreraProducer createProducer(ClusterConfig conf) {
        CarreraConfig config = new CarreraConfig();
        List<String> servers = conf.getPproxyServers().stream().map(this::host2IP).filter(s -> StringUtils.isNoneBlank(s)).collect(Collectors.toList());
        LOGGER.info("broker: {}, producer servers:{}", broker, servers);
        config.setCarreraProxyList(servers);
        config.setCarreraProxyTimeout(100);
        config.setCarreraClientRetry(2);
        config.setCarreraClientTimeout(1000);
        config.setCarreraPoolSize(5);
        return new CarreraProducer(config);
    }

    private String host2IP(String host) {
        try {
            String[] split = host.split(":");
            return host2IPCache.get(split[0]) + ":" + split[1];
        } catch (ExecutionException e) {
            LOGGER.error("host -> ip error, host: {}", host);
            return "";
        }
    }
}
