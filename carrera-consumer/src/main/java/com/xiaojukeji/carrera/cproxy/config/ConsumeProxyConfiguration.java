package com.xiaojukeji.carrera.cproxy.config;

import com.xiaojukeji.carrera.config.ConfigurationValidator;
import com.xiaojukeji.carrera.utils.ConfigUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;


public class ConsumeProxyConfiguration implements ConfigurationValidator {

    private String zookeeperAddr;

    private int port;

    private String host;

    public String getZookeeperAddr() {
        return zookeeperAddr;
    }

    public void setZookeeperAddr(String zookeeperAddr) {
        this.zookeeperAddr = zookeeperAddr;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public static ConsumeProxyConfiguration loadFromFile(String path) throws Exception {
        return ConfigUtils.newConfig(path, ConsumeProxyConfiguration.class);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConsumeProxyConfiguration that = (ConsumeProxyConfiguration) o;
        return port == that.port &&
                Objects.equals(zookeeperAddr, that.zookeeperAddr) &&
                Objects.equals(host, that.host);
    }

    @Override
    public int hashCode() {

        return Objects.hash(zookeeperAddr, port, host);
    }

    @Override
    public String toString() {
        return "ConsumeProxyConfiguration{" +
                "zookeeperAddr='" + zookeeperAddr + '\'' +
                ", port=" + port +
                ", host='" + host + '\'' +
                '}';
    }

    @Override
    public boolean validate() {
        return StringUtils.isNotEmpty(zookeeperAddr) && port > 0;
    }
}