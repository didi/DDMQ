package com.xiaojukeji.carrera.config.v4.pproxy;

import com.xiaojukeji.carrera.config.ConfigurationValidator;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;

import java.util.List;
import java.util.Map;


public class CarreraConfiguration implements ConfigurationValidator {
    private List<Integer> retryDelays;
    private boolean useKafka;
    private boolean useRocketmq;
    private boolean useRequestLimiter;
    private int rocketmqProducers;
    private int kafkaProducers;
    private ThriftServerConfiguration thriftServer;
    private Map<String/*brokerCluster*/, KafkaConfiguration> kafkaConfigurationMap;
    private Map<String/*brokerCluster*/, RocketmqConfiguration> rocketmqConfigurationMap;
    private DelayConfiguration delay;
    private boolean useAutoBatch = false;
    private BatchMQProducerConfiguration autoBatch;
    private boolean warmUpConnection;

    private int maxTps = 100000;
    private double tpsWarningRatio = 0.9;
    // 静态添加的配置。优先级低于动态配置。会被覆盖。
    private TopicInfoConfiguration defaultTopicInfoConf = new TopicInfoConfiguration();

    private boolean warmUpFetchTopicRouteInfo = true;

    private int limiterFailureRetryQueueSize = 1024;

    public TopicInfoConfiguration getDefaultTopicInfoConf() {
        return defaultTopicInfoConf;
    }

    public void setDefaultTopicInfoConf(TopicInfoConfiguration defaultTopicInfoConf) {
        this.defaultTopicInfoConf = defaultTopicInfoConf;
    }

    public boolean isUseRequestLimiter() {
        return useRequestLimiter;
    }

    public void setUseRequestLimiter(boolean useRequestLimiter) {
        this.useRequestLimiter = useRequestLimiter;
    }

    public boolean isWarmUpConnection() {
        return warmUpConnection;
    }

    public void setWarmUpConnection(boolean warmUpConnection) {
        this.warmUpConnection = warmUpConnection;
    }

    public ThriftServerConfiguration getThriftServer() {
        return thriftServer;
    }

    public void setThriftServer(ThriftServerConfiguration thriftServer) {
        this.thriftServer = thriftServer;
    }

    public boolean isUseKafka() {
        return useKafka;
    }

    public void setUseKafka(boolean useKafka) {
        this.useKafka = useKafka;
    }

    public boolean isUseRocketmq() {
        return useRocketmq;
    }

    public void setUseRocketmq(boolean useRocketmq) {
        this.useRocketmq = useRocketmq;
    }

    public int getRocketmqProducers() {
        return rocketmqProducers;
    }

    public void setRocketmqProducers(int rocketmqProducers) {
        this.rocketmqProducers = rocketmqProducers;
    }

    public int getKafkaProducers() {
        return kafkaProducers;
    }

    public void setKafkaProducers(int kafkaProducers) {
        this.kafkaProducers = kafkaProducers;
    }

    public List<Integer> getRetryDelays() {
        return retryDelays;
    }

    public void setRetryDelays(List<Integer> retryDelays) {
        this.retryDelays = retryDelays;
    }

    public int getMaxTps() {
        return maxTps;
    }

    public void setMaxTps(int maxTps) {
        this.maxTps = maxTps;
    }

    public double getTpsWarningRatio() {
        return tpsWarningRatio;
    }

    public void setTpsWarningRatio(double tpsWarningRatio) {
        this.tpsWarningRatio = tpsWarningRatio;
    }

    public DelayConfiguration getDelay() {
        return delay;
    }

    public void setDelay(DelayConfiguration delay) {
        this.delay = delay;
    }

    public boolean isUseAutoBatch() {
        return useAutoBatch;
    }

    public void setUseAutoBatch(boolean useAutoBatch) {
        this.useAutoBatch = useAutoBatch;
    }

    public BatchMQProducerConfiguration getAutoBatch() {
        return autoBatch;
    }

    public void setAutoBatch(BatchMQProducerConfiguration autoBatch) {
        this.autoBatch = autoBatch;
    }

    public Map<String, KafkaConfiguration> getKafkaConfigurationMap() {
        return kafkaConfigurationMap;
    }

    public void setKafkaConfigurationMap(Map<String, KafkaConfiguration> kafkaConfigurationMap) {
        this.kafkaConfigurationMap = kafkaConfigurationMap;
    }

    public Map<String, RocketmqConfiguration> getRocketmqConfigurationMap() {
        return rocketmqConfigurationMap;
    }

    public void setRocketmqConfigurationMap(Map<String, RocketmqConfiguration> rocketmqConfigurationMap) {
        this.rocketmqConfigurationMap = rocketmqConfigurationMap;
    }

    public boolean isWarmUpFetchTopicRouteInfo() {
        return warmUpFetchTopicRouteInfo;
    }

    public void setWarmUpFetchTopicRouteInfo(boolean warmUpFetchTopicRouteInfo) {
        this.warmUpFetchTopicRouteInfo = warmUpFetchTopicRouteInfo;
    }

    public int getLimiterFailureRetryQueueSize() {
        return limiterFailureRetryQueueSize;
    }

    public void setLimiterFailureRetryQueueSize(int limiterFailureRetryQueueSize) {
        this.limiterFailureRetryQueueSize = limiterFailureRetryQueueSize;
    }

    @Override
    public boolean validate() throws ConfigException {
        if (CollectionUtils.isEmpty(retryDelays)) {
            throw new ConfigException("[CarreraConfiguration] retryDelays empty");
        } else if (thriftServer == null || !thriftServer.validate()) {
            throw new ConfigException("[CarreraConfiguration] thriftServer error");
        } else if (useKafka && (kafkaProducers <= 0 || MapUtils.isEmpty(kafkaConfigurationMap) || !kafkaConfigurationMap.values().stream().allMatch(KafkaConfiguration::validate))) {
            throw new ConfigException("[CarreraConfiguration] kafka config error");
        } else if (useRocketmq && (rocketmqProducers <= 0 || MapUtils.isEmpty(rocketmqConfigurationMap) || !rocketmqConfigurationMap.values().stream().allMatch(RocketmqConfiguration::validate))) {
            throw new ConfigException("[CarreraConfiguration] rocketmq config error");
        } else if (useAutoBatch && (autoBatch == null || !autoBatch.validate())) {
            throw new ConfigException("[CarreraConfiguration] autoBatch error");
        } else if (maxTps <= 0) {
            throw new ConfigException("[CarreraConfiguration] maxTps <= 0");
        } else if (tpsWarningRatio <= 0) {
            throw new ConfigException("[CarreraConfiguration] tpsWarningRatio <= 0");
        } else if (defaultTopicInfoConf == null) {
            throw new ConfigException("[CarreraConfiguration] defaultTopicInfoConf is null");
        }

        return true;
    }

    @Override
    public String toString() {
        return "CarreraConfiguration{" +
                "retryDelays=" + retryDelays +
                ", useKafka=" + useKafka +
                ", useRocketmq=" + useRocketmq +
                ", useRequestLimiter=" + useRequestLimiter +
                ", rocketmqProducers=" + rocketmqProducers +
                ", kafkaProducers=" + kafkaProducers +
                ", thriftServer=" + thriftServer +
                ", kafkaConfigurationMap=" + kafkaConfigurationMap +
                ", rocketmqConfigurationMap=" + rocketmqConfigurationMap +
                ", delay=" + delay +
                ", useAutoBatch=" + useAutoBatch +
                ", autoBatch=" + autoBatch +
                ", warmUpConnection=" + warmUpConnection +
                ", maxTps=" + maxTps +
                ", tpsWarningRatio=" + tpsWarningRatio +
                ", defaultTopicInfoConf=" + defaultTopicInfoConf +
                ", warmUpFetchTopicRouteInfo=" + warmUpFetchTopicRouteInfo +
                ", limiterFailureRetryQueueSize=" + limiterFailureRetryQueueSize +
                '}';
    }
}