package com.xiaojukeji.carrera.metric;

import com.google.common.util.concurrent.RateLimiter;
import com.xiaojukeji.carrera.utils.CommonFastJsonUtils;
import com.xiaojukeji.carrera.utils.ConfigUtils;
import com.xiaojukeji.carrera.utils.HttpUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;


public class MetricClient implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(MetricClient.class);

    private static final int BATCH_COUNT = 200;
    private static final int SEND_RETRY_MAX = 3;
    private static final int SEND_FAILED_WAIT_MS = 50;
    private static final int PUSH_SUCCESS_CODE = 200;
    private static final int POLL_WAIT_TIME_MS = 10;
    private static final int POLL_WAIT_COUNT = 5;
    private static final int SEND_THREAD_POOL_SIZE = 5;
    private static final int BLOCK_QUEUE_SIZE = ConfigUtils.getDefaultConfig("com.xiaojukeji.carrera.metric.queue.size", 50 * 1024);
    private static final boolean DISABLE_SEND_METRIC = ConfigUtils.getDefaultConfig("com.xiaojukeji.carrera.metric.disable", true);

    private static final double RATE = 2000;
    private RateLimiter rateLimiter = RateLimiter.create(RATE);
    private String pushUrl;
    private String hostName;
    private volatile boolean isRunning = true;

    private BlockingQueue<Metric> metricsQueue = new LinkedBlockingQueue<>(BLOCK_QUEUE_SIZE);
    private ExecutorService sendThreadPool;

    static class InnerStaticClass {

        private static MetricClient instance = new MetricClient();
    }

    public static MetricClient getInstance() {
        return InnerStaticClass.instance;
    }

    private MetricClient() {
        sendThreadPool = Executors.newFixedThreadPool(SEND_THREAD_POOL_SIZE, r -> {
            Thread t = new Thread(r, "metricSendThread");
            t.setDaemon(true);
            return t;
        });
        for (int i = 0; i < SEND_THREAD_POOL_SIZE; i++) {
            sendThreadPool.submit(this);
        }
    }

    public void init() throws Exception {
        // init url and host etc.
    }

    public void shutDown() {
        isRunning = false;
        sendThreadPool.shutdown();
    }

    public String getHostName() {
        return hostName;
    }

    @Override
    public void run() {
        while (isRunning) {
            try {
                doMetricSend();
            } catch (Throwable throwable) {
                LOGGER.error("metric send failed", throwable);
            }
        }

        LOGGER.info("metric client shutdown");
    }

    private void doMetricSend() throws Exception {
        List<Metric> batch = getBatch(BATCH_COUNT);
        if (CollectionUtils.isEmpty(batch)) {
            Thread.sleep(POLL_WAIT_TIME_MS);
            return;
        }

        String jsonData = CommonFastJsonUtils.toJsonString(batch);
        if (StringUtils.isEmpty(jsonData)) {
            LOGGER.error("metric to json failed, metrics=" + batch);
            return;
        }
        LOGGER.debug("report metric, count={}, data={}", batch.size(), jsonData);
        if (DISABLE_SEND_METRIC) {
            LOGGER.debug("disable report metric, count={}, date={}", batch.size(), jsonData);
            return;
        }

        rateLimiter.acquire();
        int retryCount = 0;
        while (retryCount++ < SEND_RETRY_MAX) {
            CloseableHttpResponse response = HttpUtils.doPost(pushUrl, jsonData);
            if (response == null) {
                LOGGER.info("send message, response is null");
                Thread.sleep(SEND_FAILED_WAIT_MS);
                continue;
            }

            LOGGER.debug("metric send code={}", response.getStatusLine().getStatusCode());
            if (response.getStatusLine().getStatusCode() == PUSH_SUCCESS_CODE) {
                break;
            } else {
                LOGGER.error("send metric error, code={}, response={}", response.getStatusLine().getStatusCode(), response);
                Thread.sleep(SEND_FAILED_WAIT_MS);
            }
        }

        if (retryCount >= SEND_RETRY_MAX) {
            LOGGER.error("send failed, data=" + jsonData);
        }
    }

    public void sendMetrics(List<Metric> metrics) {
        for (Metric metric : metrics) {
            if (!metricsQueue.offer(metric)) {
                LOGGER.error("metricsQueue full, drop metric:" + metric);
            }
        }
    }

    private List<Metric> getBatch(int count) {
        List<Metric> batch = new ArrayList<>();
        int waitCount = POLL_WAIT_COUNT;
        try {
            while (waitCount > 0) {
                Metric metric = metricsQueue.poll(POLL_WAIT_TIME_MS, TimeUnit.MILLISECONDS);
                if (metric == null) {
                    waitCount--;
                    continue;
                }

                batch.add(metric);
                if (batch.size() > count) {
                    break;
                }
            }
        } catch (Exception ex) {
            LOGGER.error("get batch failed", ex);
        }
        return batch;
    }
}