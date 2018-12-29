package com.xiaojukeji.carrera.recover;

import com.alibaba.fastjson.JSON;
import com.xiaojukeji.carrera.config.CarreraConfig;
import com.xiaojukeji.carrera.producer.CarreraProducer;
import com.xiaojukeji.carrera.producer.CarreraReturnCode;
import com.xiaojukeji.carrera.thrift.Message;
import com.xiaojukeji.carrera.thrift.Result;
import com.xiaojukeji.carrera.utils.TimeUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;


public class CarreraRecover {

    private static final Logger LOGGER = LoggerFactory.getLogger(CarreraRecover.class);
    private static CarreraProducer recoverProducer;
    private static boolean isRunning;
    private static final ScheduledExecutorService singleThreadScheduledExecutor = Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r, "RecoverFromDropLogScheduler");
            t.setDaemon(true);
            return t;
        }
    });

    private static void recoverFromDropLog() {
        LOGGER.info("accept a mission");
        File dir = new File(CarreraConfig.RecoverFromDropLogDir);
        Collection<File> files = FileUtils.listFiles(dir, new RegexFileFilter(CarreraConfig.RecoverFromDropLogPattern), null);
        LOGGER.warn("dir:{},pattern:{},files.size:files:{},{}", CarreraConfig.RecoverFromDropLogDir, CarreraConfig.RecoverFromDropLogPattern, files.size(), files);
        for (File file : files) {
            recoverFromDropLog(file);
        }
        LOGGER.info("mission completed");
    }

    private static void recoverFromDropLog(File file) {
        LOGGER.info("recover from drop log:{}", file.getAbsoluteFile());
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            String line = null;
            while ((line = reader.readLine()) != null) {
                try {
                    int reasonStart = line.indexOf("REASON:");
                    int messageStart = line.indexOf(",CARRERA_MESSAGE:");
                    String reason = line.substring(reasonStart + "REASON:".length(), messageStart);
                    String message = line.substring(messageStart + ",CARRERA_MESSAGE:".length());
                    Result ret = JSON.parseObject(reason, Result.class);
                    Message msg = JSON.parseObject(message, Message.class);
                    if (ret != null && msg != null) {
                        if (ret.getCode() == CarreraReturnCode.FAIL_ILLEGAL_MSG ||
                                ret.getCode() == CarreraReturnCode.FAIL_TOPIC_NOT_EXIST ||
                                ret.getCode() == CarreraReturnCode.FAIL_TOPIC_NOT_ALLOWED) {
                            LOGGER.warn("recover from drop log, bad line to recover,line:{}", line);
                            continue;
                        }

                        if(ArrayUtils.getLength(msg.getBody()) == 0) {
                            LOGGER.warn("recover from drop log, bad line to recover, msg body is empty,line:{}", line);
                            continue;
                        }

                        long start = TimeUtils.getCurTime();
                        ret = recoverProducer.sendWithPartition(msg.getTopic(), msg.getPartitionId(), msg.getHashId(),
                                msg.getBody(), msg.getKey(), msg.getTags());
                        if(ret.getCode() > CarreraReturnCode.OK) {
                            if(ret.getCode() == CarreraReturnCode.FAIL_REFUSED_BY_RATE_LIMITER) {
                                Long used = Math.max(recoverProducer.getConfig().getCarreraClientTimeout() - TimeUtils.getElapseTime(start), 0);
                                Thread.sleep(used);
                            }
                            LOGGER.warn("recover from drop log result:{}; msg[topic:{},key:{},partition:{},hashId:{},len:{},used:{},ret.Code{},ret.Msg:{}]",
                                    "failure",
                                    msg.getTopic(), msg.getKey(), msg.getPartitionId(), msg.getHashId(),
                                    ArrayUtils.getLength(msg.getBody()), TimeUtils.getElapseTime(start),
                                    ret.getCode(), ret.getMsg());
                        }

                    }
                } catch (Exception e) {
                    LOGGER.error(String.format("exception processing content:%s", line), e);
                }
            }
            FileUtils.deleteQuietly(file);
        } catch (Exception e) {
            LOGGER.error("recover from drop log:{} exception", file.getAbsoluteFile(), e);
        }
    }

    public static void start(long initialDelay, long delay, CarreraProducer producer) throws Exception {
        if (!isRunning) {
            recoverProducer = producer;
            recoverProducer.start();
            LOGGER.info("recover producer start");
            singleThreadScheduledExecutor.scheduleWithFixedDelay(new Runnable() {
                @Override
                public void run() {
                    recoverFromDropLog();
                }
            }, initialDelay, delay, TimeUnit.MINUTES);
            isRunning = true;
        }
    }

    public static void start(CarreraProducer producer) throws Exception {
        start(5, 60, producer);
    }

    public static void shutdown() {
        if (isRunning) {
            singleThreadScheduledExecutor.shutdown();
            LOGGER.info("recover producer shutting down");
            recoverProducer.shutdown();
            LOGGER.info("recover producer shutdown");
            isRunning = false;
        }
    }

    public static final void main(String[] args) throws Exception {
        if (args.length < 3) {
            System.out.print("usage : drop_log file_type(DROP_LOG|KV) serverIP:serverPort");
            return;
        }
        String file = args[0];
        String fileType = args[1];
        String server = args[2];

        CarreraConfig config = new CarreraConfig();
        List<String> servers = new ArrayList<>();
        servers.add(server);
        config.setCarreraProxyList(servers);

        recoverProducer = new CarreraProducer(config);
        recoverProducer.start();

        if ("DROP_LOG".equals(fileType)) {
            recoverFromDropLog(new File(file));
        } else if ("KV".equals(fileType)) {
            recoverFromKVFile(new File(file));
        }
    }

    private static void recoverFromKVFile(File file) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
        while (true) {
            String topic = reader.readLine();
            if (topic == null) return;
            String key = reader.readLine();
            if (key == null) return;
            long hashId = Long.valueOf(reader.readLine());
            int partition = Integer.valueOf(reader.readLine());
            String tag = reader.readLine();
            String value = reader.readLine();
            Result result = recoverProducer.sendWithPartition(topic, partition, hashId, value, key, tag);
            LOGGER.info("result={}.topic={},key={},hashId={},partition={},tag={},value.len={}",
                    result, topic, key, hashId, partition, tag, value.length());
        }
    }
}