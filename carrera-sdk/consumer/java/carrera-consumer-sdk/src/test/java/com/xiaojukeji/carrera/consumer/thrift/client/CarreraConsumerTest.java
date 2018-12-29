package com.xiaojukeji.carrera.consumer.thrift.client;

import com.xiaojukeji.carrera.consumer.thrift.Context;
import com.xiaojukeji.carrera.consumer.thrift.FetchResponse;
import com.xiaojukeji.carrera.consumer.thrift.Message;
import com.xiaojukeji.carrera.consumer.thrift.PullResponse;
import com.xiaojukeji.carrera.consumer.thrift.QidResponse;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;


public class CarreraConsumerTest {

    private LowLevelCarreraConsumer lowLevelCarreraConsumer;
    private SimpleCarreraConsumer simpleCarreraConsumer;
    private CarreraConsumer carreraConsumer;
    private LowLevelPoolCarreraConsumer lowLevelPoolCarreraConsumer;

    @Before
    public void setUp() throws Exception {
        LowLevelCarreraConfig config = new LowLevelCarreraConfig("cg_test_kafka2rmq", "127.0.0.1:9713");
        config.setMaxBatchSize(64);
        config.setClusterName("R_test");
        config.setCommitAckInterval(10000);

        lowLevelCarreraConsumer = new LowLevelCarreraConsumer(config);
        simpleCarreraConsumer = new SimpleCarreraConsumer(config);


        carreraConsumer = new CarreraConsumer(config);
        lowLevelPoolCarreraConsumer = new LowLevelPoolCarreraConsumer(config);

    }

    @Test
    public void testLowLevelPoolCarreraConsumerException() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        lowLevelPoolCarreraConsumer.startConsume(new MessageProcessor() {
            @Override
            public Result process(Message message, Context context) {
                System.out.println("receive msg:" + message);
                return Result.SUCCESS;
            }
        }, 10);

        latch.await();
    }

    @Test
    public void testCarreraConsumer() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        carreraConsumer.startConsume(new BatchMessageProcessor() {
            @Override
            public Map<Result, List<Long>> process(List<Message> messages, Context context) {
                System.out.println("receive msg:" + context.toString() + ", size=" + messages.size());
                Map<Result, List<Long>> map  = new HashMap<>();
                List<Long> list = new ArrayList<>();
                for(Message msg : messages) {
                    list.add(msg.getOffset());
                }
                map.put(Result.SUCCESS, list);
                return map;
            }
        }, 10);

        latch.await();
    }


    @Test
    public void testLowLevelCarreraConsumer() throws InterruptedException {
        lowLevelCarreraConsumer.startConsume(new MessageProcessor() {
            @Override
            public Result process(Message message, Context context) {
                System.out.println("receive msg:" + context.toString() + message.toString());

                return Result.SUCCESS;
            }
        });
    }

    @Test
    public void testLowLevelPullMessage() throws InterruptedException{
        CountDownLatch latch = new CountDownLatch(1);
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                for(;;) {
                    FetchResponse resp = lowLevelCarreraConsumer.pullMessage();
                    if(resp == null || resp.getResults() == null || resp.getResults().size() == 0) {

                        try {
                            Thread.sleep(1000L);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        continue;
                    }

                    for(QidResponse qidResponse : resp.getResults()) {
                        System.out.println(qidResponse.toString());
                    }
                }

            }
        });
        thread.start();
        latch.await();
    }

    @Test
    public void testSimpleCarreraConsumer() throws InterruptedException{
        simpleCarreraConsumer.startConsume(new MessageProcessor() {
            @Override
            public Result process(Message message, Context context) {
                System.out.println("receive msg:" + context.toString() + message.toString());

                return Result.SUCCESS;
            }
        });
    }

    @Test
    public void testSimplePullMessage() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                for(;;) {
                    PullResponse resp = simpleCarreraConsumer.pullMessage();
                    if(resp == null || resp.getMessages() == null || resp.getMessages().size() == 0) {
                        try {
                            Thread.sleep(1000L);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        continue;
                    }

                    for(Message message : resp.getMessages()) {
                        System.out.println(message.toString());
                        simpleCarreraConsumer.ack(resp.getContext(), message.getOffset());
                    }
                }

            }
        });
        thread.start();
        latch.await();
    }

    @Test
    public void testShuffle() throws Exception {
        String[] servers = "1,2,3,4".split(",");
        CarreraConfig config = new CarreraConfig("some-group", "111.111.111.1:123;111.111.111.1:123;");
        CarreraConsumer consumer = new CarreraConsumer(config);
        int cnt = 10;
        while (cnt --> 0) {
            //consumer.shuffle(servers, new Random());
            System.out.println(Arrays.toString(servers));
        }

    }
}