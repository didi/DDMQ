/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.rocketmq.broker.config;

import com.alibaba.fastjson.JSONObject;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;
import org.apache.commons.lang3.StringUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.NodeCache;
import org.apache.curator.framework.recipes.cache.NodeCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.rocketmq.broker.BrokerController;
import org.apache.rocketmq.common.constant.ConfigName;
import org.apache.rocketmq.common.constant.LoggerName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigUpdate {
    private static final Logger log = LoggerFactory.getLogger(LoggerName.BROKER_LOGGER_NAME);
    private static final String HOST_NAME_SUFFIX = ".diditaxi.com";
    private BrokerController controller;
    private CuratorFramework client;
    private NodeCache cache;

    public ConfigUpdate(BrokerController controller) {
        this.controller = controller;
    }

    public boolean init() {
        if (StringUtils.isEmpty(controller.getBrokerConfig().getZkPath())) {
            return true;
        }

        client = CuratorFrameworkFactory.newClient(controller.getBrokerConfig().getZkPath(), new ExponentialBackoffRetry(1000, 3));
        client.start();

        String path = getBrokerConfigPath();
        try {
            if (client.checkExists().forPath(path) == null) {
                log.error("config path in not exist, path:{}", path);
                return false;
            }
            //add watcher
            cache = new NodeCache(client, path);
            NodeCacheListener listener = new NodeCacheListener() {
                @Override public void nodeChanged() throws Exception {
                    log.info("config changed, update");
                    ChildData data = cache.getCurrentData();
                    if (null != data) {
                        String config = new String(cache.getCurrentData().getData());
                        updateConfig(config);
                    } else {
                        log.warn("node is deleted");
                    }
                }
            };

            cache.getListenable().addListener(listener);

            cache.start();
        } catch (Exception ex) {
            log.error("cache start failed", ex);
            return false;
        }

        return true;
    }

    private String getBrokerConfigPath() {
        String host = getHostNameWithIpDefault();
        int index = host.indexOf(HOST_NAME_SUFFIX);
        if (index > 0) {
            host = host.substring(0, index);
        }
        String path = "/" + host + ":" + controller.getNettyServerConfig().getListenPort();
        return path;
    }

    private String getHostNameWithIpDefault() {
        String host = "unknown_host";

        String ip;
        try {
            ip = InetAddress.getLocalHost().getHostName();
            if (StringUtils.isNotEmpty(ip)) {
                host = ip;
            }
        } catch (Exception var3) {
            log.error("get host name failed", var3);
        }

        if ("unknown_host".equals(host) || host.toLowerCase().equals("localhost")) {
            try {
                ip = getHostAddress();
                if (StringUtils.isNotEmpty(ip)) {
                    host = ip;
                }
            } catch (Exception var2) {
                log.error("get ip failed");
            }
        }

        return host;
    }

    private String getHostAddress() {
        try {
            Enumeration netInterfaces = NetworkInterface.getNetworkInterfaces();

            while (netInterfaces.hasMoreElements()) {
                NetworkInterface ni = (NetworkInterface) netInterfaces.nextElement();
                Enumeration ips = ni.getInetAddresses();

                while (ips.hasMoreElements()) {
                    InetAddress ip = (InetAddress) ips.nextElement();
                    if (!ip.getHostAddress().equals("127.0.0.1") && !ip.isLoopbackAddress() && ip.getHostAddress().indexOf(":") == -1) {
                        return ip.getHostAddress();
                    }
                }
            }
        } catch (Exception var4) {
            log.error("get ip failed", var4);
        }

        return null;
    }

    private void updateConfig(String config) {
        if (StringUtils.isEmpty(config)) {
            return;
        }
        log.info("update config:{}", config);
        Map<String, Object> configMap = JSONObject.parseObject(config, Map.class);
        if (configMap == null || configMap.isEmpty()) {
            return;
        }

        Properties properties = new Properties();
        for (Map.Entry<String, Object> entry : configMap.entrySet()) {
            if (isConfigIgnore(entry.getKey())) {
                log.info("not first start, ignore config:{}", entry.getKey());
                continue;
            }

            properties.put(entry.getKey(), entry.getValue().toString());
        }
        if (!controller.getBrokerConfig().isRoleConfigInit()) {
            properties.put(ConfigName.ROLE_CONFIG_INIT, "true");
        }
        log.info("properties:{}", properties);

        controller.getConfiguration().update(properties);

        if (properties.containsKey(ConfigName.NAME_SRV_ADDR) && controller.getBrokerOuterAPI() != null) {
            controller.getBrokerOuterAPI().updateNameServerAddressList(controller.getBrokerConfig().getNamesrvAddr());
            log.info("update name srv addr:{}", controller.getBrokerConfig().getNamesrvAddr());
        }
    }

    private boolean isConfigIgnore(String key) {
        boolean isIgnore = false;
        if (ConfigName.BROKER_ROLE.equals(key) || ConfigName.BROKER_ID.equals(key)
            || ConfigName.BROKER_CLUSTER_NAME.equals(key) || ConfigName.BROKER_NAME.equals(key)) {
            isIgnore = true;
        }

        return isIgnore && controller.getBrokerConfig().isRoleConfigInit();
    }

    public void shutdown() {
        try {
            if (cache != null) {
                cache.close();
            }
        } catch (Exception ex) {
            log.warn("cache close failed", ex);
        }
        if (client != null) {
            client.close();
        }
    }
}
