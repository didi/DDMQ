package com.xiaojukeji.carrera.config.v4.cproxy;

import java.util.Map;
import java.util.Properties;

import com.xiaojukeji.carrera.utils.PropertyUtils;
import kafka.consumer.ConsumerConfig;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;


public class KafkaConfiguration extends MQServerConfiguration {
    private String zookeeperAddr;
    private int autoCommitInterval;
    private int rebalanceMaxRetries = 8;

    private String partitionAssignmentStrategy = "range"; //range or roundrobin
    private Map<String, String> extra;

    public String getZookeeperAddr() {
        return zookeeperAddr;
    }

    public void setZookeeperAddr(String zookeeperAddr) {
        this.zookeeperAddr = zookeeperAddr;
    }

    public int getAutoCommitInterval() {
        return autoCommitInterval;
    }

    public void setAutoCommitInterval(int autoCommitInterval) {
        this.autoCommitInterval = autoCommitInterval;
    }

    public String getPartitionAssignmentStrategy() {
        return partitionAssignmentStrategy;
    }

    public void setPartitionAssignmentStrategy(String partitionAssignmentStrategy) {
        this.partitionAssignmentStrategy = partitionAssignmentStrategy;
    }

    public int getRebalanceMaxRetries() {
        return rebalanceMaxRetries;
    }

    public void setRebalanceMaxRetries(int rebalanceMaxRetries) {
        this.rebalanceMaxRetries = rebalanceMaxRetries;
    }

    @Override
    public String toString() {
        return "KafkaConfiguration{" +
                "zookeeperAddr='" + zookeeperAddr + '\'' +
                ", autoCommitInterval=" + autoCommitInterval +
                ", rebalanceMaxRetries=" + rebalanceMaxRetries +
                ", partitionAssignmentStrategy='" + partitionAssignmentStrategy + '\'' +
                ", extra=" + extra +
                '}';
    }

    public Properties toProperties() {
        Properties props = new Properties();
        props.put("zookeeper.connect", zookeeperAddr);
        props.put("auto.commit.interval.ms", String.valueOf(autoCommitInterval));
        props.put("partition.assignment.strategy", partitionAssignmentStrategy);
        props.put("rebalance.max.retries", String.valueOf(rebalanceMaxRetries));

        if (MapUtils.isNotEmpty(extra)) {
            props.putAll(extra);
        }
        return props;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        KafkaConfiguration that = (KafkaConfiguration) o;

        if (autoCommitInterval != that.autoCommitInterval) return false;
        if (rebalanceMaxRetries != that.rebalanceMaxRetries) return false;
        if (zookeeperAddr != null ? !zookeeperAddr.equals(that.zookeeperAddr) : that.zookeeperAddr != null)
            return false;
        if (partitionAssignmentStrategy != null ? !partitionAssignmentStrategy.equals(that.partitionAssignmentStrategy) : that.partitionAssignmentStrategy != null)
            return false;
        return extra != null ? extra.equals(that.extra) : that.extra == null;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (zookeeperAddr != null ? zookeeperAddr.hashCode() : 0);
        result = 31 * result + autoCommitInterval;
        result = 31 * result + rebalanceMaxRetries;
        result = 31 * result + (partitionAssignmentStrategy != null ? partitionAssignmentStrategy.hashCode() : 0);
        result = 31 * result + (extra != null ? extra.hashCode() : 0);
        return result;
    }

    @Override
    public boolean validate() throws ConfigException {
        try {
            Properties p = toProperties();
            p.put("group.id", "validate");
            new ConsumerConfig(p);
        } catch (Exception e) {
            throw new ConfigException("[CProxy.KafkaConfig] property error: " + e.getMessage());
        }
        if (StringUtils.isEmpty(zookeeperAddr))
            throw new ConfigException("[CProxy.KafkaConfig] zookeeperAddr is empty");
        if (autoCommitInterval <= 0) throw new ConfigException("[CProxy.KafkaConfig] autoCommitInterval <= 0");
        return true;
    }

    @Override
    public KafkaConfiguration clone() {
        KafkaConfiguration kafkaConfiguration = new KafkaConfiguration();
        PropertyUtils.copyNonNullProperties(kafkaConfiguration, this);
        return kafkaConfiguration;
    }
}