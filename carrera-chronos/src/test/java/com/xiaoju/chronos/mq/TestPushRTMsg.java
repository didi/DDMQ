package com.xiaoju.chronos.mq;

import com.google.common.collect.Lists;
import com.xiaojukeji.carrera.config.CarreraConfig;
import com.xiaojukeji.carrera.producer.CarreraProducer;
import com.xiaojukeji.carrera.producer.CarreraReturnCode;
import com.xiaojukeji.carrera.thrift.Result;
import org.apache.logging.log4j.LogManager;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;


public class TestPushRTMsg {
    private static Logger LOGGER = LoggerFactory.getLogger("PushLogger");
    private static AtomicInteger counter = new AtomicInteger(0);
    private static CarreraProducer producer = null;
    private static final String TOPIC = "chronos_consume_offset";

    // 针对所有测试，只执行一次，且必须为static void
    @BeforeClass
    public static void startProducer() {
        CarreraConfig config = new CarreraConfig();
        config.setCarreraProxyList(Lists.newArrayList("127.0.0.1:9613"));
        config.setCarreraProxyTimeout(1000);
        config.setCarreraClientRetry(3);
        config.setCarreraClientTimeout(1000);
        config.setCarreraPoolSize(20);
        producer = new CarreraProducer(config);
        try {
            System.out.println("producer start...");
            producer.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 针对所有测试，只执行一次，且必须为static void
    @AfterClass
    public static void shutdownProducer() {
        System.out.println("producer shutdown...");
        producer.shutdown();
        LogManager.shutdown();
    }

    @Test
    public void testSingleAddRT() {
        addRT();
    }

    //------------------------------------------------------------------------------------------------------------------//

    private ChronosStat addRT() {
        int count = counter.getAndIncrement();

        final String body = "test send realtime message-" + count;
        final String[] tags = {"add-realtime-trace-" + count};

        Result result;
        while (!succSend(result = producer.send(TOPIC, "hello-joy"), count)) {
            try {
                TimeUnit.MILLISECONDS.sleep(100);
            } catch (InterruptedException e) {
            }
        }

        return new ChronosStat(result.getKey(), count);
    }

    private boolean succSend(Result result, int count) {
        if (result == null) {
            return false;
        }

        if (result.getCode() == CarreraReturnCode.OK) {
            LOGGER.info("succ push message, count:{}, code:{}, msg:{}, delayMsgId:{}", count, result.getCode(), result.getMsg(), result.getKey());
            return true;
        }

        return false;
    }

    class ChronosStat {
        public String delayMsgId;
        public int count;

        public ChronosStat(String delayMsgId, int count) {
            this.delayMsgId = delayMsgId;
            this.count = count;
        }
    }
}