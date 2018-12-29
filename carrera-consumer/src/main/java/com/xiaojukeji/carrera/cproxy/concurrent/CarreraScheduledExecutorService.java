package com.xiaojukeji.carrera.cproxy.concurrent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.*;


public class CarreraScheduledExecutorService extends AbstractExecutorService implements ScheduledExecutorService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CarreraScheduledExecutorService.class);

    private final ScheduledExecutorService executorService;

    public CarreraScheduledExecutorService(ScheduledExecutorService executor) {
        this.executorService = executor;
    }

    public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
        return executorService.schedule(new CarreraRunnable(command), delay, unit);
    }

    public <V> ScheduledFuture<V> schedule(Callable<V> callable, long delay, TimeUnit unit) {
        return executorService.schedule(new CarreraCallable<>(callable), delay, unit);
    }

    public ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit) {
        return executorService.scheduleAtFixedRate(new CarreraRunnable(command), initialDelay, period, unit);
    }

    public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit) {
        return executorService.scheduleWithFixedDelay(new CarreraRunnable(command), initialDelay, delay, unit);
    }

    @Override
    public void shutdown() {
        executorService.shutdown();
    }

    @Override
    public List<Runnable> shutdownNow() {
        return executorService.shutdownNow();
    }

    @Override
    public boolean isShutdown() {
        return executorService.isShutdown();
    }

    @Override
    public boolean isTerminated() {
        return executorService.isTerminated();
    }

    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit)
            throws InterruptedException {
        return executorService.awaitTermination(timeout, unit);
    }

    @Override
    public void execute(Runnable command) {
        executorService.execute(new CarreraRunnable(command));
    }

    class CarreraCallable<V> implements Callable<V> {
        private Callable<V> callable;

        public CarreraCallable(Callable<V> callable) {
            this.callable = callable;
        }

        @Override
        public V call() throws Exception {
            try {
                return callable.call();
            } catch (Throwable e) {
                LOGGER.error("CarreraScheduledExecutorService, job execute error", e);
                return null;
            }
        }
    }

    class CarreraRunnable implements Runnable {
        private Runnable runnable;

        public CarreraRunnable(Runnable runnable) {
            this.runnable = runnable;
        }

        @Override
        public void run() {
            try {
                runnable.run();
            } catch (Throwable e) {
                LOGGER.error("CarreraScheduledExecutorService, job execute error", e);
            }
        }
    }
}