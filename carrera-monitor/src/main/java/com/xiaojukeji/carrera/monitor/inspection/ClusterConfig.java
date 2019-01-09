package com.xiaojukeji.carrera.monitor.inspection;


import com.xiaojukeji.carrera.config.ConfigurationValidator;

import java.util.Set;


public class ClusterConfig implements ConfigurationValidator {
    private String topic;

    private String group;

    private Set<String> pproxyServers;

    private Set<String> cproxyServers;

    private int concurrentMessages = 16;

    private int maxBodyLen = 4096;

    private int maxKeyLen = 64;

    private int maxTagLen = 64;

    public ClusterConfig() {
    }

    public int getConcurrentMessages() {
        return concurrentMessages;
    }

    public void setConcurrentMessages(int concurrentMessages) {
        this.concurrentMessages = concurrentMessages;
    }

    public int getMaxBodyLen() {
        return maxBodyLen;
    }

    public void setMaxBodyLen(int maxBodyLen) {
        this.maxBodyLen = maxBodyLen;
    }

    public int getMaxKeyLen() {
        return maxKeyLen;
    }

    public void setMaxKeyLen(int maxKeyLen) {
        this.maxKeyLen = maxKeyLen;
    }

    public int getMaxTagLen() {
        return maxTagLen;
    }

    public void setMaxTagLen(int maxTagLen) {
        this.maxTagLen = maxTagLen;
    }

    @Override
    public boolean validate() throws ConfigException {
        return true;
    }

    public Set<String> getPproxyServers() {
        return pproxyServers;
    }

    public void setPproxyServers(Set<String> pproxyServers) {
        this.pproxyServers = pproxyServers;
    }

    public Set<String> getCproxyServers() {
        return cproxyServers;
    }

    public void setCproxyServers(Set<String> cproxyServers) {
        this.cproxyServers = cproxyServers;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }
}
