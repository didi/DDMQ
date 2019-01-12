package com.xiaojukeji.chronos.benchmark;

import com.xiaojukeji.carrera.consumer.thrift.Context;
import com.xiaojukeji.carrera.consumer.thrift.Message;
import com.xiaojukeji.carrera.consumer.thrift.client.CarreraConfig;
import com.xiaojukeji.carrera.consumer.thrift.client.CarreraConsumer;
import com.xiaojukeji.carrera.consumer.thrift.client.MessageProcessor;
import com.xiaojukeji.carrera.utils.ConfigUtils;
import com.xiaojukeji.chronos.benchmark.config.BenchmarkConfig;
import com.xiaojukeji.chronos.benchmark.config.PullConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;


public class DelayPull {
    private static final Logger LOGGER = LoggerFactory.getLogger(DelayPull.class);

    private static PullConfig pullConfig;
    private static CarreraConsumer consumer;
    private static AtomicInteger counter = new AtomicInteger(0);

    public static void main(String[] args) throws Exception {
        if (args.length > 1) {
            LOGGER.error("too many args. only need one.");
            return;
        }

        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                consumer.stop();
                LOGGER.info("benchmark pull over, total pull message count:{}", counter.get());
            }
        }));

        LOGGER.info("benchmark pull start");

        init(args[0]);

        consumer.startConsume(new MessageProcessor() {
            @Override
            public Result process(Message message, Context context) {
                LOGGER.info("succ pull message, body.len:{}, delayMsgId:{}", new String(message.getValue()).length(), message.getKey());
                counter.getAndIncrement();
                return Result.SUCCESS;
            }
        }, pullConfig.getThreadNum());

        try {
            TimeUnit.DAYS.sleep(30);
        } catch (InterruptedException e) {
        }
    }

    private static void init(final String configPath) throws Exception {
        pullConfig = ConfigUtils.newConfig(configPath, BenchmarkConfig.class).getPullConfig();

        CarreraConfig config = new CarreraConfig(pullConfig.getGroup(), pullConfig.getCproxyAddrs());
        config.setRetryInterval(pullConfig.getRetryIntervalMs());
        config.setMaxBatchSize(pullConfig.getMaxBatchSize());
        config.setMaxLingerTime(2000);
        consumer = new CarreraConsumer(config);
    }
}