package com.xiaojukeji.carrera.config.v4.cproxy;

import com.xiaojukeji.carrera.config.ConfigurationValidator;


public abstract class MQServerConfiguration implements ConfigurationValidator, Cloneable {

    private String clusterName;

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MQServerConfiguration that = (MQServerConfiguration) o;

        return clusterName != null ? clusterName.equals(that.clusterName) : that.clusterName == null;
    }

    @Override
    public int hashCode() {
        return clusterName != null ? clusterName.hashCode() : 0;
    }
}