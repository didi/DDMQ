package com.xiaojukeji.carrera.producer;

import com.xiaojukeji.carrera.config.CarreraConfig;
import com.xiaojukeji.carrera.thrift.Message;
import com.xiaojukeji.carrera.thrift.Result;
import com.xiaojukeji.carrera.utils.TimeUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class CarreraProducerTest {
    CarreraProducer producer;

    @Before
    public void setUp() throws Exception {
        CarreraConfig config = CarreraConfig.NewDefaultConfigForTestEnv();
        producer = CarreraProducer.newCarreraProducer(config);
        producer.start();
    }

    @After
    public void destroy() {
        producer.shutdown();
    }

    @Test
    public void sendBatchMessagesConcurrently() throws Exception {
        List<Message> msgs = mockMessageList(100);

        long start = TimeUtils.getCurTime();
        Result result = producer.sendBatchConcurrently(msgs);
        System.out.println(result);
        System.out.println("concurrently:" + TimeUtils.getElapseTime(start));
        Assert.assertTrue(result.getCode() == CarreraReturnCode.OK);
    }

    private List<Message> mockMessageList(int cnt) {
        List<Message> msgs = new ArrayList<>();
        for (int i = 0; i < cnt; i++) {
            msgs.add(CarreraProducer.buildMessage("test-0",
                    CarreraConfig.PARTITION_HASH, i % 5, "test-body".getBytes(), "some-key"));
        }
        return msgs;
    }

    @Test
    public void sendBatchMessagesOrderly() throws Exception {
        List<Message> msgs = mockMessageList(100);
        long start = TimeUtils.getCurTime();
        Result result = producer.sendBatchOrderly(msgs);
        System.out.println(result);
        System.out.println("orderly:" + TimeUtils.getElapseTime(start));
        Assert.assertTrue(result.getCode() == CarreraReturnCode.OK);
    }

    @Test
    public void sendMessageConcurrently() throws Exception {
        int concurrency = 200;
        ExecutorService executorService = Executors.newFixedThreadPool(concurrency);
        final CountDownLatch latch = new CountDownLatch(concurrency);
        long start = TimeUtils.getCurTime();
        for (int idx = 0; idx < concurrency; idx ++) {
            executorService.submit(new Runnable() {
                @Override
                public void run() {
                    for (int count = 0; count < 10; count ++ ) {
                        Result ret = producer.send("test-0", RandomStringUtils.random(128));
                        Assert.assertTrue(ret.getCode() == CarreraReturnCode.OK);
                    }
                    latch.countDown();
                }
            });
        }
        latch.await();
        System.out.println(TimeUtils.getElapseTime(start));
        executorService.shutdown();
    }

    @Test
    public void sendBinaryMessage() {
        Result result = producer.send("test-0", RandomStringUtils.random(128).getBytes());
        Assert.assertTrue(result.getCode() == CarreraReturnCode.OK);
    }

}