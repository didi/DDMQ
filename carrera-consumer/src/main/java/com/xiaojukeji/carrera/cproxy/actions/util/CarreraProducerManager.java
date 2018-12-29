package com.xiaojukeji.carrera.cproxy.actions.util;

import com.xiaojukeji.carrera.config.CarreraConfig;
import com.xiaojukeji.carrera.producer.CarreraProducer;
import com.xiaojukeji.carrera.cproxy.concurrent.CarreraExecutors;
import com.xiaojukeji.carrera.cproxy.config.ProducerProxyConfiguration;
import org.slf4j.Logger;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.slf4j.LoggerFactory.getLogger;


public class CarreraProducerManager {
    private static final Logger LOGGER = getLogger(CarreraProducerManager.class);

    private static volatile CarreraProducer producer = null;
    private static final ScheduledExecutorService scheduler = CarreraExecutors.newScheduledThreadPool(1, "CarreraProducerManagerScheduler");

    public static synchronized void startCarreraProducer(ProducerProxyConfiguration producerProxyConfiguration) {
        if (producerProxyConfiguration.getCarreraProxyList() == null || producerProxyConfiguration.getCarreraProxyList().size() == 0) {
            LOGGER.error("error while start carrera producer for no pproxy list, producerProxyConfiguration:{}", producerProxyConfiguration);
            return;
        }
        final long start = System.currentTimeMillis();
        CarreraProducer oldProducer = producer;

        final CarreraConfig config = new CarreraConfig();
        config.setCarreraProxyList(producerProxyConfiguration.getCarreraProxyList());
        config.setCarreraProxyTimeout(producerProxyConfiguration.getCarreraProxyTimeout());
        config.setCarreraClientRetry(producerProxyConfiguration.getCarreraClientRetry());
        config.setCarreraClientTimeout(producerProxyConfiguration.getCarreraClientTimeout());
        config.setCarreraPoolSize(producerProxyConfiguration.getCarreraPoolSize());
        config.setBatchSendThreadNumber(producerProxyConfiguration.getBatchSendThreadNumber());
        CarreraProducer newProducer = new CarreraProducer(config);
        try {
            newProducer.start();
            LOGGER.info("start carrera producer, cost:{}ms, producerProxyConfiguration:{}", System.currentTimeMillis() - start, producerProxyConfiguration);
        } catch (Exception e) {
            LOGGER.error("error while start carrera producer, producerProxyConfiguration:{}, err:{}", producerProxyConfiguration, e.getMessage(), e);
            return;
        }

        producer = newProducer;

        // old producer可能正在被使用，延迟一定时间之后再关闭
        scheduler.schedule(() -> stopCarreraProducer(oldProducer), 2, TimeUnit.MINUTES);
    }

    public static void stopCarreraProducer(CarreraProducer producer) {
        final long start = System.currentTimeMillis();
        if (producer != null) {
            producer.shutdown();
            LOGGER.info("stop carrera producer, cost:{}ms", System.currentTimeMillis() - start);
        }
    }

    public static synchronized void shutdown() {
        stopCarreraProducer(producer);
        scheduler.shutdown();
    }

    public static CarreraProducer getProducer() {
        return producer;
    }
}