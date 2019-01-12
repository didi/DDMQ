package com.xiaojukeji.carrera.metric;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;


public class CounterMetric extends MetricReporter {
    private static final Logger LOGGER = LoggerFactory.getLogger(CounterMetric.class);

    private List<MetricBuffer<AtomicLong>> metricBufferArray = new ArrayList<>();

    public CounterMetric(String metricName, long step, TimeUnit unit, Logger metricLogger, String... metricTags) {
        super(metricName, step, unit, metricLogger, metricTags);

        metricBufferArray.add(new MetricBuffer<>());
        metricBufferArray.add(new MetricBuffer<>());
    }

    public void inc(String... tags) {
        inc(1, tags);
    }

    public void inc(long val, String... tags) {
        MetricKey metricKey = new MetricKey(tags);
        metricBufferArray.get(metricBufferIdx).computeIfAbsent(metricKey, _tags -> new AtomicLong(0))
                .getAndAdd(val);
    }

    public void max(long val, String... tags) {
        MetricKey metricKey = new MetricKey(tags);
        metricBufferArray.get(metricBufferIdx).computeIfAbsent(metricKey, _tags -> new AtomicLong(0))
                .updateAndGet(x -> Math.max(x, val));
    }

    protected List<Metric> buildMetrics(int index) {
        List<Metric> metrics = new ArrayList<>();
        metricBufferArray.get(index).forEach((key, val) -> {
            long value = calcValue(val.getAndSet(0));
            if (value > 0) {
                Metric metric = buildMetric(key.getTags(), value);
                if (metric != null) {
                    metrics.add(metric);
                }
            }
        });

        return metrics;
    }

    protected long calcValue(long value) {
        return value;
    }

    private Metric buildMetric(String tags[], long value) {
        if (tags == null || metricTags == null || tags.length != metricTags.length) {
            LOGGER.error("metric:{}, tag count not equal, tags:{}, metricTags:{}", metricName, tags, metricTags);
            return null;
        }
        Metric metric = new Metric(metricName, MetricClient.getInstance().getHostName(), value, step);

        for (int i = 0; i < metricTags.length; i++) {
            metric.setTag(metricTags[i], trimTag(tags[i]));
        }

        return metric;
    }

}