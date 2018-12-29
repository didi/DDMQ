package com.xiaojukeji.carrera.consumer.thrift.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.regex.Pattern;


public class CarreraConfig implements Cloneable, Serializable {
    private static final long serialVersionUID = 3661212922184986125L;
    private static final Logger LOGGER = LoggerFactory.getLogger(CarreraConfig.class);

    public static int TIMEOUT = 5000;
    public static int RETRY_INTERVAL = 50;
    public static int SUBMIT_MAX_RETRIES = 3;
    public static int MAX_BATCH_SIZE = 8;
    public static int MAX_LINGER_TIME = 50;

    static final Pattern GROUP_PATTERN = Pattern.compile("^[%|a-zA-Z0-9_-]+$");
    static final Pattern SERVERS_PATTERN = Pattern.compile("^[\\d.:;]+$");

    //消费组名称
    private String groupId;

    //configs, could be updated by carrera service discovery
    //!!!!add or delete need update the module about carrera service discovery
    //client和proxy server的超时时间, 要大于maxLingerTime
    private int timeout = TIMEOUT;
    //拉取消息失败时的延迟重试间隔
    private int retryInterval = RETRY_INTERVAL;
    //提交消费状态的最大重试次数
    private int submitMaxRetries = SUBMIT_MAX_RETRIES;
    //一次拉取能获取到的最大消息条数，服务端根据此值和服务端的配置，取最小值
    private int maxBatchSize = MAX_BATCH_SIZE;
    //拉取消息时，在服务端等待消息的最长时间
    private int maxLingerTime = MAX_LINGER_TIME;

    //configs, report but could not be updated by carrera service discovery
    //proxy列表,本地配置proxy时必要参数；使用服务发现时,如果服务发现获取IP list失败，则会使用此list作为默认配置
    private String servers = null;

    public CarreraConfig() {
    }

    public CarreraConfig(String groupId, String servers) {
        this.groupId = groupId;
        this.servers = servers;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public int getRetryInterval() {
        return retryInterval;
    }

    public void setRetryInterval(int retryInterval) {
        this.retryInterval = retryInterval;
    }

    public int getSubmitMaxRetries() {
        return submitMaxRetries;
    }

    public void setSubmitMaxRetries(int submitMaxRetries) {
        this.submitMaxRetries = submitMaxRetries;
    }

    public int getMaxBatchSize() {
        return maxBatchSize;
    }

    public void setMaxBatchSize(int maxBatchSize) {
        this.maxBatchSize = maxBatchSize;
    }

    public int getMaxLingerTime() {
        return maxLingerTime;
    }

    public void setMaxLingerTime(int maxLingerTime) {
        this.maxLingerTime = maxLingerTime;
    }

    public String getServers() {
        return servers;
    }

    public void setServers(String servers) {
        this.servers = servers;
    }

    @Override
    public String toString() {
        return "CarreraConfig{" +
                "groupId='" + groupId + '\'' +
                ", timeout=" + timeout +
                ", retryInterval=" + retryInterval +
                ", submitMaxRetries=" + submitMaxRetries +
                ", maxBatchSize=" + maxBatchSize +
                ", maxLingerTime=" + maxLingerTime +
                ", servers='" + servers + '\'' +
                '}';
    }

    @Override
    public CarreraConfig clone() {
        CarreraConfig config = new CarreraConfig();
        config.groupId = this.groupId;
        config.servers = this.servers;
        config.timeout = this.timeout;
        config.retryInterval = this.retryInterval;
        config.submitMaxRetries = this.submitMaxRetries;
        config.maxBatchSize = this.maxBatchSize;
        config.maxLingerTime = this.maxLingerTime;

        return config;
    }

    public void validate(boolean singleServer) {
        if (groupId == null || !GROUP_PATTERN.matcher(groupId).matches()) {
            throw new CarreraConfigError("Invalid groupId : " + groupId);
        }
        if (servers == null || !SERVERS_PATTERN.matcher(servers).matches()) {
            throw new CarreraConfigError("Invalid servers : " + servers);
        }
        if (singleServer && servers.contains(";")) {
            throw new CarreraConfigError("Invalid single server : " + servers);
        }

        if (timeout < 0) throw new CarreraConfigError("Invalid timeout : " + timeout);
        if (retryInterval <= 0) throw new CarreraConfigError("Invalid retryInterval : " + retryInterval);
        if (submitMaxRetries <= 0) throw new CarreraConfigError("Invalid submitMaxRetries : " + submitMaxRetries);
        if (maxBatchSize <= 0) throw new CarreraConfigError("Invalid maxBatchSize : " + maxBatchSize);
        if (maxLingerTime < 0) throw new CarreraConfigError("Invalid maxLingerTime : " + maxLingerTime);
        if (timeout < maxLingerTime || maxLingerTime * 2 > timeout)
            throw new CarreraConfigError("timeout must be more than 2 times maxLingerTime, timeout=" + timeout + ",maxLingerTime=" + maxLingerTime);
    }

    public static class CarreraConfigError extends RuntimeException {
        public CarreraConfigError(String message) {
            super(message);
        }
    }
}