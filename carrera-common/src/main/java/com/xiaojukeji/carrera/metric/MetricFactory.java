package com.xiaojukeji.carrera.metric;

import com.xiaojukeji.carrera.utils.ConfigUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;


public class MetricFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(MetricFactory.class);
    private static final int SCHEDULER_POOL_SIZE = 4;
    private static volatile boolean isInit = false;
    private static ScheduledExecutorService metricCalcScheduler;
    private static final String TAG = ConfigUtils.getDefaultConfig("com.xiaojukeji.carrera.metric.tag", "carrera");

    static {
        metricCalcScheduler = Executors.newScheduledThreadPool(SCHEDULER_POOL_SIZE, r -> {
            Thread t = new Thread(r, "MetricScheduler");
            t.setDaemon(true);
            return t;
        });
        try {
            MetricClient.getInstance().init();
            isInit = true;
        }catch (Exception ex){
            metricCalcScheduler.shutdownNow();
            LOGGER.error("MetricFactory init failed", ex);
        }
    }

    public static void destory() {
        if (isInit && metricCalcScheduler != null) {
            metricCalcScheduler.shutdown();
            MetricClient.getInstance().shutDown();
            isInit = false;
        }
    }

    public static CounterMetric getCounterMetric(String metricName, long step, TimeUnit unit, Logger metricLogger, String... metricTags) {
        if (!isInit) {
            throw new RuntimeException("not init");
        }

        CounterMetric counterMetric = new CounterMetric(getName(metricName), step, unit, metricLogger, metricTags);
        ScheduledFuture future = metricCalcScheduler.scheduleAtFixedRate(counterMetric::reportWorker, step, step, unit);
        counterMetric.setFuture(future);
        return counterMetric;
    }

    public static CounterMetric getCounterMetric(String metricName, long step, TimeUnit unit, String... metricTags) {
        return getCounterMetric(metricName, step, unit, null, metricTags);
    }

    public static RateMetric getRateMetric(String metricName, long step, TimeUnit unit, Logger metricLogger, String... metricTags) {
        if (!isInit) {
            throw new RuntimeException("not init");
        }

        RateMetric rateMetric = new RateMetric(getName(metricName), step, unit, metricLogger, metricTags);
        ScheduledFuture future = metricCalcScheduler.scheduleAtFixedRate(rateMetric::reportWorker, step, step, unit);
        rateMetric.setFuture(future);
        return rateMetric;
    }

    public static RateMetric getRateMetric(String metricName, long step, TimeUnit unit, String... metricTags) {
        return getRateMetric(metricName, step, unit, null, metricTags);
    }

    public static PercentileMetric getPercentileMetric(String metricName, List<Integer> percents, long step, TimeUnit unit, Logger metricLogger, String... metricTags) {
        if (!isInit) {
            throw new RuntimeException("not init");
        }

        PercentileMetric percentileMetric = new PercentileMetric(getName(metricName), percents, step, unit, metricLogger, metricTags);
        ScheduledFuture future = metricCalcScheduler.scheduleAtFixedRate(percentileMetric::reportWorker, step, step, unit);
        percentileMetric.setFuture(future);
        return percentileMetric;
    }

    public static PercentileMetric getPercentileMetric(String metricName, List<Integer> percents, long step, TimeUnit unit, String... metricTags) {
        return getPercentileMetric(metricName, percents, step, unit, null, metricTags);
    }

    private static String getName(String name) {
        if (!StringUtils.isEmpty(TAG)) {
            return TAG + "." + name;
        }
        return name;
    }
}