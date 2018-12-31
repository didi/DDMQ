package com.xiaojukeji.carrera.config.v4;

import com.alibaba.fastjson.TypeReference;
import com.xiaojukeji.carrera.config.ConfigurationValidator;
import com.xiaojukeji.carrera.utils.CommonFastJsonUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;
import java.util.Set;


public class BrokerConfig implements ConfigurationValidator, Cloneable {
    private String brokerCluster;
    private String brokerClusterAddrs;
    private Map<String/*master ip:port*/, Set<String>/*slave ip:port*/> brokers;

    private Map<String/*proxyCluster*/, Set<String>> pproxies;
    private Map<String/*proxyCluster*/, Set<String>> cproxies;

    public String getBrokerCluster() {
        return brokerCluster;
    }

    public void setBrokerCluster(String brokerCluster) {
        this.brokerCluster = brokerCluster;
    }

    public String getBrokerClusterAddrs() {
        return brokerClusterAddrs;
    }

    public void setBrokerClusterAddrs(String brokerClusterAddrs) {
        this.brokerClusterAddrs = brokerClusterAddrs;
    }

    public Map<String, Set<String>> getPproxies() {
        return pproxies;
    }

    public void setPproxies(Map<String, Set<String>> pproxies) {
        this.pproxies = pproxies;
    }

    public Map<String, Set<String>> getCproxies() {
        return cproxies;
    }

    public void setCproxies(Map<String, Set<String>> cproxies) {
        this.cproxies = cproxies;
    }

    public Map<String, Set<String>> getBrokers() {
        return brokers;
    }

    public void setBrokers(Map<String, Set<String>> brokers) {
        this.brokers = brokers;
    }

    @Override
    public String toString() {
        return "BrokerConfig{" +
                ", brokerCluster='" + brokerCluster + '\'' +
                ", brokerClusterAddrs=" + brokerClusterAddrs +
                ", brokers=" + brokers +
                ", pproxies=" + pproxies +
                ", cproxies=" + cproxies +
                '}';
    }

    @Override
    public boolean validate() throws ConfigException {
        if (StringUtils.isEmpty(this.brokerCluster)) {
            throw new ConfigException("[BrokerConfig] brokerCluster empty, brokerCluster=" + brokerCluster);
        } else if (StringUtils.isEmpty(this.brokerClusterAddrs)) {
            throw new ConfigException("[BrokerConfig] brokerClusterAddrs empty, brokerCluster=" + brokerCluster);
        }

        return true;
    }

    @Override
    public BrokerConfig clone() {
        return CommonFastJsonUtils.toObject(CommonFastJsonUtils.toJsonString(this), new TypeReference<BrokerConfig>() {
        });
    }
}