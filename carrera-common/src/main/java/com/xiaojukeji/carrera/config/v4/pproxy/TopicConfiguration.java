package com.xiaojukeji.carrera.config.v4.pproxy;

import com.google.common.collect.Maps;
import com.xiaojukeji.carrera.config.ConfigurationValidator;
import com.xiaojukeji.carrera.utils.ConfigUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;
import java.util.Set;


public class TopicConfiguration implements ConfigurationValidator {
    private static final int DEFAULT_MAX_TPS = ConfigUtils.getDefaultConfig(
            "com.xiaojukeji.carrera.config.v4.pproxy.TopicConfiguration.maxTps", 1024);

    private static final int DEFAULT_TOTAL_MAX_TPS = ConfigUtils.getDefaultConfig(
            "com.xiaojukeji.carrera.config.v4.pproxy.TopicConfiguration.totalMaxTps", 1024);

    private String idc;
    private String brokerCluster;
    private String clusterName;
    private Map<String/*proxyCluster*/, Set<String>> proxies = Maps.newHashMap();

    private int totalMaxTps = DEFAULT_TOTAL_MAX_TPS;

    private int maxTps = DEFAULT_MAX_TPS;

    public int getMaxTps() {
        return maxTps;
    }

    public void setMaxTps(int maxTps) {
        this.maxTps = maxTps;
    }

    public int getTotalMaxTps() {
        return totalMaxTps;
    }

    public void setTotalMaxTps(int totalMaxTps) {
        this.totalMaxTps = totalMaxTps;
    }

    public String getIdc() {
        return idc;
    }

    public void setIdc(String idc) {
        this.idc = idc;
    }

    public String getBrokerCluster() {
        return brokerCluster;
    }

    public void setBrokerCluster(String brokerCluster) {
        this.brokerCluster = brokerCluster;
    }

    public Map<String, Set<String>> getProxies() {
        return proxies;
    }

    public void setProxies(Map<String, Set<String>> proxies) {
        this.proxies = proxies;
    }

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    @Override
    public boolean validate() {
        return StringUtils.isNotEmpty(idc) && StringUtils.isNotEmpty(brokerCluster) && totalMaxTps > 0;
    }

    @Override
    public String toString() {
        return "TopicConfiguration{" +
                "idc='" + idc + '\'' +
                ", brokerCluster='" + brokerCluster + '\'' +
                ", clusterName='" + clusterName + '\'' +
                ", proxies=" + proxies +
                ", totalMaxTps=" + totalMaxTps +
                ", maxTps=" + maxTps +
                '}';
    }
}