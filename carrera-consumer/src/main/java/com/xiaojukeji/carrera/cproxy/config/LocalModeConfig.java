package com.xiaojukeji.carrera.cproxy.config;

import com.xiaojukeji.carrera.config.ConfigurationValidator;
import com.xiaojukeji.carrera.config.v4.CProxyConfig;
import com.xiaojukeji.carrera.config.v4.GroupConfig;
import com.xiaojukeji.carrera.cproxy.utils.StringUtils;
import org.apache.commons.collections.CollectionUtils;

import java.util.List;
import java.util.Objects;


public class LocalModeConfig implements ConfigurationValidator {

    private String idc;
    private int port;
    private CProxyConfig cProxyConfig;
    private List<GroupConfig> groupConfigs;

    public String getIdc() {
        return idc;
    }

    public void setIdc(String idc) {
        this.idc = idc;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public CProxyConfig getcProxyConfig() {
        return cProxyConfig;
    }

    public void setcProxyConfig(CProxyConfig cProxyConfig) {
        this.cProxyConfig = cProxyConfig;
    }

    public List<GroupConfig> getGroupConfigs() {
        return groupConfigs;
    }

    public void setGroupConfigs(List<GroupConfig> groupConfigs) {
        this.groupConfigs = groupConfigs;
    }

    @Override
    public boolean validate() throws ConfigException {
        return !StringUtils.isEmpty(idc) && port > 0 && cProxyConfig != null && !CollectionUtils.isEmpty(groupConfigs);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LocalModeConfig that = (LocalModeConfig) o;
        return port == that.port &&
                Objects.equals(idc, that.idc) &&
                Objects.equals(cProxyConfig, that.cProxyConfig) &&
                Objects.equals(groupConfigs, that.groupConfigs);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idc, port, cProxyConfig, groupConfigs);
    }

    @Override
    public String toString() {
        return "LocalModeConfig{" +
                "idc='" + idc + '\'' +
                ", port=" + port +
                ", cProxyConfig=" + cProxyConfig +
                ", groupConfigs=" + groupConfigs +
                '}';
    }
}