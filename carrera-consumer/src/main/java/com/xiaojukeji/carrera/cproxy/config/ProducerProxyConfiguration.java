package com.xiaojukeji.carrera.cproxy.config;

import com.xiaojukeji.carrera.config.ConfigurationValidator;
import com.xiaojukeji.carrera.cproxy.utils.PropertyUtils;

import java.util.List;
import java.util.Objects;


public class ProducerProxyConfiguration implements ConfigurationValidator, Cloneable {
    private List<String> carreraProxyList;
    private long carreraProxyTimeout = 50;
    private int carreraClientTimeout = 100;
    private int carreraClientRetry = 2;
    private int carreraPoolSize = 40;
    private int batchSendThreadNumber = 32;

    public List<String> getCarreraProxyList() {
        return carreraProxyList;
    }

    public void setCarreraProxyList(List<String> carreraProxyList) {
        this.carreraProxyList = carreraProxyList;
    }

    public long getCarreraProxyTimeout() {
        return carreraProxyTimeout;
    }

    public void setCarreraProxyTimeout(long carreraProxyTimeout) {
        this.carreraProxyTimeout = carreraProxyTimeout;
    }

    public int getCarreraClientTimeout() {
        return carreraClientTimeout;
    }

    public void setCarreraClientTimeout(int carreraClientTimeout) {
        this.carreraClientTimeout = carreraClientTimeout;
    }

    public int getCarreraClientRetry() {
        return carreraClientRetry;
    }

    public void setCarreraClientRetry(int carreraClientRetry) {
        this.carreraClientRetry = carreraClientRetry;
    }

    public int getCarreraPoolSize() {
        return carreraPoolSize;
    }

    public void setCarreraPoolSize(int carreraPoolSize) {
        this.carreraPoolSize = carreraPoolSize;
    }

    public int getBatchSendThreadNumber() {
        return batchSendThreadNumber;
    }

    public void setBatchSendThreadNumber(int batchSendThreadNumber) {
        this.batchSendThreadNumber = batchSendThreadNumber;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProducerProxyConfiguration that = (ProducerProxyConfiguration) o;
        return carreraProxyTimeout == that.carreraProxyTimeout &&
                carreraClientTimeout == that.carreraClientTimeout &&
                carreraClientRetry == that.carreraClientRetry &&
                carreraPoolSize == that.carreraPoolSize &&
                batchSendThreadNumber == that.batchSendThreadNumber &&
                Objects.equals(carreraProxyList, that.carreraProxyList);
    }

    @Override
    public int hashCode() {

        return Objects.hash(carreraProxyList, carreraProxyTimeout, carreraClientTimeout, carreraClientRetry, carreraPoolSize, batchSendThreadNumber);
    }

    @Override
    protected ProducerProxyConfiguration clone() {
        ProducerProxyConfiguration producerProxyConfiguration = new ProducerProxyConfiguration();
        PropertyUtils.copyNonNullProperties(producerProxyConfiguration, this);
        return producerProxyConfiguration;
    }

    @Override
    public boolean validate() throws ConfigurationValidator.ConfigException {
        return this.getCarreraClientTimeout() >= 0 &&
                this.getCarreraProxyTimeout() > 0 &&
                this.getCarreraClientRetry() >= 0 &&
                this.getCarreraPoolSize() > 0 &&
                this.getBatchSendThreadNumber() > 0;
    }

    @Override
    public String toString() {
        return "ProducerProxyConfiguration{" +
                "carreraProxyList=" + carreraProxyList +
                ", carreraProxyTimeout=" + carreraProxyTimeout +
                ", carreraClientTimeout=" + carreraClientTimeout +
                ", carreraClientRetry=" + carreraClientRetry +
                ", carreraPoolSize=" + carreraPoolSize +
                ", batchSendThreadNumber=" + batchSendThreadNumber +
                '}';
    }
}