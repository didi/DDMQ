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
package org.apache.rocketmq.namesrv.ha;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import java.net.NetworkInterface;
import java.util.Enumeration;
import org.apache.commons.lang3.StringUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.leader.LeaderLatch;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.rocketmq.common.constant.LoggerName;
import org.apache.rocketmq.namesrv.NamesrvController;
import org.apache.zookeeper.CreateMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.util.Set;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;




public class StateKeeper {
    private static final Logger log = LoggerFactory.getLogger(LoggerName.NAMESRV_LOGGER_NAME);
    private static final int SESSION_TIMEOUT_MS = 3000;
    private static final int CONNECTION_TIMEOUT_MS = 3000;
    private static final int RETRY_INTERVAL_MS = 1000;
    private static final int RETRY_COUNT = 3;
    private static final int NAME_SERVER_SIZE_MIN = 2;
    private static final String NS_LEADER = "leader";
    private static final String IDS = "ids";

    private String hostName;
    private CuratorFramework zkClient;
    private LeaderLatch leaderLatch;
    private NamesrvController namesrvController;
    private ConcurrentHashMap<String, Set<String>> aliveBrokers = new ConcurrentHashMap<>(4);

    public StateKeeper(NamesrvController namesrvController) {
        this.namesrvController = namesrvController;
    }

    public void start() throws Exception {
        if (StringUtils.isEmpty(namesrvController.getNamesrvConfig().getClusterName())
            || StringUtils.isEmpty(namesrvController.getNamesrvConfig().getZkPath())) {
            log.error("clusterName:{} or zk path:{} is empty",
                namesrvController.getNamesrvConfig().getClusterName(), namesrvController.getNamesrvConfig().getZkPath());
            throw new Exception("cluster name or zk path is null");
        }
        hostName = getHostName();
        zkClient = CuratorFrameworkFactory.newClient(namesrvController.getNamesrvConfig().getZkPath(),
            SESSION_TIMEOUT_MS, CONNECTION_TIMEOUT_MS, new ExponentialBackoffRetry(RETRY_INTERVAL_MS, RETRY_COUNT));
        zkClient.getConnectionStateListenable().addListener(new StateListener());
        zkClient.start();

        createRootPath();
        registerLeaderLatch();
    }

    public void shutdown() {
        try {
            if (leaderLatch != null) {
                leaderLatch.close();
            }
        } catch (Exception ex) {
            log.warn("leaderLatch close failed", ex);
        }
        if (zkClient != null) {
            zkClient.close();
        }
    }

    private void createRootPath() throws Exception {
        if (zkClient.checkExists().forPath(getRootPath()) == null) {
            zkClient.create().forPath(getRootPath());
        }
    }

    private String getRootPath() {
        return "/" + namesrvController.getNamesrvConfig().getClusterName();
    }

    private String getClusterPath(String clusterName) {
        return getRootPath() + "/" + clusterName;
    }

    private String getNSPath(String clusterName) {
        return getIdsPath(clusterName) + "/" + hostName;
    }

    private String getNSPath(String clusterName, String ns) {
        return getIdsPath(clusterName) + "/" + ns;
    }

    private String getIdsPath(String clusterName) {
        return getRootPath() + "/" + clusterName + "/" + IDS;
    }

    private String getLeaderPath() {
        return getRootPath() + "/" + NS_LEADER;
    }

    public boolean isLeader() {
        return leaderLatch != null && leaderLatch.hasLeadership();
    }

    public boolean isHealthyInOtherNS(String clusterName, String broker) {
        List<String> nameServers = null;
        try {
            nameServers = zkClient.getChildren().forPath(getIdsPath(clusterName));
        } catch (Exception ex) {
            log.error("get child failed", ex);
        }

        if (nameServers == null || nameServers.size() < NAME_SERVER_SIZE_MIN) {
            log.warn("name servers would not be null or empty, nameServers:{}", nameServers);
            return true;
        }

        int healthyCount = 0;
        for (String ns : nameServers) {
            if (hostName.equals(ns)) {
                continue;
            }
            try {
                Set brokers = fromJsonBytes(zkClient.getData().forPath(getNSPath(clusterName, ns)), Set.class);
                if (brokers != null && brokers.contains(broker)) {
                    healthyCount++;
                    log.info("host:{} think it is healthy, cluster:{}, broker:{}", ns, clusterName, broker);
                }
            } catch (Exception ex) {
                log.warn("get data failed, path:" + getNSPath(clusterName, ns), ex);
            }
        }

        int unhealthyCount = nameServers.size() - healthyCount;
        int maxUnhealthyCount = (int) Math.ceil(nameServers.size() * namesrvController.getNamesrvConfig().getUnhealthyRateAllNs());

        log.info("total count:{}, unhealthy count:{}, unhealthy rate:{}, threshold:{}", nameServers.size(),
            nameServers.size() - healthyCount, namesrvController.getNamesrvConfig().getUnhealthyRateAllNs(), maxUnhealthyCount);

        if (unhealthyCount >= maxUnhealthyCount) {
            return false;
        }

        return true;
    }

    public void updateBrokerStatus(String clusterName, Set<String> brokers) {
        try {
            //add cluster node
            if (zkClient.checkExists().forPath(getClusterPath(clusterName)) == null) {
                zkClient.create().forPath(getClusterPath(clusterName));
                zkClient.create().forPath(getIdsPath(clusterName));
            }

            if (zkClient.checkExists().forPath(getNSPath(clusterName)) != null) {
                Set brokerSetInZk = fromJsonBytes(zkClient.getData().forPath(getNSPath(clusterName)), Set.class);
                if (!brokers.equals(brokerSetInZk)) {
                    zkClient.setData().forPath(getNSPath(clusterName), toJsonString(brokers).getBytes());
                    aliveBrokers.put(clusterName, brokers);
                }

            } else {
                zkClient.create().withMode(CreateMode.EPHEMERAL).forPath(getNSPath(clusterName), toJsonString(brokers).getBytes());
                aliveBrokers.put(clusterName, brokers);
            }
        } catch (Exception ex) {
            log.error("update broker status failed, clusterName:" + clusterName + ", brokers:" + brokers, ex);
        }
    }

    public synchronized void registerLeaderLatch() throws Exception {
        if (zkClient.checkExists().forPath(getLeaderPath()) == null) {
            zkClient.create().forPath(getLeaderPath());
        }
        leaderLatch = new LeaderLatch(zkClient, getLeaderPath(), hostName);
        leaderLatch.start();

        log.info("leaderLatch start");
    }

    public synchronized void registerAll() {
        for (Map.Entry<String, Set<String>> entry : aliveBrokers.entrySet()) {
            try {
                if (zkClient.checkExists().forPath(getNSPath(entry.getKey())) == null) {
                    zkClient.create().withMode(CreateMode.EPHEMERAL).forPath(getNSPath(entry.getKey()), toJsonString(entry.getValue()).getBytes());
                }
            } catch (Exception ex) {
                log.error("create node fail in registerAll", ex);
            }
        }
    }

    public String toJsonString(Object obj) {
        return JSON.toJSONString(obj, SerializerFeature.WriteMapNullValue);
    }

    public static <T> T fromJsonBytes(byte[] jsonBytes, Class<T> clz) {
        try {
            return JSONObject.parseObject(jsonBytes, clz);
        } catch (Exception e) {
            log.error("error while fromJsonString, err:{}", e.getMessage(), e);
        }
        return null;
    }

    private String getHostName() {
        //host
        String host = "unknown_host";
        try {
            String hostGet = InetAddress.getLocalHost().getHostName();
            if (StringUtils.isNotEmpty(hostGet)) {
                host = hostGet;
            }
        } catch (Exception ex) {
            log.error("get host name failed", ex);
        }

        if ("unknown_host".equals(host) || host.toLowerCase().equals("localhost")) {
            try {
                String ip = getAddress();
                if (StringUtils.isNotEmpty(ip)) {
                    host = ip;
                }
            } catch (Exception ex) {
                log.error("get ip failed");
            }
        }
        return host;
    }

    private String getAddress() {
        try {
            Enumeration<NetworkInterface> netInterfaces = NetworkInterface.getNetworkInterfaces();
            while (netInterfaces.hasMoreElements()) {
                NetworkInterface ni = netInterfaces.nextElement();
                Enumeration<InetAddress> ips = ni.getInetAddresses();
                while (ips.hasMoreElements()) {
                    InetAddress ip = ips.nextElement();
                    if (ip.getHostAddress().equals("127.0.0.1")) {
                        continue;
                    }
                    if (!ip.isLoopbackAddress() && ip.getHostAddress().indexOf(":") == -1) {
                        return ip.getHostAddress();
                    }
                }
            }
        } catch (Exception e) {
            log.error("get ip failed", e);
        }
        return null;
    }

    class StateListener implements ConnectionStateListener {

        @Override public void stateChanged(CuratorFramework client, ConnectionState newState) {
            log.warn("state changed, state:{}", newState);
            if (ConnectionState.CONNECTED == newState || ConnectionState.RECONNECTED == newState) {
                log.warn("session change:{}, register again", newState);
                registerAll();
            }
        }
    }
}
