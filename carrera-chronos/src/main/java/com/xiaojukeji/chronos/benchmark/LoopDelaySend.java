package com.xiaojukeji.chronos.benchmark;

import com.xiaojukeji.carrera.chronos.enums.MsgTypes;
import com.xiaojukeji.carrera.config.CarreraConfig;
import com.xiaojukeji.carrera.producer.CarreraProducer;
import com.xiaojukeji.carrera.producer.CarreraReturnCode;
import com.xiaojukeji.carrera.thrift.DelayMeta;
import com.xiaojukeji.carrera.thrift.DelayResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class LoopDelaySend {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoopDelaySend.class);

    public static void main(String[] args) throws Exception {

        CarreraProducer producer;
        List<String> ips = new ArrayList();
        ips.add("127.0.0.1:9613");

        CarreraConfig carreraConfig = new CarreraConfig();
        carreraConfig.setCarreraProxyList(ips);
        carreraConfig.setCarreraProxyTimeout(200);
        carreraConfig.setCarreraClientRetry(3);
        carreraConfig.setCarreraClientTimeout(300);
        carreraConfig.setCarreraPoolSize(10);
        producer = new CarreraProducer(carreraConfig);

        long start = System.currentTimeMillis();
        producer.start();
        ExecutorService executorService = Executors.newFixedThreadPool(100);

//        for(int k = 0; k < 10; k++) {
            CountDownLatch cdl = new CountDownLatch(100);
            for (int i = 0; i < 100; i++) {
                executorService.execute(() -> {
                    sendMsg(producer);
                    cdl.countDown();
                });
            }
            cdl.await();
//        }

        long end = System.currentTimeMillis();
        LOGGER.info("benchmark over~~~~~~~~~~~~~~~~~~~~~, cost {} ms.", end - start);
    }

    static void sendMsg(CarreraProducer producer) {
        DelayMeta delayMeta = new DelayMeta();
        long now = System.currentTimeMillis();
        for (int j = 0; j < 1000; j++) {
            delayMeta.setTimestamp(now / 1000 + 10);
            delayMeta.setExpire(0);
            delayMeta.setDmsgtype(MsgTypes.LOOP_DELAY.getValue());
            delayMeta.setInterval(10);
            delayMeta.setTimes(1);
            DelayResult res = producer.sendDelay("chronos_test", "terstsdf", delayMeta);
            if (res.getCode() == CarreraReturnCode.OK) {
                LOGGER.info("send res : {}", res);
            }
        }
    }
}