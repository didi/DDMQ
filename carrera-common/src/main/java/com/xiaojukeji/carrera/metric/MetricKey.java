package com.xiaojukeji.carrera.metric;

import java.util.Arrays;


public class MetricKey {
    private String[] tags;

    public MetricKey(String[] tags) {
        this.tags = tags;
    }

    public String[] getTags() {
        return tags;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MetricKey metricKey = (MetricKey) o;

        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        return Arrays.equals(tags, metricKey.tags);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(tags);
    }
}