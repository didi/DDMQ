package com.xiaojukeji.carrera.config.v4;

import com.alibaba.fastjson.TypeReference;
import com.xiaojukeji.carrera.config.ConfigurationValidator;
import com.xiaojukeji.carrera.config.v4.pproxy.CarreraConfiguration;
import com.xiaojukeji.carrera.utils.CommonFastJsonUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Set;


public class PProxyConfig implements ConfigurationValidator, Cloneable {
    private String instance/*ip:port*/;
    private String proxyCluster;
    private List<String> brokerClusters;

    private Set<String> topics = Collections.emptySet(); /* topic white list. */

    private CarreraConfiguration carreraConfiguration;

    @Override
    public boolean validate() throws ConfigException {
        try {
            if (StringUtils.isEmpty(this.instance)) {
                throw new ConfigException("[PProxyConfig] instance empty");
            } else if (StringUtils.isEmpty(this.proxyCluster)) {
                throw new ConfigException("[PProxyConfig] proxyCluster empty");
            } else if (CollectionUtils.isEmpty(this.brokerClusters)) {
                throw new ConfigException("[PProxyConfig] brokerClusters empty");
            } else if (carreraConfiguration == null || !carreraConfiguration.validate()) {
                throw new ConfigException("[PProxyConfig] carreraConfiguration error");
            }
        } catch (ConfigException e) {
            throw  new ConfigException(e.getMessage() + ", instance=" + instance);
        }

        return true;
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

    public Set<String> getTopics() {
        return topics;
    }

    public void setTopics(Set<String> topics) {
        this.topics = topics;
    }

    public String getProxyCluster() {
        return proxyCluster;
    }

    public void setProxyCluster(String proxyCluster) {
        this.proxyCluster = proxyCluster;
    }

    public CarreraConfiguration getCarreraConfiguration() {
        return carreraConfiguration;
    }

    public void setCarreraConfiguration(CarreraConfiguration carreraConfiguration) {
        this.carreraConfiguration = carreraConfiguration;
    }

    @Override
    public PProxyConfig clone() {
        return CommonFastJsonUtils.toObject(CommonFastJsonUtils.toJsonString(this), new TypeReference<PProxyConfig>() {
        });
    }

    @Override
    public String toString() {
        return "PProxyConfig{" +
                "instance='" + instance + '\'' +
                ", proxyCluster='" + proxyCluster + '\'' +
                ", brokerClusters=" + brokerClusters +
                ", topics=" + topics +
                ", carreraConfiguration=" + carreraConfiguration +
                '}';
    }
}