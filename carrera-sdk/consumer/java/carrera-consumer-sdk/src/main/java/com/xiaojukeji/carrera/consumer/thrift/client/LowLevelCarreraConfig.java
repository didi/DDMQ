package com.xiaojukeji.carrera.consumer.thrift.client;


public class LowLevelCarreraConfig extends CarreraConfig {

    /**
     * 消费集群名称，请联系队列同学获取
     */
    private String clusterName;

    /**
     * 自动提交Offset间隔，单位ms
     */
    private int commitAckInterval = 5000;

    public LowLevelCarreraConfig(){}

    public LowLevelCarreraConfig(String groupId, String servers) {
        super(groupId, servers);
    }

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public Integer getCommitAckInterval() {
        return commitAckInterval;
    }

    public void setCommitAckInterval(int commitAckInterval) {
        this.commitAckInterval = commitAckInterval;
    }

    @Override
    public String toString() {
        return "LowLevelCarreraConfig{" +
                "clusterName='" + clusterName + '\'' +
                ", commitAckInterval=" + commitAckInterval +
                "} " + super.toString();
    }

    @Override
    public LowLevelCarreraConfig clone() {
        LowLevelCarreraConfig config = new LowLevelCarreraConfig(getGroupId(), getServers());
        config.setTimeout(getTimeout());
        config.setRetryInterval(getRetryInterval());
        config.setSubmitMaxRetries(getSubmitMaxRetries());
        config.setMaxBatchSize(getMaxBatchSize());
        config.setMaxLingerTime(getMaxLingerTime());
        config.setClusterName(getClusterName());
        config.setCommitAckInterval(getCommitAckInterval());

        return config;
    }

    @Override
    public void validate(boolean singleServer) {
        super.validate(singleServer);
        if (commitAckInterval <= 0) {
            throw new CarreraConfig.CarreraConfigError("Invalid commitAckInterval : " + commitAckInterval);
        }
    }

}