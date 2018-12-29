package com.xiaojukeji.carrera.consumer.examples;

import com.xiaojukeji.carrera.consumer.thrift.Context;
import com.xiaojukeji.carrera.consumer.thrift.Message;
import com.xiaojukeji.carrera.consumer.thrift.client.CarreraConfig;
import com.xiaojukeji.carrera.consumer.thrift.client.CarreraConsumer;
import com.xiaojukeji.carrera.consumer.thrift.client.MessageProcessor;
import org.apache.commons.cli.*;
import org.apache.logging.log4j.LogManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.apache.commons.cli.PatternOptionBuilder.NUMBER_VALUE;


public class ConsumerPerformance {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConsumerPerformance.class);

    private static volatile long lastTime;
    private static volatile int lastCnt;

    public static void main(String[] args) {

        CommandLine cmd = parseCmdLine(args);
        CarreraConfig config = parseConfig(cmd);
        LOGGER.info("config:", config);
        int concurrency = Integer.valueOf(cmd.getOptionValue('c'));
        LOGGER.info("concurrency:{}", concurrency);
        final CarreraConsumer consumer = new CarreraConsumer(config);
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                LogManager.shutdown(); //shutdown log4j2.
                consumer.stop();
            }
        }));
        final boolean logDetail = cmd.hasOption("d");
        final AtomicInteger ai = new AtomicInteger();
        final long start = System.currentTimeMillis();
        lastTime = start;
        lastCnt = 0;
        consumer.startConsume(new MessageProcessor() {
            @Override
            public Result process(Message message, Context context) {
                if (logDetail) {
                    LOGGER.info("process key:{}, value.length:{}, offset:{}, context:{}", message.getKey(),
                            message.getValue().length, message.getOffset(), context);
                }
                ai.incrementAndGet();
                return Result.SUCCESS;
            }
        }, concurrency);

        ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();

        scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                int curCnt = ai.get();
                long now = System.currentTimeMillis();
                LOGGER.info("time:{},curTPS:{},tolTPS:{}", (int) Math.round((now - start) / 1000.0),
                        1000.0 * (curCnt - lastCnt) / (now - lastTime),
                        1000.0 * curCnt / (now - start));
                lastCnt = curCnt;
                lastTime = now;
            }
        }, 1, 1, TimeUnit.SECONDS);
    }

    private static CarreraConfig parseConfig(CommandLine cmd) {

        CarreraConfig config = new CarreraConfig(cmd.getOptionValue('g'), cmd.getOptionValue('s'));
        if (cmd.hasOption("t")) {
            config.setTimeout(Integer.valueOf(cmd.getOptionValue("t")));
        }
        if (cmd.hasOption("r")) {
            config.setRetryInterval(Integer.valueOf(cmd.getOptionValue("r")));
        }
        if (cmd.hasOption("sr")) {
            config.setSubmitMaxRetries(Integer.valueOf(cmd.getOptionValue("sr")));
        }
        if (cmd.hasOption("b")) {
            config.setMaxBatchSize(Integer.valueOf(cmd.getOptionValue("b")));
        }
        if (cmd.hasOption("l")) {
            config.setMaxLingerTime(Integer.valueOf(cmd.getOptionValue("l")));
        }
        return config;
    }

    private static CommandLine parseCmdLine(String[] args) {
        Options options = new Options();
        options.addOption(Option.builder("g").hasArg().desc("groupId").required().build());
        options.addOption(Option.builder("s").hasArg().desc("servers").required().build());
        options.addOption(Option.builder("t").hasArg().desc("timeout").type(NUMBER_VALUE).build());
        options.addOption(Option.builder("r").hasArg().desc("retryInterval").type(NUMBER_VALUE).build());
        options.addOption(Option.builder("sr").hasArg().desc("submitMaxRetries").type(NUMBER_VALUE).build());
        options.addOption(Option.builder("b").hasArg().desc("maxBatchSize").type(NUMBER_VALUE).build());
        options.addOption(Option.builder("l").hasArg().desc("maxLingerTime").type(NUMBER_VALUE).build());
        options.addOption(Option.builder("c").hasArg().desc("concurrency").required().type(NUMBER_VALUE).build());
        options.addOption(Option.builder("d").hasArg(false).desc("log detail").build());

        try {
            return new DefaultParser().parse(options, args);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            new HelpFormatter().printHelp("ConsumerPerformance", options);

            System.exit(1);
            return null;
        }
    }
}