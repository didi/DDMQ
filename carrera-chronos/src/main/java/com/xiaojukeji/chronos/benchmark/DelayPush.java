package com.xiaojukeji.chronos.benchmark;

import com.google.common.util.concurrent.RateLimiter;
import com.xiaojukeji.carrera.chronos.enums.MsgTypes;
import com.xiaojukeji.carrera.config.CarreraConfig;
import com.xiaojukeji.carrera.producer.AddDelayMessageBuilder;
import com.xiaojukeji.carrera.producer.CarreraProducer;
import com.xiaojukeji.carrera.producer.CarreraReturnCode;
import com.xiaojukeji.carrera.thrift.DelayMeta;
import com.xiaojukeji.carrera.thrift.DelayResult;
import com.xiaojukeji.carrera.utils.ConfigUtils;
import com.xiaojukeji.chronos.benchmark.config.BenchmarkConfig;
import com.xiaojukeji.chronos.benchmark.config.PushConfig;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;


public class DelayPush {
    private static final Logger LOGGER = LoggerFactory.getLogger(DelayPush.class);

    private static PushConfig pushConfig;
    private static volatile boolean stop = false;
    private static AtomicInteger counter = new AtomicInteger(0);
    private static long currentTimeSecond;
    private static CarreraProducer producer;
    private static ExecutorService executor;
    private static final ScheduledExecutorService scheduler = new ScheduledThreadPoolExecutor(1);
    private static RateLimiter rateLimiter;
    private static Random rnd = new Random();
    private static String RANDOM_STR;


    public static void main(String[] args) throws Exception {
        if (args.length > 1) {
            LOGGER.error("too many args. only need one.");
            return;
        }

        LOGGER.info("benchmark push start");

        init(args[0]);

        RANDOM_STR = RandomStringUtils.randomAlphanumeric(pushConfig.getMsgBodyLen());
        final long start = System.currentTimeMillis();
        sendMsg();
        LOGGER.info("benchmark push over, cost:{}ms", System.currentTimeMillis() - start);
    }

    public static void sendMsg() {
        do {
            currentTimeSecond = System.currentTimeMillis() / 1000;
            final long start = System.currentTimeMillis();
            CountDownLatch cdl = new CountDownLatch(pushConfig.getThreadNum());
            for (int i = 0; i < pushConfig.getThreadNum(); i++) {
                executor.execute(() -> {
                    rateLimiter.acquire(); // may wait
                    realSend();
                    if (pushConfig.getSleepMsPerLoop() != 0) {
                        try {
                            Thread.sleep(pushConfig.getSleepMsPerLoop());
                        } catch (InterruptedException e) {
                        }
                    }
                    cdl.countDown();
                });
            }
            counter.set(0);
            try {
                cdl.await();
            } catch (InterruptedException e) {
            }
            LOGGER.info("total send delay msg, counter:{}, cost:{}", counter.get(), System.currentTimeMillis() - start);
        } while (pushConfig.isSendLoop());

        producer.shutdown();
    }

    private static void realSend() {
        for (int i = 0; i < pushConfig.getMsgSentCountPerThread(); i++) {
            if (stop) {
                LOGGER.info("time is up, stop send message to pproxy");
                break;
            }

            DelayMeta delayMeta = new DelayMeta();
            if (pushConfig.getAllDelayTimeSecond() != 0) {
                delayMeta.setTimestamp(currentTimeSecond + pushConfig.getBaseDelayTimeSecond() + pushConfig.getAllDelayTimeSecond());
            } else {
                delayMeta.setTimestamp((System.currentTimeMillis() / 1000 + pushConfig.getBaseDelayTimeSecond() + rnd.nextInt(pushConfig.getPerDelayTimeSecond())));
            }
            delayMeta.setExpire(System.currentTimeMillis() / 1000 + pushConfig.getBaseDelayTimeSecond() + pushConfig.getExpireSecond());
            delayMeta.setDmsgtype(MsgTypes.DELAY.getValue());
            String body = RANDOM_STR + UUID.randomUUID();
            String topic = pushConfig.getTopic();

//            final DelayResult result = producer.sendDelay(topic, body, delayMeta);
            AddDelayMessageBuilder addDelayMessageBuilder = producer.addDelayMessageBuilder()
                    .setTopic(topic)
                    .setBody(body)
                    .setDelayMeta(delayMeta)
                    .setTraceId("traceid" + System.currentTimeMillis())
                    .setSpanId("spanid" + System.currentTimeMillis())
                    .addHeader("header_key_0", "header_value_0")
                    .addHeader("header_key_1", "header_value_1")
                    .addHeader("hintCode", "99")
                    .addHeader("hintContent", "{\"sample\":{\"code\":88}}")
                    .addProperty("property_key_0", "property_value_0")
                    .addProperty("property_key_1", "property_value_1")
                    .setPressureTraffic(false);

            final DelayResult result = addDelayMessageBuilder.send();

            if (result.getCode() == CarreraReturnCode.OK) {
                counter.incrementAndGet();
                LOGGER.info("succ send, delayMsgId:{}, bodyLen:{}", result.getUniqDelayMsgId(), body.length());
            } else {
                LOGGER.error("error while send, body:{}", body);
            }

            if (pushConfig.getSleepMsPerMsg() != 0) {
                try {
                    Thread.sleep(pushConfig.getSleepMsPerMsg());
                } catch (InterruptedException e) {
                }
            }
        }
    }

    private static void init(final String configPath) throws Exception {
        pushConfig = ConfigUtils.newConfig(configPath, BenchmarkConfig.class).getPushConfig();
        pushConfig.setQpsLimit(pushConfig.getQpsLimit() == 0 ? 400000 : pushConfig.getQpsLimit());
        pushConfig.setRunTimeMinute(pushConfig.getRunTimeMinute() == 0 ? Integer.MAX_VALUE : pushConfig.getRunTimeMinute());

        CarreraConfig carreraConfig = new CarreraConfig();
        carreraConfig.setCarreraProxyList(pushConfig.getPproxyAddrs());
        carreraConfig.setCarreraProxyTimeout(pushConfig.getProxyTimeoutMs());
        carreraConfig.setCarreraClientRetry(pushConfig.getClientRetry());
        carreraConfig.setCarreraClientTimeout(pushConfig.getClientTimeoutMs());
        carreraConfig.setCarreraPoolSize(pushConfig.getPoolSize());

        producer = new CarreraProducer(carreraConfig);
        producer.start();

        rateLimiter = RateLimiter.create(pushConfig.getQpsLimit());

        executor = Executors.newFixedThreadPool(pushConfig.getThreadNum());

        scheduler.schedule(() -> {
            stop = true;
            executor.shutdown();
        }, pushConfig.getRunTimeMinute(), TimeUnit.MINUTES);
    }
}