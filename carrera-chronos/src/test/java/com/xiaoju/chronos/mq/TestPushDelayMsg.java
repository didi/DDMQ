package com.xiaoju.chronos.mq;

import com.google.common.collect.Lists;
import com.xiaojukeji.carrera.chronos.enums.MsgTypes;
import com.xiaojukeji.carrera.chronos.model.InternalKey;
import com.xiaojukeji.carrera.config.CarreraConfig;
import com.xiaojukeji.carrera.producer.AddDelayMessageBuilder;
import com.xiaojukeji.carrera.producer.CarreraProducer;
import com.xiaojukeji.carrera.producer.CarreraReturnCode;
import com.xiaojukeji.carrera.thrift.DelayMeta;
import com.xiaojukeji.carrera.thrift.DelayResult;
import org.apache.logging.log4j.LogManager;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;


public class TestPushDelayMsg {
    private static Logger LOGGER;
    private static AtomicInteger counter = new AtomicInteger(0);
    private static CarreraProducer producer = null;
//    private static final String TOPIC = "chronos_consume_offset";
    private static final String TOPIC = "chronos_test";

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

    @BeforeClass
    public static void setLogger() throws MalformedURLException
    {
        System.setProperty("log4j.configurationFile","log4j2-test.xml");
        LOGGER = LoggerFactory.getLogger("PushLogger");
    }

    // 针对所有测试，只执行一次，且必须为static void
    @AfterClass
    public static void shutdownProducer() {
        System.out.println("producer shutdown...");
        producer.shutdown();
        LogManager.shutdown();
    }

    @Test
    public void testSingleAddDelay() {
        long timestamp = System.currentTimeMillis() / 1000 + 5;
        addDelay(MsgTypes.DELAY.getValue(), timestamp);
    }

    @Test
    public void testSingleAddLoop() {
        long timestamp = System.currentTimeMillis() / 1000 + 10;
        addDelay(MsgTypes.LOOP_DELAY.getValue(), timestamp);
    }

    @Test
    public void testSingleAddLoopExp() {
        long timestamp = System.currentTimeMillis() / 1000 + 10;
        addDelay(MsgTypes.LOOP_EXPONENT_DELAY.getValue(), timestamp);
    }

    @Test
    public void testSingleAddThenCancelDelay() {
        final long timestamp = System.currentTimeMillis() / 1000 + 60;
        final Map<ChronosStat, String> chronosStatMap = new ConcurrentHashMap();
        chronosStatMap.put(addDelay(MsgTypes.DELAY.getValue(), timestamp), "");

        int count = 0;
        for (Map.Entry<ChronosStat, String> entry : chronosStatMap.entrySet()) {
            cancelDelayWithOriginDelayMsgId(entry.getKey(), count++);
        }
    }

    @Test
    public void testSingleAddThenCancelLoopWithOriginDelayMsgId() throws InterruptedException {
        final long timestamp = System.currentTimeMillis() / 1000 + 10;
        final Map<ChronosStat, String> chronosStatMap = new ConcurrentHashMap();
        chronosStatMap.put(addDelay(MsgTypes.LOOP_DELAY.getValue(), timestamp), "");

        TimeUnit.SECONDS.sleep(20);

        int count = 0;
        for (Map.Entry<ChronosStat, String> entry : chronosStatMap.entrySet()) {
            cancelDelayWithOriginDelayMsgId(entry.getKey(), count++);
        }
    }

    @Test
    public void testSingleAddThenCancelLoopWithNextDelayMsgId() throws InterruptedException {
        final long timestamp = System.currentTimeMillis() / 1000 + 10;
        final Map<ChronosStat, String> chronosStatMap = new ConcurrentHashMap();
        chronosStatMap.put(addDelay(MsgTypes.LOOP_DELAY.getValue(), timestamp), "");

        TimeUnit.SECONDS.sleep(20);

        int count = 0;
        for (Map.Entry<ChronosStat, String> entry : chronosStatMap.entrySet()) {
            cancelDelayWithNextDelayMsgId(entry.getKey(), count++);
        }
    }

    @Test
    public void testSingleAddThenCancelLoopExpWithOriginDelayMsgId() throws InterruptedException {
        final long timestamp = System.currentTimeMillis() / 1000 + 10;
        final Map<ChronosStat, String> chronosStatMap = new ConcurrentHashMap();
        chronosStatMap.put(addDelay(MsgTypes.LOOP_EXPONENT_DELAY.getValue(), timestamp), "");

        TimeUnit.SECONDS.sleep(20);

        int count = 0;
        for (Map.Entry<ChronosStat, String> entry : chronosStatMap.entrySet()) {
            cancelDelayWithOriginDelayMsgId(entry.getKey(), count++);
        }
    }

    @Test
    public void testSingleAddThenCancelLoopExpWithNextDelayMsgId() throws InterruptedException {
        final long timestamp = System.currentTimeMillis() / 1000 + 10;
        final Map<ChronosStat, String> chronosStatMap = new ConcurrentHashMap();
        chronosStatMap.put(addDelay(MsgTypes.LOOP_EXPONENT_DELAY.getValue(), timestamp), "");

        TimeUnit.SECONDS.sleep(30);

        int count = 0;
        for (Map.Entry<ChronosStat, String> entry : chronosStatMap.entrySet()) {
            cancelDelayWithNextDelayMsgId(entry.getKey(), count++);
        }
    }

    @Test
    public void testSingleRepeatCancelLoopWithOriginDelayMsgId() throws InterruptedException {
        final long delaySeconds = 10;
        final long timestamp = System.currentTimeMillis() / 1000 + delaySeconds;
        final Map<ChronosStat, String> chronosStatMap = new ConcurrentHashMap();
        chronosStatMap.put(addDelay(MsgTypes.LOOP_DELAY.getValue(), timestamp), "");

        int count = 0;
        for (Map.Entry<ChronosStat, String> entry : chronosStatMap.entrySet()) {
            cancelDelayWithOriginDelayMsgId(entry.getKey(), count++);

            TimeUnit.SECONDS.sleep(delaySeconds + 10);

            cancelDelayWithOriginDelayMsgId(entry.getKey(), count++);
        }
    }

    @Test
    public void testBatchAddDelay() throws InterruptedException {
        final int threadNum = 20;
        final int msgNumPerThread = 500;
        final CountDownLatch latch = new CountDownLatch(threadNum);
        final long timestamp = System.currentTimeMillis() / 1000 + 120;
        for (int i = 0; i < threadNum; i++) {
            new Thread(() -> {
                for (int j = 0; j < msgNumPerThread; j++) {
                    addDelay(MsgTypes.DELAY.getValue(), timestamp);
                    try {
                        TimeUnit.MILLISECONDS.sleep(20);
                    } catch (InterruptedException e) {
                    }
                }
                latch.countDown();
            }).start();
        }
        latch.await();
    }

    // batch add
    @Test
    public void testBatchAddLoop() throws InterruptedException {
        final int threadNum = 20;
        final int msgNumPerThread = 500;
        final CountDownLatch latch = new CountDownLatch(threadNum);
        final long timestamp = System.currentTimeMillis() / 1000 + 120;
        for (int i = 0; i < threadNum; i++) {
            new Thread(() -> {
                for (int j = 0; j < msgNumPerThread; j++) {
                    addDelay(MsgTypes.LOOP_DELAY.getValue(), timestamp);
                    try {
                        TimeUnit.MILLISECONDS.sleep(20);
                    } catch (InterruptedException e) {
                    }
                }
                latch.countDown();
            }).start();
        }
        latch.await();
    }

    @Test
    public void testBatchAddThenCancelDelayWithOriginDelayMsgId() throws InterruptedException {
        final int threadNum = 20;
        final int msgNumPerThread = 500;
        final CountDownLatch latch = new CountDownLatch(threadNum);
        final long timestamp = System.currentTimeMillis() / 1000 + 120;
        final Map<ChronosStat, String> chronosStatMap = new ConcurrentHashMap();
        for (int i = 0; i < threadNum; i++) {
            new Thread(() -> {
                for (int j = 0; j < msgNumPerThread; j++) {
                    chronosStatMap.put(addDelay(MsgTypes.DELAY.getValue(), timestamp), "");
                }
                latch.countDown();
            }).start();
        }

        TimeUnit.SECONDS.sleep(5);

        int count = 0;
        for (Map.Entry<ChronosStat, String> entry : chronosStatMap.entrySet()) {
            cancelDelayWithOriginDelayMsgId(entry.getKey(), count++);
        }

        latch.await();
    }

    @Test
    public void testBatchAddThenCancelLoopWithOriginDelayMsgId() throws InterruptedException {
        final int threadNum = 20;
        final int msgNumPerThread = 500;
        final CountDownLatch latch = new CountDownLatch(threadNum);
        final long timestamp = System.currentTimeMillis() / 1000 + 60;
        final Map<ChronosStat, String> chronosStatMap = new ConcurrentHashMap();
        for (int i = 0; i < threadNum; i++) {
            new Thread(() -> {
                for (int j = 0; j < msgNumPerThread; j++) {
                    chronosStatMap.put(addDelay(MsgTypes.LOOP_DELAY.getValue(), timestamp), "");
                }
                latch.countDown();
            }).start();
        }

        TimeUnit.SECONDS.sleep(5);

        int count = 0;
        for (Map.Entry<ChronosStat, String> entry : chronosStatMap.entrySet()) {
            cancelDelayWithOriginDelayMsgId(entry.getKey(), count++);
        }

        latch.await();
    }

    /**
     * 测试在chronos消息发送期间停止并启动chronos是否有丢消息的情况发生
     *
     * @throws InterruptedException
     */
    @Test public void testStopStartGracefully() throws InterruptedException {
        final int threadNum = 20;
        final int msgNumPerThread = 500;
        final long timestamp = System.currentTimeMillis() / 1000 + 1200;

        for (int round = 0; round < 60; round++) {
            final CountDownLatch latch = new CountDownLatch(threadNum);
            final long roundTimestamp = timestamp + round;
            for (int i = 0; i < threadNum; i++) {
                new Thread(() -> {
                    for (int j = 0; j < msgNumPerThread; j++) {
                        addDelay(MsgTypes.DELAY.getValue(), roundTimestamp);
                        try {
                            TimeUnit.MILLISECONDS.sleep(20);
                        } catch (InterruptedException e) {
                        }
                    }
                    latch.countDown();
                }).start();
            }
            latch.await();
        }
        LOGGER.info("total push message count:{}", counter.get());
        System.out.println("total push message count:" + counter.get());
    }

    //------------------------------------------------------------------------------------------------------------------//

    private ChronosStat addDelay(final int msgType, final long timestamp) {
        DelayMeta delayMeta = new DelayMeta();
        delayMeta.setTimestamp(timestamp);
        delayMeta.setExpire(timestamp + 100000);

        if (msgType == MsgTypes.LOOP_DELAY.getValue()) {
            delayMeta.setDmsgtype(MsgTypes.LOOP_DELAY.getValue());
            delayMeta.setInterval(5);
            delayMeta.setTimes(30);
        } else if (msgType == MsgTypes.LOOP_EXPONENT_DELAY.getValue()) {
            delayMeta.setDmsgtype(MsgTypes.LOOP_EXPONENT_DELAY.getValue());
            delayMeta.setInterval(2);
            delayMeta.setTimes(10);
        } else {
            delayMeta.setDmsgtype(MsgTypes.DELAY.getValue());
        }

        int count = counter.getAndIncrement();

//        final String body = "test send delay message-" + count;
//        final String body = "Jeff曾无奈写过一次O(n^2)的算法，其解决的问题是旅行商问题。（该问题是NPC的，即计算机中最复杂最难解决的一类问题，许多人相信这些问题是没有多项式时间复杂度的解的）Jeff曾无奈写过一次O(n^2)的算法，其解决的问题是旅行商问题。（该问题是NPC的，即计算机中最复杂最难解决的一类问题，许多人相信这些问题是没有多项式时间复杂度的解的）Jeff曾无奈写过一次O(n^2)的算法，其解决的问题是旅行商问题。（该问题是NPC的，即计算机中最复杂最难解决的一类问题，许多人相信这些问题是没有多项式时间复杂度的解的）Jeff曾无奈写过一次O(n^2)的算法，其解决的问题是旅行商问题。（该问题是NPC的，即计算机中最复杂最难解决的一类问题，许多人相信这些问题是没有多项式时间复杂度的解的）Jeff曾无奈写过一次O(n^2)的算法，其解决的问题是旅行商问题。（该问题是NPC的，即计算机中最复杂最难解决的一类问题，许多人相信这些问题是没有多项式时间复杂度的解的）Jeff曾无奈写过一次O(n^2)的算法，其解决的问题是旅行商问题。（该问题是NPC的，即计算机中最复杂最难解决的一类问题，许多人相信这些问题是没有多项式时间复杂度的解的）Jeff曾无奈写过一次O(n^2)的算法，其解决的问题是旅行商问题。（该问题是NPC的，即计算机中最复杂最难解决的一类问题，许多人相信这些问题是没有多项式时间复杂度的解的）Jeff曾无奈写过一次O(n^2)的算法，其解决的问题是旅行商问题。（该问题是NPC的，即计算机中最复杂最难解决的一类问题，许多人相信这些问题是没有多项式时间复杂度的解的）Jeff曾无奈写过一次O(n^2)的算法，其解决的问题是旅行商问题。（该问题是NPC的，即计算机中最复杂最难解决的一类问题，许多人相信这些问题是没有多项式时间复杂度的解的）Jeff曾无奈写过一次O(n^2)的算法，其解决的问题是旅行商问题。（该问题是NPC的，即计算机中最复杂最难解决的一类问题，许多人相信这些问题是没有多项式时间复杂度的解的）Jeff曾无奈写过一次O(n^2)的算法，其解决的问题是旅行商问题。（该问题是NPC的，即计算机中最复杂最难解决的一类问题，许多人相信这些问题是没有多项式时间复杂度的解的）Jeff曾无奈写过一次O(n^2)的算法，其解决的问题是旅行商问题。（该问题是NPC的，即计算机中最复杂最难解决的一类问题，许多人相信这些问题是没有多项式时间复杂度的解的）Jeff曾无奈写过一次O(n^2)的算法，其解决的问题是旅行商问题。（该问题是NPC的，即计算机中最复杂最难解决的一类问题，许多人相信这些问题是没有多项式时间复杂度的解的）Jeff曾无奈写过一次O(n^2)的算法，其解决的问题是旅行商问题。（该问题是NPC的，即计算机中最复杂最难解决的一类问题，许多人相信这些问题是没有多项式时间复杂度的解的）Jeff曾无奈写过一次O(n^2)的算法，其解决的问题是旅行商问题。（该问题是NPC的，即计算机中最复杂最难解决的一类问题，许多人相信这些问题是没有多项式时间复杂度的解的）Jeff曾无奈写过一次O(n^2)的算法，其解决的问题是旅行商问题。（该问题是NPC的，即计算机中最复杂最难解决的一类问题，许多人相信这些问题是没有多项式时间复杂度的解的）Jeff曾无奈写过一次O(n^2)的算法，其解决的问题是旅行商问题。（该问题是NPC的，即计算机中最复杂最难解决的一类问题，许多人相信这些问题是没有多项式时间复杂度的解的）Jeff曾无奈写过一次O(n^2)的算法，其解决的问题是旅行商问题。（该问题是NPC的，即计算机中最复杂最难解决的一类问题，许多人相信这些问题是没有多项式时间复杂度的解的）Jeff曾无奈写过一次O(n^2)的算法，其解决的问题是旅行商问题。（该问题是NPC的，即计算机中最复杂最难解决的一类问题，许多人相信这些问题是没有多项式时间复杂度的解的）Jeff曾无奈写过一次O(n^2)的算法，其解决的问题是旅行商问题。（该问题是NPC的，即计算机中最复杂最难解决的一类问题，许多人相信这些问题是没有多项式时间复杂度的解的）Jeff曾无奈写过一次O(n^2)的算法，其解决的问题是旅行商问题。（该问题是NPC的，即计算机中最复杂最难解决的一类问题，许多人相信这些问题是没有多项式时间复杂度的解的）Jeff曾无奈写过一次O(n^2)的算法，其解决的问题是旅行商问题。（该问题是NPC的，即计算机中最复杂最难解决的一类问题，许多人相信这些问题是没有多项式时间复杂度的解的）Jeff曾无奈写过一次O(n^2)的算法，其解决的问题是旅行商问题。（该问题是NPC的，即计算机中最复杂最难解决的一类问题，许多人相信这些问题是没有多项式时间复杂度的解的）Jeff曾无奈写过一次O(n^2)的算法，其解决的问题是旅行商问题。（该问题是NPC的，即计算机中最复杂最难解决的一类问题，许多人相信这些问题是没有多项式时间复杂度的解的）Jeff曾无奈写过一次O(n^2)的算法，其解决的问题是旅行商问题。（该问题是NPC的，即计算机中最复杂最难解决的一类问题，许多人相信这些问题是没有多项式时间复杂度的解的）Jeff曾无奈写过一次O(n^2)的算法，其解决的问题是旅行商问题。（该问题是NPC的，即计算机中最复杂最难解决的一类问题，许多人相信这些问题是没有多项式时间复杂度的解的）Jeff曾无奈写过一次O(n^2)的算法，其解决的问题是旅行商问题。（该问题是NPC的，即计算机中最复杂最难解决的一类问题，许多人相信这些问题是没有多项式时间复杂度的解的）Jeff曾无奈写过一次O(n^2)的算法，其解决的问题是旅行商问题。（该问题是NPC的，即计算机中最复杂最难解决的一类问题，许多人相信这些问题是没有多项式时间复杂度的解的）Jeff曾无奈写过一次O(n^2)的算法，其解决的问题是旅行商问题。（该问题是NPC的，即计算机中最复杂最难解决的一类问题，许多人相信这些问题是没有多项式时间复杂度的解的）Jeff曾无奈写过一次O(n^2)的算法，其解决的问题是旅行商问题。（该问题是NPC的，即计算机中最复杂最难解决的一类问题，许多人相信这些问题是没有多项式时间复杂度的解的）Jeff曾无奈写过一次O(n^2)的算法，其解决的问题是旅行商问题。（该问题是NPC的，即计算机中最复杂最难解决的一类问题，许多人相信这些问题是没有多项式时间复杂度的解的）Jeff曾无奈写过一次O(n^2)的算法，其解决的问题是旅行商问题。（该问题是NPC的，即计算机中最复杂最难解决的一类问题，许多人相信这些问题是没有多项式时间复杂度的解的）Jeff曾无奈写过一次O(n^2)的算法，其解决的问题是旅行商问题。（该问题是NPC的，即计算机中最复杂最难解决的一类问题，许多人相信这些问题是没有多项式时间复杂度的解的）";
        final String body = "test momomo";
//        final String body = "{\"phone\":\"13507084228\",\"from\":\"\\u9752\\u4e91\\u8c31\\u533a.\\u4e95\\u5188\\u5c71.\\u4e95\\u5188\\u5c71\\u5927\\u9053\\u5f90\\u574a\\u5ba2\\u8fd0\\u7ad9-\\u516c\\u4ea4\\u7ad9\\u5bf9\\u9762\",\"to\":\"\\u897f\\u6e56\\u533a\\u6d2a\\u57ce\\u8def\\u5609\\u7965\\u5927\\u53a6\",\"driverId\":\"566111928197975\",\"orderId\":\"6005064092\"}";
//        final String body = "{\"sid\":\"dache\",\"orderInfo\":\"{\\\"OrderId\\\":73512236891043,\\\"ArrOrderIds\\\":null,\\\"OrderStatus\\\":0,\\\"OrderType\\\":0,\\\"CityId\\\":24,\\\"CompetitionStartTime\\\":0,\\\"Extra\\\":{\\\"area\\\":\\\"24\\\",\\\"road_dis\\\":\\\"3166\\\"},\\\"AuctionTimes\\\":0}\",\"driverInfo\":\"{\\\"DriverId\\\":3437483,\\\"Name\\\":\\\"高师傅\\\",\\\"License\\\":\\\"湘AT****\\\",\\\"PkValue\\\":-901,\\\"PkType\\\":34,\\\"ApiPkType\\\":0,\\\"Phone\\\":\\\"\\\",\\\"TimeStamp\\\":1482740934026,\\\"IsParticipate\\\":1,\\\"IsTopOne\\\":false,\\\"PullType\\\":0,\\\"PullToken\\\":\\\"\\\",\\\"BroadcastTimeStamp\\\":1482740930203,\\\"Extra\\\":{\\\"ApiDriverInfo\\\":\\\"{\\\\\\\"name\\\\\\\":\\\\\\\"\\\\\\\\u9ad8\\\\\\\\u5e08\\\\\\\\u5085\\\\\\\",\\\\\\\"license\\\\\\\":\\\\\\\"\\\\\\\\u6e58AT****\\\\\\\",\\\\\\\"distance\\\\\\\":934,\\\\\\\"distance2\\\\\\\":0,\\\\\\\"lng\\\\\\\":false,\\\\\\\"lat\\\\\\\":false,\\\\\\\"model\\\\\\\":\\\\\\\"Che2-UL00\\\\\\\",\\\\\\\"imei\\\\\\\":\\\\\\\"867469023019812\\\\\\\",\\\\\\\"uuid\\\\\\\":\\\\\\\"89C3A2F4C84F58FAD8978F6A88415DC0\\\\\\\",\\\\\\\"openid\\\\\\\":false,\\\\\\\"suuid\\\\\\\":false,\\\\\\\"appversion\\\\\\\":\\\\\\\"2.9.992\\\\\\\",\\\\\\\"datatype\\\\\\\":\\\\\\\"2\\\\\\\",\\\\\\\"ip\\\\\\\":\\\\\\\"127.0.0.1\\\\\\\",\\\\\\\"callbackurl\\\\\\\":\\\\\\\"default\\\\\\\",\\\\\\\"extra\\\\\\\":{\\\\\\\"get\\\\\\\":{\\\\\\\"tip\\\\\\\":0,\\\\\\\"key\\\\\\\":\\\\\\\"STG_SENDINFO_73512236891043_3437483_1\\\\\\\",\\\\\\\"appversion\\\\\\\":\\\\\\\"2.9.992\\\\\\\",\\\\\\\"datatype\\\\\\\":\\\\\\\"2\\\\\\\",\\\\\\\"dianzhao\\\\\\\":false,\\\\\\\"tip2\\\\\\\":0,\\\\\\\"oid\\\\\\\":73512236891043,\\\\\\\"oid2\\\\\\\":0,\\\\\\\"broadcastid\\\\\\\":\\\\\\\"73512236891043\\\\\\\",\\\\\\\"seqoid1\\\\\\\":false,\\\\\\\"seqoid2\\\\\\\":false,\\\\\\\"assign_type\\\\\\\":1}}}\\\",\\\"DriverType\\\":\\\"0\\\",\\\"area\\\":\\\"24\\\",\\\"assign_type\\\":\\\"1\\\",\\\"driver_grade\\\":\\\"10\\\"}}\"}";
        final String tags = "add-delay-trace-" + count;

        AddDelayMessageBuilder addDelayMessageBuilder = producer.addDelayMessageBuilder()
                .setTopic(TOPIC)
                .setBody(body)
                .setTags(tags)
                .setDelayMeta(delayMeta)
                .setTraceId("6445cb0b5a8fbaed754aa04c8dffb802")
                .setSpanId("1519368941752244391")
                .addProperty("property_1", "property_1")
                .addHeader("didi-header-hint-code", "hhh")
                .addHeader("didi-header-hint-content", "iii");

        DelayResult result;
        while (!succSend(result = addDelayMessageBuilder.send(), count)) {
            try {
                TimeUnit.MILLISECONDS.sleep(100);
            } catch (InterruptedException e) {
            }
        }

        return new ChronosStat(result.getUniqDelayMsgId(), count);
    }

    private void cancelDelayWithOriginDelayMsgId(ChronosStat chronosStat, final int count) {
        final String[] tags = {"chronos-cancel-1", "chronos-cancel-2"};

        while (!succCancel(producer.cancelDelay(TOPIC, new InternalKey(chronosStat.delayMsgId).genUniqDelayMsgId(), tags), count)) {
            try {
                TimeUnit.MILLISECONDS.sleep(100);
            } catch (InterruptedException e) {
            }
        }
    }

    private void cancelDelayWithNextDelayMsgId(ChronosStat chronosStat, final int count) {
        final String[] tags = {"chronos-cancel-1", "chronos-cancel-2"};

        while (!succCancel(producer.cancelDelay(TOPIC, new InternalKey(chronosStat.delayMsgId).nextUniqDelayMsgId().nextUniqDelayMsgId().genUniqDelayMsgId(), tags), count)) {
            try {
                TimeUnit.MILLISECONDS.sleep(100);
            } catch (InterruptedException e) {
            }
        }
    }

    private boolean succSend(DelayResult result, int count) {
        if (result == null) {
            return false;
        }

        if (result.getCode() == CarreraReturnCode.OK) {
            LOGGER.info("succ push message, count:{}, code:{}, msg:{}, delayMsgId:{}", count, result.getCode(), result.getMsg(), result.getUniqDelayMsgId());
            return true;
        }

        return false;
    }

    private boolean succCancel(DelayResult result, int count) {
        if (result == null) {
            return false;
        }

        if (result.getCode() == CarreraReturnCode.OK) {
            LOGGER.info("succ cancel message count:{}, code:{}, msg:{}, delayMsgId:{}", count, result.getCode(), result.getMsg(), result.getUniqDelayMsgId());
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