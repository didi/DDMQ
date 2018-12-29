package com.xiaoju.chronos.mq;

import com.xiaojukeji.carrera.consumer.thrift.Message;
import com.xiaojukeji.carrera.consumer.thrift.PullResponse;
import com.xiaojukeji.carrera.consumer.thrift.client.CarreraConfig;
import com.xiaojukeji.carrera.consumer.thrift.client.SimpleCarreraConsumer;
import org.apache.logging.log4j.LogManager;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;


public class TestPullDelayMsg {
    private static Logger LOGGER = LoggerFactory.getLogger("PullLogger");
    private static SimpleCarreraConsumer carreraConsumer = null;
    private static ConcurrentHashMap<String, String> m = new ConcurrentHashMap<>();
    final static String destBody = "Jeff曾无奈写过一次O(n^2)的算法，其解决的问题是旅行商问题。（该问题是NPC的，即计算机中最复杂最难解决的一类问题，许多人相信这些问题是没有多项式时间复杂度的解的）Jeff曾无奈写过一次O(n^2)的算法，其解决的问题是旅行商问题。（该问题是NPC的，即计算机中最复杂最难解决的一类问题，许多人相信这些问题是没有多项式时间复杂度的解的）Jeff曾无奈写过一次O(n^2)的算法，其解决的问题是旅行商问题。（该问题是NPC的，即计算机中最复杂最难解决的一类问题，许多人相信这些问题是没有多项式时间复杂度的解的）Jeff曾无奈写过一次O(n^2)的算法，其解决的问题是旅行商问题。（该问题是NPC的，即计算机中最复杂最难解决的一类问题，许多人相信这些问题是没有多项式时间复杂度的解的）Jeff曾无奈写过一次O(n^2)的算法，其解决的问题是旅行商问题。（该问题是NPC的，即计算机中最复杂最难解决的一类问题，许多人相信这些问题是没有多项式时间复杂度的解的）Jeff曾无奈写过一次O(n^2)的算法，其解决的问题是旅行商问题。（该问题是NPC的，即计算机中最复杂最难解决的一类问题，许多人相信这些问题是没有多项式时间复杂度的解的）Jeff曾无奈写过一次O(n^2)的算法，其解决的问题是旅行商问题。（该问题是NPC的，即计算机中最复杂最难解决的一类问题，许多人相信这些问题是没有多项式时间复杂度的解的）Jeff曾无奈写过一次O(n^2)的算法，其解决的问题是旅行商问题。（该问题是NPC的，即计算机中最复杂最难解决的一类问题，许多人相信这些问题是没有多项式时间复杂度的解的）Jeff曾无奈写过一次O(n^2)的算法，其解决的问题是旅行商问题。（该问题是NPC的，即计算机中最复杂最难解决的一类问题，许多人相信这些问题是没有多项式时间复杂度的解的）Jeff曾无奈写过一次O(n^2)的算法，其解决的问题是旅行商问题。（该问题是NPC的，即计算机中最复杂最难解决的一类问题，许多人相信这些问题是没有多项式时间复杂度的解的）Jeff曾无奈写过一次O(n^2)的算法，其解决的问题是旅行商问题。（该问题是NPC的，即计算机中最复杂最难解决的一类问题，许多人相信这些问题是没有多项式时间复杂度的解的）Jeff曾无奈写过一次O(n^2)的算法，其解决的问题是旅行商问题。（该问题是NPC的，即计算机中最复杂最难解决的一类问题，许多人相信这些问题是没有多项式时间复杂度的解的）Jeff曾无奈写过一次O(n^2)的算法，其解决的问题是旅行商问题。（该问题是NPC的，即计算机中最复杂最难解决的一类问题，许多人相信这些问题是没有多项式时间复杂度的解的）Jeff曾无奈写过一次O(n^2)的算法，其解决的问题是旅行商问题。（该问题是NPC的，即计算机中最复杂最难解决的一类问题，许多人相信这些问题是没有多项式时间复杂度的解的）Jeff曾无奈写过一次O(n^2)的算法，其解决的问题是旅行商问题。（该问题是NPC的，即计算机中最复杂最难解决的一类问题，许多人相信这些问题是没有多项式时间复杂度的解的）Jeff曾无奈写过一次O(n^2)的算法，其解决的问题是旅行商问题。（该问题是NPC的，即计算机中最复杂最难解决的一类问题，许多人相信这些问题是没有多项式时间复杂度的解的）Jeff曾无奈写过一次O(n^2)的算法，其解决的问题是旅行商问题。（该问题是NPC的，即计算机中最复杂最难解决的一类问题，许多人相信这些问题是没有多项式时间复杂度的解的）Jeff曾无奈写过一次O(n^2)的算法，其解决的问题是旅行商问题。（该问题是NPC的，即计算机中最复杂最难解决的一类问题，许多人相信这些问题是没有多项式时间复杂度的解的）Jeff曾无奈写过一次O(n^2)的算法，其解决的问题是旅行商问题。（该问题是NPC的，即计算机中最复杂最难解决的一类问题，许多人相信这些问题是没有多项式时间复杂度的解的）Jeff曾无奈写过一次O(n^2)的算法，其解决的问题是旅行商问题。（该问题是NPC的，即计算机中最复杂最难解决的一类问题，许多人相信这些问题是没有多项式时间复杂度的解的）Jeff曾无奈写过一次O(n^2)的算法，其解决的问题是旅行商问题。（该问题是NPC的，即计算机中最复杂最难解决的一类问题，许多人相信这些问题是没有多项式时间复杂度的解的）Jeff曾无奈写过一次O(n^2)的算法，其解决的问题是旅行商问题。（该问题是NPC的，即计算机中最复杂最难解决的一类问题，许多人相信这些问题是没有多项式时间复杂度的解的）Jeff曾无奈写过一次O(n^2)的算法，其解决的问题是旅行商问题。（该问题是NPC的，即计算机中最复杂最难解决的一类问题，许多人相信这些问题是没有多项式时间复杂度的解的）Jeff曾无奈写过一次O(n^2)的算法，其解决的问题是旅行商问题。（该问题是NPC的，即计算机中最复杂最难解决的一类问题，许多人相信这些问题是没有多项式时间复杂度的解的）Jeff曾无奈写过一次O(n^2)的算法，其解决的问题是旅行商问题。（该问题是NPC的，即计算机中最复杂最难解决的一类问题，许多人相信这些问题是没有多项式时间复杂度的解的）Jeff曾无奈写过一次O(n^2)的算法，其解决的问题是旅行商问题。（该问题是NPC的，即计算机中最复杂最难解决的一类问题，许多人相信这些问题是没有多项式时间复杂度的解的）Jeff曾无奈写过一次O(n^2)的算法，其解决的问题是旅行商问题。（该问题是NPC的，即计算机中最复杂最难解决的一类问题，许多人相信这些问题是没有多项式时间复杂度的解的）Jeff曾无奈写过一次O(n^2)的算法，其解决的问题是旅行商问题。（该问题是NPC的，即计算机中最复杂最难解决的一类问题，许多人相信这些问题是没有多项式时间复杂度的解的）Jeff曾无奈写过一次O(n^2)的算法，其解决的问题是旅行商问题。（该问题是NPC的，即计算机中最复杂最难解决的一类问题，许多人相信这些问题是没有多项式时间复杂度的解的）Jeff曾无奈写过一次O(n^2)的算法，其解决的问题是旅行商问题。（该问题是NPC的，即计算机中最复杂最难解决的一类问题，许多人相信这些问题是没有多项式时间复杂度的解的）Jeff曾无奈写过一次O(n^2)的算法，其解决的问题是旅行商问题。（该问题是NPC的，即计算机中最复杂最难解决的一类问题，许多人相信这些问题是没有多项式时间复杂度的解的）Jeff曾无奈写过一次O(n^2)的算法，其解决的问题是旅行商问题。（该问题是NPC的，即计算机中最复杂最难解决的一类问题，许多人相信这些问题是没有多项式时间复杂度的解的）Jeff曾无奈写过一次O(n^2)的算法，其解决的问题是旅行商问题。（该问题是NPC的，即计算机中最复杂最难解决的一类问题，许多人相信这些问题是没有多项式时间复杂度的解的）";

    @BeforeClass
    public static void setLogger() throws MalformedURLException
    {
        System.setProperty("log4j.configurationFile","log4j2-test.xml");
        LOGGER = LoggerFactory.getLogger("PushLogger");
    }

    @Test
    public void startConsumer() throws InterruptedException {
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println("stop consumer");
                System.out.println("total pull message count:" + m.size());
                LOGGER.info("total pull message count:{}", m.size());
                carreraConsumer.stop();
                LogManager.shutdown();
            }
        }));

        CarreraConfig carreraConfig = new CarreraConfig();
        carreraConfig.setServers("127.0.0.1:9713");
        carreraConfig.setGroupId("cg_chronos_consume_offset");
        carreraConfig.setRetryInterval(500);
        carreraConfig.setTimeout(3000);
        carreraConfig.setMaxBatchSize(8);

        carreraConsumer = new SimpleCarreraConsumer(carreraConfig);

        while (true) {
            List<Long> ackOffsets = new ArrayList<>();
            PullResponse response = carreraConsumer.pullMessage();
            if (response == null || response.messages == null || response.messages.size() == 0) {
                try {
                    TimeUnit.MILLISECONDS.sleep(1000);
                } catch (InterruptedException e) {
                }
                continue;
            }

            for (Message message : response.messages) {
                String body = new String(message.getValue());
                boolean isEqual = false;

                if (body.equals(destBody)) {
                    isEqual = true;
                }

                LOGGER.info("succ pull message, isEqual:{}, body:{}, delayMsgId:{}", isEqual, body, message.getKey());
                ackOffsets.add(message.getOffset());
                m.put(message.getKey(), "");
            }

            for (Long offset : ackOffsets) {
                carreraConsumer.ack(response.context, offset);
            }
        }
    }
}