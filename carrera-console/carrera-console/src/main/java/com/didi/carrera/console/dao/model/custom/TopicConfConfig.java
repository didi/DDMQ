package com.didi.carrera.console.dao.model.custom;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;


public class TopicConfConfig implements Serializable {
    public static final String  key_proxies = "proxies";

    private Map<String/*proxyCluster*/, Set<String>> proxies;

    public Map<String, Set<String>> getProxies() {
        return proxies;
    }

    public void setProxies(Map<String, Set<String>> proxies) {
        this.proxies = proxies;
    }

    @Override
    public String toString() {
        return "TopicConfConfig{" +
                "proxies=" + proxies +
                '}';
    }
}