package com.xiaojukeji.carrera.config;

import org.apache.commons.pool2.impl.GenericKeyedObjectPoolConfig;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


public class CarreraConfig extends GenericKeyedObjectPoolConfig implements Cloneable, Serializable {
    private static final long serialVersionUID = 3407987537894504519L;

    public static final int PARTITION_HASH = -1;
    public static final int PARTITION_RAND = -2;

    public static final int RANDOM_KEY_SIZE = 16;
    public static final long PROXY_TIMEOUT = 50;
    public static final int CLIENT_TIMEOUT = 100;
    public static final int CLIENT_RETRY = 2;
    public static final int POOL_SIZE = 20;
    public static final int BATCH_SEND_THREAD_NUMBER = 16;

    public static String RecoverFromDropLogDir = "logs/mq/old/";
    public static String RecoverFromDropLogPattern = "drop-\\d{4}-\\d{2}-\\d{2}-\\d{2}.\\d+.log";

    //Proxy端处理请求的超时时间。写队列超时之后会尝试Cache。Cache成功后会返回CACHE_OK
    private volatile long carreraProxyTimeout = PROXY_TIMEOUT;
    //client和proxy server的超时时间，一般不建议设太小。必须大于carreraProxyTimeout的值，建议设置2倍的比例
    private volatile int carreraClientTimeout = CLIENT_TIMEOUT;
    //客户端失败重试次数
    private volatile int carreraClientRetry = CLIENT_RETRY;
    //实例池中启动的producer实例数
    private volatile int carreraPoolSize = POOL_SIZE;
    //消息并发发送的并发线程数，仅使用sendBatchConcurrently方法时需要配置
    private volatile int batchSendThreadNumber = BATCH_SEND_THREAD_NUMBER;

    //proxy列表
    private List<String> carreraProxyList;

    public CarreraConfig() {
    }

    public int getBatchSendThreadNumber() {
        return batchSendThreadNumber;
    }

    public void setBatchSendThreadNumber(int batchSendThreadNumber) {
        this.batchSendThreadNumber = batchSendThreadNumber;
    }

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
        this.setMaxTotalPerKey(carreraPoolSize);
    }

    @Override
    public String toString() {
        return "CarreraConfig{" +
                "carreraProxyTimeout=" + carreraProxyTimeout +
                ", carreraClientTimeout=" + carreraClientTimeout +
                ", carreraClientRetry=" + carreraClientRetry +
                ", carreraPoolSize=" + carreraPoolSize +
                ", batchSendThreadNumber=" + batchSendThreadNumber +
                ", carreraProxyList=" + carreraProxyList +
                "} " + super.toString();
    }

    @Override
    public CarreraConfig clone() {
        CarreraConfig config = (CarreraConfig) super.clone();
        if (this.carreraProxyList != null) {
            config.carreraProxyList = new ArrayList<>(this.carreraProxyList);
        }
        config.carreraProxyTimeout = this.carreraProxyTimeout;
        config.carreraClientTimeout = this.carreraClientTimeout;
        config.carreraClientRetry = this.carreraClientRetry;
        config.carreraPoolSize = this.carreraPoolSize;
        config.batchSendThreadNumber = this.batchSendThreadNumber;

        return config;
    }

    public boolean validate() {
        return this.getCarreraClientTimeout() >= 0 &&
                this.getCarreraProxyTimeout() > 0 &&
                this.getCarreraClientRetry() >= 0 &&
                this.getCarreraPoolSize() > 0 &&
                this.getBatchSendThreadNumber() > 0;
    }

    public static CarreraConfig NewDefaultConfigForTestEnv() {
        CarreraConfig config = new CarreraConfig();
        List<String> servers = new ArrayList<>();
        servers.add("127.0.0.1:9613");
        config.setCarreraProxyList(servers);
        return config;
    }
}