package com.xiaojukeji.carrera.metric;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.TimeUnit;


public class PercentileMetric extends MetricReporter {
    private static final Logger LOGGER = LoggerFactory.getLogger(PercentileMetric.class);

    private List<MetricBuffer<RecycleArray>> metricBufferArray = new ArrayList<>();
    private Set<Integer> percents = new HashSet<>();


    public PercentileMetric(String metricName, List<Integer> percents, long step, TimeUnit unit, Logger metricLogger, String... metricTags) {
        super(metricName, step, unit, metricLogger, metricTags);
        if (CollectionUtils.isEmpty(percents)) {
            throw new IllegalArgumentException("percent is empty or null");
        }
        if (!checkPercent(percents)) {
            throw new IllegalArgumentException("percent is not in (0,100], percents:" + percents);
        }
        this.percents.addAll(percents);

        metricBufferArray.add(new MetricBuffer<>());
        metricBufferArray.add(new MetricBuffer<>());
    }

    private boolean checkPercent(List<Integer> percents) {
        for (int percent : percents) {
            if (percent <= 0 || percent > 100) {
                return false;
            }
        }
        return true;
    }

    public void put(long val, String... tags) {
        MetricKey metricKey = new MetricKey(tags);
        metricBufferArray.get(metricBufferIdx).computeIfAbsent(metricKey, _tags -> new RecycleArray())
                .put(val);
    }

    protected List<Metric> buildMetrics(int index) {
        List<Metric> metrics = new ArrayList<>();
        metricBufferArray.get(index).forEach((key, val) -> {
            if(val.getLength() > 0) {
                metrics.addAll(buildMetric(key.getTags(), val));
            }
            val.reset();
        });

        return metrics;
    }

    private List<Metric> buildMetric(String tags[], RecycleArray array) {
        List<Metric> metrics = new ArrayList<>();
        if (tags == null || metricTags == null || tags.length != metricTags.length) {
            LOGGER.error("metric:{}, tag count not equal, tags:{}, metricTags:{}", metricName, tags, metricTags);
            return metrics;
        }

        int dateLen = array.getLength();
        Arrays.sort(array.getData(), 0, dateLen);

        for (int percent : percents) {
            long value;
            if (percent == 100) {
                value = array.getMax();
            } else {
                value = array.getData()[(percent * dateLen) / 100];
            }
            if (value <= 0) {
                continue;
            }

            Metric metric = new Metric(metricName, MetricClient.getInstance().getHostName(), value, step);
            for (int i = 0; i < metricTags.length; i++) {
                metric.setTag(metricTags[i], trimTag(tags[i]));
            }
            metric.setTag("percentile", percent == 100 ? "max" : String.valueOf(percent));

            metrics.add(metric);
        }

        return metrics;
    }

}