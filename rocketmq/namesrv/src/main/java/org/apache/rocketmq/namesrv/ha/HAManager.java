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

import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.rocketmq.common.MixAll;
import org.apache.rocketmq.common.ThreadFactoryImpl;
import org.apache.rocketmq.common.constant.LoggerName;
import org.apache.rocketmq.common.protocol.route.BrokerData;
import org.apache.rocketmq.namesrv.NamesrvController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.SortedSet;
import java.util.TreeSet;

public class HAManager {
    private static final Logger log = LoggerFactory.getLogger(LoggerName.NAMESRV_LOGGER_NAME);

    private final ScheduledExecutorService haScheduledThread = Executors.newSingleThreadScheduledExecutor(new ThreadFactoryImpl(
        "HAScheduledThread"));

    private NamesrvController namesrvController;
    private Detector detector;
    private StateKeeper stateKeeper;
    private RoleManager roleManager;
    private ConcurrentHashMap<String, Long> brokerEnableSwitch = new ConcurrentHashMap<>();

    public HAManager() {
    }

    public HAManager(NamesrvController namesrvController) {
        this.namesrvController = namesrvController;
        detector = new Detector(namesrvController);
        stateKeeper = new StateKeeper(namesrvController);
        roleManager = new RoleManager(namesrvController);
    }

    public void start() throws Exception {
        detector.start();
        stateKeeper.start();
        roleManager.start();
        haScheduledThread.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                try {
                    HAManager.this.doHa();
                } catch (Throwable throwable) {
                    log.error("do ha failed", throwable);
                }
            }
        }, namesrvController.getNamesrvConfig().getDetectIntervalMs(), namesrvController.getNamesrvConfig().getDetectIntervalMs(), TimeUnit.MILLISECONDS);
    }

    public void shutdown() {
        haScheduledThread.shutdown();
        detector.shutdown();
        stateKeeper.shutdown();
    }

    private void doHa() throws Exception {
        HealthStatus healthStatus = detector.detectLiveStat();
        Map<String, Map<String, Boolean>> liveStatus = getLiveBroker(healthStatus);

        //role switch
        checkAndSwitchRole(liveStatus);

        //status update to zk
        for (Map.Entry<String, Map<String, Boolean>> entry : liveStatus.entrySet()) {
            updateBrokerStatus(entry.getKey(), entry.getValue());
        }

    }

    public boolean changeToSlave(String clusterName, String brokerName, String brokerAddr) {
        if (!stateKeeper.isLeader()) {
            return true;
        }

        BrokerData brokerData = namesrvController.getRouteInfoManager().getBrokerData(brokerName);
        if (brokerData == null) {
            return true;
        }

        SortedSet<Long> ids = new TreeSet<>(brokerData.getBrokerAddrs().keySet());
        long slaveId = selectSlaveId(ids);

        log.info("change to slave, broker name:{}, broker addr:{}, broker id:{}", brokerName, brokerAddr, slaveId);
        return roleManager.change2Slave(brokerName, brokerAddr, slaveId, true);
    }

    public long selectSlaveId(SortedSet<Long> ids) {
        long slaveId = -1;
        if (ids.size() - 1 == ids.last()) {
            slaveId = ids.last() + 1;
        } else {
            long preId = 0;
            for (long id : ids) {
                if (id == MixAll.MASTER_ID) {
                    continue;
                }
                if (id - preId > 1) {
                    slaveId = preId + 1;
                    break;
                }
                preId++;
            }
            if (slaveId == -1) {
                slaveId = ids.last() + 1;
            }
        }
        return slaveId;
    }

    private void checkAndSwitchRole(Map<String, Map<String, Boolean>> liveStatus) {
        for (Map.Entry<String, Map<String, Boolean>> cluster : liveStatus.entrySet()) {
            if (!stateKeeper.isLeader()) {
                continue;
            }
            String clusterName = cluster.getKey();

            for (Map.Entry<String, Boolean> broker : cluster.getValue().entrySet()) {
                if (broker.getValue()) {
                    continue;
                }
                String brokerName = broker.getKey();
                log.info("this name server detect that the broker is unhealthy, broker name:{}, cluster:{}", brokerName, clusterName);

                if (!isInRouteInfoManager(clusterName, brokerName)) {
                    log.warn("not in route info, not need to change");
                    continue;
                }

                //unhealthy in other node
                boolean isHealthyInOtherNS = stateKeeper.isHealthyInOtherNS(clusterName, brokerName);
                log.info("healthy status in other ns:{}", isHealthyInOtherNS);
                if (!isHealthyInOtherNS) {
                    log.warn("broker is unhealthy in other ns, broker name:{}, cluster:{}", brokerName, clusterName);
                }

                //select new master
                if (!isHealthyInOtherNS && isSwitchRole(clusterName, brokerName)) {
                    RoleChangeInfo roleChangeInfo = selectNewMaster(clusterName, brokerName);
                    if (roleChangeInfo == null) {
                        log.warn("can not get a new master, clusterName:{}, brokerName:{}", clusterName, brokerName);
                        continue;
                    }
                    log.info("nodes would be changed {}", roleChangeInfo);

                    if (roleChangeInfo.oldMaster == null) {
                        log.warn("no old master, just change a slave to master");
                        //slave to new master
                        if (!roleManager.change2Master(brokerName, roleChangeInfo.newMaster.addr, true)) {
                            log.error("change slave to master failed, stop. clusterName:{}, brokerName:{}, brokerAddr:{}", clusterName,
                                brokerName, roleChangeInfo.newMaster.addr);
                            continue;
                        }
                    } else {
                        //old master to slave
                        if (!roleManager.change2Slave(brokerName, roleChangeInfo.oldMaster.addr, roleChangeInfo.oldMaster.expectId, false)) {
                            log.error("change master to slave failed, stop. clusterName:{}, brokerName:{}, brokerAddr:{}", clusterName,
                                brokerName, roleChangeInfo.oldMaster.addr);
                            continue;
                        }
                        //slave to new master
                        if (!roleManager.change2Master(brokerName, roleChangeInfo.newMaster.addr, true)) {
                            log.error("change slave to master failed, stop. clusterName:{}, brokerName:{}, brokerAddr:{}", clusterName,
                                brokerName, roleChangeInfo.newMaster.addr);
                            continue;
                        }
                        //change new slave id
                        long slaveId;
                        BrokerData brokerData = namesrvController.getRouteInfoManager().getBrokerData(brokerName);
                        if (brokerData != null) {
                            SortedSet<Long> ids = new TreeSet<>(brokerData.getBrokerAddrs().keySet());
                            slaveId = selectSlaveId(ids);
                        } else {
                            slaveId = roleChangeInfo.newMaster.oldId;
                        }

                        if (!roleManager.changeId(brokerName, roleChangeInfo.oldMaster.addr, slaveId, false)) {
                            log.error("change id failed, stop. clusterName:{}, brokerName:{}, brokerAddr:{}", clusterName,
                                brokerName, roleChangeInfo.oldMaster.addr);
                            continue;
                        }
                    }

                    log.info("cluster:{}, broker:{}, change role success", clusterName, brokerName);
                    //clear old detect info
                    detector.reset(clusterName, brokerName);
                }
            }
        }
    }

    private boolean isSwitchRole(String clusterName, String brokerName) {
        if (namesrvController.getNamesrvConfig().isRoleAutoSwitchEnable()) {
            return true;
        }

        boolean isSwitch = false;
        String key = getClusterBrokerKey(clusterName, brokerName);
        if (brokerEnableSwitch.containsKey(key)) {
            if (System.currentTimeMillis() - brokerEnableSwitch.get(key) < namesrvController.getNamesrvConfig().getEnableValidityPeriodMs()) {
                log.info("broker:{} enable to switch", brokerName);
                isSwitch = true;
            }
            brokerEnableSwitch.remove(key);
        }

        return isSwitch;
    }

    public void enableBrokerRoleSwitch(String clusterName, String brokerName) {
        brokerEnableSwitch.put(getClusterBrokerKey(clusterName, brokerName), System.currentTimeMillis());
        log.info("enable clusterName:{}, brokerName:{} to switch role", clusterName, brokerName);
    }

    private String getClusterBrokerKey(String clusterName, String brokerName) {
        return clusterName + "@" + brokerName;
    }

    private boolean isInRouteInfoManager(String cluster, String brokerName) {
        BrokerData brokerData = namesrvController.getRouteInfoManager().getBrokerData(brokerName);
        if (brokerData == null || !cluster.equals(brokerData.getCluster())) {
            log.warn("no broker data for broker name:{}, broker data:{}", brokerName, brokerData);
            return false;
        }

        return true;
    }

    public RoleChangeInfo selectNewMaster(String cluster, String brokerName) {
        BrokerData brokerData = namesrvController.getRouteInfoManager().getBrokerData(brokerName);
        if (brokerData == null || !cluster.equals(brokerData.getCluster())) {
            log.warn("no broker data for broker name:{}, broker data:{}", brokerName, brokerData);
            return null;
        }

        HashMap<Long, String> brokerAddrs = new HashMap<>(brokerData.getBrokerAddrs());
        for (Iterator<Map.Entry<Long, String>> it = brokerAddrs.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<Long, String> item = it.next();
            if (item.getKey() > namesrvController.getNamesrvConfig().getMaxIdForRoleSwitch()) {
                it.remove();
            }
        }

        //no broker
        if (brokerAddrs == null || brokerAddrs.isEmpty()) {
            log.warn("no broker addrs, for broker name:{}, broker data:{}", brokerName, brokerData);
            return null;
        }

        //only one, and master
        if (brokerAddrs.size() == 1 && brokerAddrs.get(MixAll.MASTER_ID) != null) {
            log.warn("only on broker, but it is current master");
            return null;
        }

        //slave exist
        RoleChangeInfo roleChangeInfo = new RoleChangeInfo();
        SortedSet<Long> ids = new TreeSet<>(brokerAddrs.keySet());
        if (ids.first() == MixAll.MASTER_ID) {
            roleChangeInfo.oldMaster = new RoleInChange(brokerAddrs.get(ids.first()), ids.first(), ids.last() + 1);
        }

        long newMasterId = pickMaster(brokerAddrs);
        if (newMasterId == -1) {
            //newMasterId = ids.last();
            log.error("do not get master, broker name:{}", brokerName);
            return null;
        }
        roleChangeInfo.newMaster = new RoleInChange(brokerAddrs.get(newMasterId), newMasterId, MixAll.MASTER_ID);

        return roleChangeInfo;
    }

    private long pickMaster(HashMap<Long, String> brokerAddrs) {
        long maxOffset = -1;
        long brokerId = -1;
        for (Map.Entry<Long, String> broker : brokerAddrs.entrySet()) {
            if (broker.getKey() == MixAll.MASTER_ID) {
                continue;
            }
            long offset = namesrvController.getRouteInfoManager().getBrokerMaxPhyOffset(broker.getValue());
            if (offset > maxOffset) {
                brokerId = broker.getKey();
                maxOffset = offset;
            }
        }
        log.info("get new master id:{}, maxOffset:{}", brokerId, maxOffset);
        return brokerId;
    }

    private void updateBrokerStatus(String clusterName, Map<String, Boolean> status) {
        Set<String> alive = new HashSet<>();
        for (Map.Entry<String, Boolean> entry : status.entrySet()) {
            if (entry.getValue()) {
                alive.add(entry.getKey());
            }
        }

        stateKeeper.updateBrokerStatus(clusterName, alive);
    }

    private Map<String, Map<String, Boolean>> getLiveBroker(HealthStatus healthStatus) {
        Map<String/*cluster name*/, Map<String/*broker name*/, Boolean>> brokerLiveStatus = new HashMap<>(4);
        for (Map.Entry<String, Map<String, HealthStatus.NodeHealthStatus>> cluster : healthStatus.getClusterStatus().entrySet()) {
            String clusterName = cluster.getKey();
            Map<String, HealthStatus.NodeHealthStatus> brokerStatus = cluster.getValue();
            for (Map.Entry<String, HealthStatus.NodeHealthStatus> broker : brokerStatus.entrySet()) {
                if (!brokerLiveStatus.containsKey(clusterName)) {
                    Map<String, Boolean> status = new HashMap<>();
                    status.put(broker.getKey(), broker.getValue().isHealthy());
                    brokerLiveStatus.put(clusterName, status);
                } else {
                    brokerLiveStatus.get(clusterName).put(broker.getKey(), broker.getValue().isHealthy());
                }
            }
        }

        return brokerLiveStatus;
    }

    class RoleChangeInfo {
        RoleInChange newMaster = null;
        RoleInChange oldMaster = null;

        @Override
        public String toString() {
            return "RoleChangeInfo{" +
                "newMaster=" + newMaster +
                ", oldMaster=" + oldMaster +
                '}';
        }
    }

    class RoleInChange {
        String addr;
        long oldId;
        long expectId;

        public RoleInChange(String addr, long oldId, long expectId) {
            this.addr = addr;
            this.oldId = oldId;
            this.expectId = expectId;
        }

        @Override
        public String toString() {
            return "RoleInChange{" +
                "addr='" + addr + '\'' +
                ", oldId=" + oldId +
                ", expectId=" + expectId +
                '}';
        }
    }
}
