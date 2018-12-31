package com.xiaojukeji.carrera.config.v4;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.xiaojukeji.carrera.config.ConfigurationValidator;
import com.xiaojukeji.carrera.config.v4.cproxy.ConsumeServerConfiguration;
import com.xiaojukeji.carrera.config.v4.cproxy.KafkaConfiguration;
import com.xiaojukeji.carrera.config.v4.cproxy.RocketmqConfiguration;
import com.xiaojukeji.carrera.utils.PropertyUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class CProxyConfig implements ConfigurationValidator, Cloneable {
    private String instance;
    private String proxyCluster;
    private List<String> brokerClusters;

    private Map<String/*brokerCluster*/, KafkaConfiguration> kafkaConfigs;
    private Map<String/*brokerCluster*/, RocketmqConfiguration> rocketmqConfigs;
    private ConsumeServerConfiguration thriftServer;

    private Set<String> groups = Collections.emptySet(); /* group white list. */

    @Override
    public boolean validate() throws ConfigException {
        try {
            if (StringUtils.isEmpty(this.instance)) {
                throw new ConfigException("[CProxyConfig] instance empty");
            } else if (StringUtils.isEmpty(this.proxyCluster)) {
                throw new ConfigException("[CProxyConfig] proxyCluster empty");
            } else if (CollectionUtils.isEmpty(this.brokerClusters)) {
                throw new ConfigException("[CProxyConfig] brokerClusters empty");
            }

            if (kafkaConfigs == null && rocketmqConfigs == null) {
                throw new ConfigException("[CProxyConfig] kafkaConfigs and rocketmqConfigs is null");
            }

            if (kafkaConfigs != null) {
                for (KafkaConfiguration kafka : kafkaConfigs.values()) {
                    if (!kafka.validate()) return false;
                }
            }

            if (rocketmqConfigs != null) {
                for (RocketmqConfiguration rocketmq : rocketmqConfigs.values()) {
                    if (!rocketmq.validate()) return false;
                }
            }

            if (thriftServer == null) {
                throw new ConfigException("[CProxyConfig] thriftServer is null");
            }
        } catch (ConfigException e) {
            throw new ConfigException(e.getMessage() + ", instance=" + this.instance);
        }

        return thriftServer.validate();
    }


    @Override
    public CProxyConfig clone() {
        CProxyConfig cProxyConfig = new CProxyConfig();
        PropertyUtils.copyNonNullProperties(cProxyConfig, this);

        if (this.kafkaConfigs != null) {
            Map<String, KafkaConfiguration> kafkaConfigs = Maps.newHashMap();
            this.kafkaConfigs.forEach((bk, conf) -> kafkaConfigs.put(bk, conf.clone()));
            cProxyConfig.setKafkaConfigs(kafkaConfigs);
        }
        if (this.rocketmqConfigs != null) {
            Map<String, RocketmqConfiguration> rocketmqConfigs = Maps.newHashMap();
            this.rocketmqConfigs.forEach((bk, conf) -> rocketmqConfigs.put(bk, conf.clone()));
            cProxyConfig.setRocketmqConfigs(rocketmqConfigs);
        }

        if (groups != null) {
            cProxyConfig.setGroups(Sets.newHashSet(groups));
        }

        return cProxyConfig;
    }

    public String getInstance() {
        return instance;
    }

    public void setInstance(String instance) {
        this.instance = instance;
    }

    public List<String> getBrokerClusters() {
        return brokerClusters;
    }

    public void setBrokerClusters(List<String> brokerClusters) {
        this.brokerClusters = brokerClusters;
    }

    public Map<String, KafkaConfiguration> getKafkaConfigs() {
        return kafkaConfigs;
    }

    public void setKafkaConfigs(Map<String, KafkaConfiguration> kafkaConfigs) {
        this.kafkaConfigs = kafkaConfigs;
    }

    public Map<String, RocketmqConfiguration> getRocketmqConfigs() {
        return rocketmqConfigs;
    }

    public void setRocketmqConfigs(Map<String, RocketmqConfiguration> rocketmqConfigs) {
        this.rocketmqConfigs = rocketmqConfigs;
    }

    public Set<String> getGroups() {
        return groups;
    }

    public void setGroups(Set<String> groups) {
        this.groups = groups;
    }

    public String getProxyCluster() {
        return proxyCluster;
    }

    public void setProxyCluster(String proxyCluster) {
        this.proxyCluster = proxyCluster;
    }

    public ConsumeServerConfiguration getThriftServer() {
        return thriftServer;
    }

    public void setThriftServer(ConsumeServerConfiguration thriftServer) {
        this.thriftServer = thriftServer;
    }

    @Override
    public String toString() {
        return "CProxyConfig{" +
                "instance='" + instance + '\'' +
                ", proxyCluster='" + proxyCluster + '\'' +
                ", brokerClusters=" + brokerClusters +
                ", kafkaConfigs=" + kafkaConfigs +
                ", rocketmqConfigs=" + rocketmqConfigs +
                ", thriftServer=" + thriftServer +
                ", groups=" + groups +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CProxyConfig that = (CProxyConfig) o;

        if (instance != null ? !instance.equals(that.instance) : that.instance != null) return false;
        if (proxyCluster != null ? !proxyCluster.equals(that.proxyCluster) : that.proxyCluster != null) return false;
        if (brokerClusters != null ? !brokerClusters.equals(that.brokerClusters) : that.brokerClusters != null)
            return false;
        if (kafkaConfigs != null ? !kafkaConfigs.equals(that.kafkaConfigs) : that.kafkaConfigs != null) return false;
        if (rocketmqConfigs != null ? !rocketmqConfigs.equals(that.rocketmqConfigs) : that.rocketmqConfigs != null)
            return false;
        if (thriftServer != null ? !thriftServer.equals(that.thriftServer) : that.thriftServer != null) return false;
        return groups != null ? groups.equals(that.groups) : that.groups == null;
    }

    @Override
    public int hashCode() {
        int result = instance != null ? instance.hashCode() : 0;
        result = 31 * result + (proxyCluster != null ? proxyCluster.hashCode() : 0);
        result = 31 * result + (brokerClusters != null ? brokerClusters.hashCode() : 0);
        result = 31 * result + (kafkaConfigs != null ? kafkaConfigs.hashCode() : 0);
        result = 31 * result + (rocketmqConfigs != null ? rocketmqConfigs.hashCode() : 0);
        result = 31 * result + (thriftServer != null ? thriftServer.hashCode() : 0);
        result = 31 * result + (groups != null ? groups.hashCode() : 0);
        return result;
    }
}

