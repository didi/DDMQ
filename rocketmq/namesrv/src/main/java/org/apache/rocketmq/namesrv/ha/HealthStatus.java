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

import org.apache.rocketmq.namesrv.NamesrvController;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class HealthStatus {
    private ConcurrentHashMap<String/*cluster name*/, Map<String/*broker name*/, NodeHealthStatus>> clusterStatus = new ConcurrentHashMap<>(8);
    private NamesrvController namesrvController;

    public HealthStatus(NamesrvController namesrvController) {
        this.namesrvController = namesrvController;
    }

    public void updateHealthStatus(String clusterName, String brokerName, boolean status) {
        if (clusterStatus.get(clusterName) == null) {
            Map<String, NodeHealthStatus> brokerInfo = new HashMap<>(16);
            NodeHealthStatus nodeHealthStatus = new NodeHealthStatus();
            brokerInfo.put(brokerName, nodeHealthStatus);
            clusterStatus.put(clusterName, brokerInfo);
        } else if (clusterStatus.get(clusterName).get(brokerName) == null) {
            NodeHealthStatus nodeHealthStatus = new NodeHealthStatus();
            clusterStatus.get(clusterName).put(brokerName, nodeHealthStatus);
        } else {
            clusterStatus.get(clusterName).get(brokerName).updateStatus(status);
        }
    }

    public void reset(String clusterName, String brokerName) {
        if (clusterStatus.get(clusterName) != null) {
            if (clusterStatus.get(clusterName).get(brokerName) != null) {
                clusterStatus.get(clusterName).get(brokerName).reset();
            }
        }
    }

    public void deleteDead() {
        for (Map.Entry<String, Map<String/*broker name*/, NodeHealthStatus>> cluster : clusterStatus.entrySet()) {
            for (Map.Entry<String, NodeHealthStatus> broker : cluster.getValue().entrySet()) {
                if (broker.getValue().isDead()) {
                    cluster.getValue().remove(broker.getKey());
                }
            }

            if (cluster.getValue().isEmpty()) {
                clusterStatus.remove(cluster.getKey());
            }
        }
    }

    public Map<String, Map<String, NodeHealthStatus>> getClusterStatus() {
        return clusterStatus;
    }

    class NodeHealthStatus {
        private Boolean[] detectStatus = new Boolean[namesrvController.getNamesrvConfig().getDetectRecordCount()];
        private long lastDetectTime = System.currentTimeMillis();
        private long indexUpdated = 0;

        public NodeHealthStatus() {
            for (int i = 0; i < namesrvController.getNamesrvConfig().getDetectRecordCount(); i++) {
                detectStatus[i] = true;
            }
        }

        public void updateStatus(boolean status) {
            detectStatus[(int) (indexUpdated++ % namesrvController.getNamesrvConfig().getDetectRecordCount())] = status;
            lastDetectTime = System.currentTimeMillis();
        }

        public boolean isHealthy() {
            if (System.currentTimeMillis() - lastDetectTime >
                namesrvController.getNamesrvConfig().getDetectRecordCount() * namesrvController.getNamesrvConfig().getDetectIntervalMs()) {
                reset();
                return true;
            }

            boolean isLatestContinuousFailCountMatch = true;
            int failedCount = 0;
            for (int i = namesrvController.getNamesrvConfig().getDetectRecordCount() - 1; i >= 0; i--) {
                if ((namesrvController.getNamesrvConfig().getDetectRecordCount() - i <= namesrvController.getNamesrvConfig().getLastContinuousFailCount())
                    && detectStatus[i]) {
                    isLatestContinuousFailCountMatch = false;
                }
                if (!detectStatus[i]) {
                    failedCount++;
                }
            }

            if (failedCount > namesrvController.getNamesrvConfig().getDetectRecordCount() * namesrvController.getNamesrvConfig().getUnhealthyRateNsDetect()
                || isLatestContinuousFailCountMatch) {
                return false;
            }
            return true;
        }

        public boolean isDead() {
            if (System.currentTimeMillis() - lastDetectTime >
                (namesrvController.getNamesrvConfig().getDetectRecordCount() / 2) * namesrvController.getNamesrvConfig().getDetectIntervalMs()) {
                return true;
            }
            return false;
        }

        public void reset() {
            for (int i = 0; i < namesrvController.getNamesrvConfig().getDetectRecordCount(); i++) {
                detectStatus[i] = true;
            }
            lastDetectTime = System.currentTimeMillis();
        }
    }
}


