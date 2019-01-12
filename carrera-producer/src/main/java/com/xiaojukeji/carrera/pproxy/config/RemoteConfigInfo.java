package com.xiaojukeji.carrera.pproxy.config;

import com.xiaojukeji.carrera.config.ConfigurationValidator;
import com.xiaojukeji.carrera.config.v4.pproxy.TopicInfoConfiguration;
import com.xiaojukeji.carrera.utils.ConfigUtils;
import org.apache.commons.lang3.StringUtils;


public class RemoteConfigInfo implements ConfigurationValidator {

    private String zookeeperAddr;
    private int port;
    private String host;
    private TopicInfoConfiguration defaultTopicInfoConf;//大量存储本地配置

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

    public static RemoteConfigInfo loadFromFile(String path) throws Exception {
        return ConfigUtils.newConfig(path, RemoteConfigInfo.class);
    }

    public TopicInfoConfiguration getDefaultTopicInfoConf() {
        return defaultTopicInfoConf;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setDefaultTopicInfoConf(TopicInfoConfiguration defaultTopicInfoConf) {
        this.defaultTopicInfoConf = defaultTopicInfoConf;
    }

    @Override
    public String toString() {
        return "RemoteConfigInfo{" +
                "zookeeperAddr='" + zookeeperAddr + '\'' +
                ", port=" + port +
                ", host=" + host +
                ", defaultTopicInfoConf=" + defaultTopicInfoConf +
                '}';
    }

    @Override public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        RemoteConfigInfo info = (RemoteConfigInfo) o;

        if (port != info.port)
            return false;
        return zookeeperAddr != null ? zookeeperAddr.equals(info.zookeeperAddr) : info.zookeeperAddr == null;
    }

    @Override public int hashCode() {
        int result = zookeeperAddr != null ? zookeeperAddr.hashCode() : 0;
        result = 31 * result + port;
        return result;
    }

    @Override
    public boolean validate() {
        return StringUtils.isNotEmpty(zookeeperAddr) && port > 0;
    }
}