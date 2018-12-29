package com.xiaojukeji.carrera.monitor.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class ExecutorUtils {

    private static final Logger logger = LoggerFactory.getLogger(ExecutorUtils.class);

    /**
     * The default rejected execution handler
     */
    private static final RejectedExecutionHandler defaultHandler = (r, executor) -> {
        logger.info("Thread rejected task,start run task by caller,current queue size {}", executor.getQueue().size());
        if (!executor.isShutdown()) {
            r.run();
        }
    };

    /**
     * ExecutorHelper默认使用CachedThreadPool作为默认的线程池，但是有风险，不建议使用
     */
    private static class DefaultThreadPool {
        public static ExecutorService es = Executors.newCachedThreadPool(new DefaultThreadFactory("cache", null));
    }

    private static ExecutorService getCachedThreadPool() {
        return DefaultThreadPool.es;
    }

    /**
     * 停掉默认的线程池
     */
    public static void shutdown() {
        if (!getCachedThreadPool().isShutdown()) {
            getCachedThreadPool().shutdown();
        }
    }

    /**
     * 停掉线程池
     *
     * @param executorService
     */
    public static void shutdown(ExecutorService executorService) {
        if (!executorService.isShutdown()) {
            executorService.shutdown();
        }
    }

    public static ExecutorService newFixedThreadPool(int nThread, String threadName, int capacity) {
        return new ThreadPoolExecutor(nThread, nThread, 0L, TimeUnit.MILLISECONDS, new ArrayBlockingQueue(capacity), new DefaultThreadFactory("fixed", threadName), defaultHandler);
    }

    /**
     * 创建线程名称前缀为threadPrefix的线程池
     *
     * @param threadPrefix
     * @return
     */
    public static ExecutorService newCachedThreadPool(String threadPrefix) {
        return Executors.newCachedThreadPool(new DefaultThreadFactory("cache", threadPrefix));
    }

    /**
     * 创建大小为threadSize的线程池，每个线程名称前缀为threadPrefix
     *
     * @param threadSize
     * @param threadPrefix
     * @return
     */
    public static ExecutorService newFixedThreadPool(Integer threadSize, String threadPrefix) {
        if (threadSize == null) {
            return null;
        }
        return Executors.newFixedThreadPool(threadSize.intValue(), new DefaultThreadFactory("fixed", threadPrefix));
    }

    /**
     * 创建大小为threadSize的缓存线程池，每个线程名称前缀为threadPrefix，超时时间为milliSeconds
     *
     * @param threadSize
     * @param threadPrefix
     * @param milliSeconds
     * @return
     */
    public static ExecutorService newFixedThreadPool(Integer threadSize, String threadPrefix, Long milliSeconds) {
        if (threadSize == null) {
            return null;
        }
        return new ThreadPoolExecutor(threadSize, threadSize,
                milliSeconds == null ? 0L : milliSeconds, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>(),
                new DefaultThreadFactory("fixed", threadPrefix));
    }

    /**
     * 创建大小为threadSize，队列容量为capacity的缓存线程池，每个线程名称前缀为threadPrefix
     *
     * @param threadSize
     * @param threadPrefix
     * @param capacity
     * @return
     */
    public static ExecutorService newFixedThreadPool(Integer threadSize, String threadPrefix, Integer capacity) {
        if (threadSize == null) {
            return null;
        }
        return new ThreadPoolExecutor(threadSize, threadSize, 0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>(capacity == null ? Integer.MAX_VALUE : capacity),
                new DefaultThreadFactory("fixed", threadPrefix), defaultHandler);
    }


    /**
     * 默认线程工厂
     */
    private static class DefaultThreadFactory implements ThreadFactory {
        private static final AtomicInteger poolNumber = new AtomicInteger(1);
        private final ThreadGroup group;
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final String namePrefix;

        DefaultThreadFactory(String threadPoolType, String threadPrefix) {
            SecurityManager s = System.getSecurityManager();
            group = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
            namePrefix = "pool" + (threadPoolType == null ? "" : "-" + threadPoolType) + "-" + poolNumber.getAndIncrement() + "-thread-"
                    + (threadPrefix == null ? "" : threadPrefix);
        }

        public Thread newThread(Runnable r) {
            Thread t = new Thread(group, r, namePrefix + threadNumber.getAndIncrement(), 0);
            if (t.isDaemon())
                t.setDaemon(true);
            if (t.getPriority() != Thread.NORM_PRIORITY)
                t.setPriority(Thread.NORM_PRIORITY);
            return t;
        }
    }

    /**
     * 交给executor执行任务
     *
     * @param executorService
     * @param call
     * @return
     */
    public static <T> Future<T> execAndReturn(ExecutorService executorService, Callable<T> call) {
        return executorService.submit(call);
    }

    /**
     * 交给executor执行任务
     *
     * @param call
     * @return
     */
    public static <T> Future<T> execAndReturn(Callable<T> call) {
        return execAndReturn(getCachedThreadPool(), call);
    }

    /**
     * 获取future里面的对象，抛出异常，用在对执行结果出错后不影响其他逻辑的地方
     *
     * @param future
     * @return
     */
    public static <T> T getFutureObject(Future<T> future) {

        T result = null;

        try {
            result = future.get();
        } catch (InterruptedException e) {
            logger.error("getFutureObject InterruptedException,result=" + result, e.fillInStackTrace());
        } catch (ExecutionException e) {
            logger.error("getFutureObject ExecutionException,result=" + result, e.fillInStackTrace());
        }

        return result;
    }

    /**
     * 并发执行多个任务
     *
     * @param executorService
     * @param callList
     * @return
     */
    public static <T> List<T> execListAndReturn(ExecutorService executorService, List<? extends Callable<T>> callList) {

        if (callList == null || callList.size() == 0) {
            return null;
        }

        List<T> result = new ArrayList<T>();
        List<Future<T>> futures = new ArrayList<Future<T>>();

        for (Callable<T> call : callList) {
            futures.add(execAndReturn(executorService, call));
        }

        for (Future<T> future : futures) {
            boolean success = false;
            T ret = null;
            try {
                ret = future.get();
                result.add(ret);
                success = true;
            } catch (InterruptedException e) {
                logger.error("execListAndReturn InterruptedException,ret=" + ret, e.fillInStackTrace());
            } catch (ExecutionException e) {
                logger.error("execListAndReturn ExecutionException,ret=" + ret, e.fillInStackTrace());
            } finally {
                if (!success) {
                    result.add(null);
                }
            }
        }

        return result;

    }

    /**
     * 并发执行多个任务
     *
     * @param callList
     * @return
     */
    public static <T> List<T> execListAndReturn(List<? extends Callable<T>> callList) {
        return execListAndReturn(getCachedThreadPool(), callList);
    }

    /**
     * 并发执行多个任务
     *
     * @param executorService
     * @param callMap
     * @return
     */
    public static <K, V> Map<K, V> execMapAndReturn(ExecutorService executorService, Map<K, ? extends Callable<V>> callMap) {

        if (callMap == null || callMap.isEmpty()) {
            return null;
        }

        Set<K> keys = callMap.keySet();

        Map<K, V> result = new HashMap<K, V>();

        Map<K, Future<V>> futures = new HashMap<K, Future<V>>();

        for (K key : keys) {
            Callable<V> call = callMap.get(key);
            futures.put(key, execAndReturn(executorService, call));
        }

        for (K key : keys) {
            V val = null;
            try {
                val = futures.get(key).get();
                result.put(key, val);
            } catch (InterruptedException e) {
                logger.error("execMapAndReturn InterruptedException,key=" + key + ",val=" + val, e.fillInStackTrace());
            } catch (ExecutionException e) {
                logger.error("execMapAndReturn ExecutionException,key=" + key + ",val=" + val, e.fillInStackTrace());
            } catch (Exception e) {
                logger.error("execMapAndReturn unknown exception,key=" + key + ",val=" + val, e.fillInStackTrace());
            }
        }

        return result;
    }

    /**
     * 并发执行多个任务
     *
     * @param callMap
     * @return
     */
    public static <K, V> Map<K, V> execMapAndReturn(Map<K, ? extends Callable<V>> callMap) {
        return execMapAndReturn(getCachedThreadPool(), callMap);
    }
}
