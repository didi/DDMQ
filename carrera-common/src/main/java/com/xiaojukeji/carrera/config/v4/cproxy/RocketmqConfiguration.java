package com.xiaojukeji.carrera.config.v4.cproxy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.xiaojukeji.carrera.config.ConfigurationValidator;
import com.xiaojukeji.carrera.utils.PropertyUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;


public class RocketmqConfiguration extends RocketMQBaseConfig implements ConfigurationValidator, Cloneable {

    private List<String> namesrvAddrs;
    private String groupPrefix;

    public List<String> getNamesrvAddrs() {
        return namesrvAddrs;
    }

    public void setNamesrvAddrs(List<String> namesrvAddrs) {
        this.namesrvAddrs = namesrvAddrs;
    }

    public String getGroupPrefix() {
        return groupPrefix;
    }

    public void setGroupPrefix(String groupPrefix) {
        this.groupPrefix = groupPrefix;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        RocketmqConfiguration that = (RocketmqConfiguration) o;

        if (namesrvAddrs != null ? !namesrvAddrs.equals(that.namesrvAddrs) : that.namesrvAddrs != null) return false;
        return groupPrefix != null ? groupPrefix.equals(that.groupPrefix) : that.groupPrefix == null;

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (namesrvAddrs != null ? namesrvAddrs.hashCode() : 0);
        result = 31 * result + (groupPrefix != null ? groupPrefix.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "RocketmqConfiguration{" +
                "namesrvAddrs=" + namesrvAddrs +
                ", groupPrefix='" + groupPrefix + '\'' +
                ", pollNameServerInterval=" + pollNameServerInterval +
                ", heartbeatBrokerInterval=" + heartbeatBrokerInterval +
                ", persistConsumerOffsetInterval=" + persistConsumerOffsetInterval +
                ", consumeConcurrentlyMaxSpan=" + consumeConcurrentlyMaxSpan +
                ", consumeMessageBatchMaxSize=" + consumeMessageBatchMaxSize +
                ", pullThresholdForQueue=" + pullThresholdForQueue +
                ", pullBatchSize=" + pullBatchSize +
                ", consumeFromWhere=" + consumeFromWhere +
                '}';
    }

    @Override
    public boolean validate() throws ConfigException {
        if (pullBatchSize <= 0) {
            throw new ConfigException("[CProxy.RocketmqConfig] pullBatchSize <= 0");
        } else if (pullThresholdForQueue <= 0) {
            throw new ConfigException("[CProxy.RocketmqConfig] pullThresholdForQueue <= 0");
        } else if (pollNameServerInterval <= 0) {
            throw new ConfigException("[CProxy.RocketmqConfig] pollNameServerInterval <= 0");
        } else if (consumeMessageBatchMaxSize <= 0) {
            throw new ConfigException("[CProxy.RocketmqConfig] consumeMessageBatchMaxSize <= 0");
        } else if (heartbeatBrokerInterval <= 0) {
            throw new ConfigException("[CProxy.RocketmqConfig] heartbeatBrokerInterval <= 0");
        } else if (consumeConcurrentlyMaxSpan <= 0) {
            throw new ConfigException("[CProxy.RocketmqConfig] consumeConcurrentlyMaxSpan <= 0");
        } else if (persistConsumerOffsetInterval <= 0) {
            throw new ConfigException("[CProxy.RocketmqConfig] persistConsumerOffsetInterval <= 0");
        } else if (CollectionUtils.isEmpty(namesrvAddrs)) {
            throw new ConfigException("[CProxy.RocketmqConfig] namesrvAddrs is empty");
        } else if (StringUtils.isEmpty(groupPrefix)) {
            throw new ConfigException("[CProxy.RocketmqConfig] groupPrefix is empty");
        }
        return true;
    }

    @Override
    public RocketmqConfiguration clone() {
        RocketmqConfiguration rocketmqConfiguration = new RocketmqConfiguration();
        PropertyUtils.copyNonNullProperties(rocketmqConfiguration, this);
        if (this.subscription != null) {
            rocketmqConfiguration.subscription = new HashMap<>(this.subscription);
        }
        if (this.namesrvAddrs != null) {
            rocketmqConfiguration.namesrvAddrs = new ArrayList<>(this.namesrvAddrs);
        }
        return rocketmqConfiguration;
    }
}
