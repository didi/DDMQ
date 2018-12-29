package com.xiaojukeji.carrera.cproxy.server;

import com.xiaojukeji.carrera.config.v4.cproxy.ConsumeServerConfiguration;
import com.xiaojukeji.carrera.thrift.consumer.ConsumerService;
import com.xiaojukeji.carrera.cproxy.utils.TimeUtils;
import org.apache.thrift.TProcessor;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TThreadedSelectorServer;
import org.apache.thrift.server.ThriftContext;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TNonblockingServerSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


public class ConsumeServer {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConsumeServer.class);

    private TServer server;
    private ConsumeServerConfiguration config;

    public ConsumeServer(ConsumeServerConfiguration config) {
        this.config = config;
    }

    public void init() throws Exception {
        if (server != null) {
            throw new Exception("server is already running!");
        }
        long start = TimeUtils.getCurTime();
        TProcessor processor = new ConsumerService.AsyncProcessor<>(ConsumerServiceImpl.getInstance());
        TNonblockingServerSocket transport = new TNonblockingServerSocket(
                new TNonblockingServerSocket.NonblockingAbstractServerSocketArgs()
                        .port(config.getPort())
                        .backlog(config.getBacklog())
        );

        TThreadedSelectorServer.Args serverArgs = new TThreadedSelectorServer.Args(transport);
        serverArgs.processor(processor)
                .protocolFactory(new TCompactProtocol.Factory())
                .transportFactory(new TFramedTransport.Factory())
                .selectorThreads(config.getSelectorThreads())
                .acceptQueueSizePerThread(config.getAcceptQueueSizePerThread())
                .executorService(getExecutorService());
        serverArgs.maxReadBufferBytes = config.getMaxReadBufferBytes();


        server = new TThreadedSelectorServer(serverArgs);
        LOGGER.info("start server cost:{}ms", TimeUtils.getElapseTime(start));
    }

    public void start() {
        if (server == null) {
            throw new RuntimeException("please call init first");
        }
        LOGGER.info("server start serve");
        server.serve();
        LOGGER.info("server stop serve");
    }

    public void stop() {
        if (server != null) {
            server.stop();
            server = null;
        }
    }

    private ExecutorService getExecutorService() {
        BlockingQueue<Runnable> workQueue;
        if (config.getWorkerQueueSize() > 0) {
            workQueue = new ArrayBlockingQueue<>(config.getWorkerQueueSize());
        } else {
            workQueue = new LinkedBlockingDeque<>();
        }
        ThreadPoolExecutor executors = new ThreadPoolExecutor(config.getWorkerThreads(), config.getWorkerThreads(),
                0L, TimeUnit.MILLISECONDS, workQueue, r -> new Thread(r, "ThriftWorker")) {
            @Override
            public void execute(Runnable command) {
                super.execute(() -> {
                    ConsumerServiceImpl.getInstance().setThriftContext(new ThriftContext(command));
                    command.run();
                });
            }
        };
        executors.prestartAllCoreThreads();
        return executors;
    }
}