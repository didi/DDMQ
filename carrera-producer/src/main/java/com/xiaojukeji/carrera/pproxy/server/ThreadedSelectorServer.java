package com.xiaojukeji.carrera.pproxy.server;

import com.xiaojukeji.carrera.pproxy.producer.ProducerPool;
import com.xiaojukeji.carrera.config.v4.pproxy.CarreraConfiguration;
import com.xiaojukeji.carrera.thrift.ProducerService;
import com.xiaojukeji.carrera.pproxy.utils.LogUtils;
import com.xiaojukeji.carrera.pproxy.utils.TimeUtils;
import org.apache.thrift.TProcessor;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.server.AbstractNonblockingServer;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TThreadedSelectorServer;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TNonblockingServerSocket;
import org.apache.thrift.transport.TTransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.concurrent.*;


public class ThreadedSelectorServer implements Server {
    private static final Logger LOGGER = LoggerFactory.getLogger(ThreadedSelectorServer.class);

    private ProducerPool producerPool;
    private TServer server;
    private CarreraConfiguration config;

    public ThreadedSelectorServer(CarreraConfiguration config, ProducerPool producerPool) {
        this.config = config;
        this.producerPool = producerPool;
    }

    public void startServer() {
        try {
            TNonblockingServerSocket transport = new TNonblockingServerSocket(
                new TNonblockingServerSocket.NonblockingAbstractServerSocketArgs()
                    .port(config.getThriftServer().getPort())
                    .backlog(config.getThriftServer().getBacklog())
                    .clientTimeout(config.getThriftServer().getClientTimeout())
            );
            TThreadedSelectorServer.Args args = new TThreadedSelectorServer.Args(transport);
            TProcessor asyncProcessor = new ProducerService.AsyncProcessor<>(new ProducerAsyncServerImpl(producerPool,
                config.getThriftServer().getTimeoutCheckerThreads()));
            args.processor(asyncProcessor);
            args.protocolFactory(new TCompactProtocol.Factory());
            args.transportFactory(new TFramedTransport.Factory());
            args.selectorThreads(config.getThriftServer().getSelectorThreads());
            args.acceptQueueSizePerThread(config.getThriftServer().getAcceptQueueSizePerThread());
            args.maxReadBufferBytes = config.getThriftServer().getMaxReadBufferBytes();
            int workerThreads = config.getThriftServer().getWorkerThreads();
            if (workerThreads == 0) {
                args.workerThreads(0);
            } else {
                BlockingQueue<Runnable> workQueue;
                if (config.getThriftServer().getWorkerQueueSize() > 0) {
                    workQueue = new ArrayBlockingQueue<>(config.getThriftServer().getWorkerQueueSize());
                } else {
                    workQueue = new LinkedBlockingDeque<>();
                }
                ThreadPoolExecutor executors = new ThreadPoolExecutor(workerThreads, workerThreads,
                    0L, TimeUnit.MILLISECONDS, workQueue, r -> new Thread(r, "ThriftWorker"),
                    (r, executor) -> { //reject execution handler
                        try {
                            Field field = r.getClass().getDeclaredField("frameBuffer");
                            field.setAccessible(true);
                            AbstractNonblockingServer.FrameBuffer fb = (AbstractNonblockingServer.FrameBuffer) field.get(r);
                            fb.close();
                        } catch (Exception e) {
                            LogUtils.logError("ThreadedSelectorServer.rejectExecutionHandler", "close connection failed!", e);
                            return;
                        }
                        LogUtils.logError("ThreadedSelectorServer.rejectExecutionHandler", "connection rejected");
                    });
                long start = TimeUtils.getCurTime();
                int preStartCnt = executors.prestartAllCoreThreads();
                LOGGER.info("preStart executor pool threads:{}, cost:{}ms", preStartCnt, TimeUtils.getElapseTime(start));
                args.executorService(executors);
            }

            server = new TThreadedSelectorServer(args);

            try {
                server.serve();
            } catch (NullPointerException e) {
                LOGGER.warn("[IGNORE THIS] server.serve() throws null exception due to invoker is null.", e);
            }
        } catch (TTransportException e) {
            e.printStackTrace();
        }

    }

    public void stopServer() {
        if (null != server) {
            server.stop();
            server = null;
        }
    }
}