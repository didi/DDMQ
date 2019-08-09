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

import org.apache.rocketmq.common.MixAll;
import org.apache.rocketmq.common.constant.ConfigName;
import org.apache.rocketmq.common.constant.LoggerName;
import org.apache.rocketmq.common.protocol.route.BrokerData;
import org.apache.rocketmq.namesrv.NamesrvController;
import org.apache.rocketmq.remoting.exception.RemotingConnectException;
import org.apache.rocketmq.remoting.exception.RemotingSendRequestException;
import org.apache.rocketmq.remoting.exception.RemotingTimeoutException;
import org.apache.rocketmq.store.config.BrokerRole;
import org.apache.rocketmq.tools.admin.DefaultMQAdminExt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Properties;




public class RoleManager {
    private static final Logger log = LoggerFactory.getLogger(LoggerName.NAMESRV_LOGGER_NAME);
    private static final long ADMIN_TIMEOUT = 20 * 1000;

    private DefaultMQAdminExt defaultMQAdminExt;
    private NamesrvController namesrvController;

    public RoleManager(NamesrvController namesrvController) {
        this.namesrvController = namesrvController;
        defaultMQAdminExt = new DefaultMQAdminExt(ADMIN_TIMEOUT);
    }

    public void start() throws Exception {
        defaultMQAdminExt.setNamesrvAddr("127.0.0.1:9876");
        defaultMQAdminExt.start();
    }

    public void shutdown() {
        defaultMQAdminExt.shutdown();
    }

    public boolean change2Master(String brokerName, String brokerAddr, boolean isGuaranteed) {
        BrokerRole role = BrokerRole.valueOf(namesrvController.getNamesrvConfig().getMasterType());
        return changeRoleAndId(brokerName, brokerAddr, MixAll.MASTER_ID, role, isGuaranteed);
    }

    public boolean change2Slave(String brokerName, String brokerAddr, long newId, boolean isGuaranteed) {
        return changeRoleAndId(brokerName, brokerAddr, newId, BrokerRole.SLAVE, isGuaranteed);
    }

    //only for slave
    public boolean changeId(String brokerName, String brokerAddr, long newId, boolean isGuaranteed) {
        return changeRoleAndId(brokerName, brokerAddr, newId, BrokerRole.SLAVE, isGuaranteed);
    }

    public boolean changeRoleAndId(String brokerName, String brokerAddr, long newId, BrokerRole newRole,
        boolean isGuaranteed) {
        Properties properties = new Properties();
        properties.put(ConfigName.BROKER_ROLE, newRole);
        properties.put(ConfigName.BROKER_ID, newId);

        if (isGuaranteed) {
            try {
                defaultMQAdminExt.updateBrokerConfig(brokerAddr, properties);
            } catch (Exception ex) {
                log.warn("change role failed, unknown exception, stop. brokerAddr:{}, id:{}, role:{}", brokerAddr, newId, newRole, ex);
                return false;
            }
        } else {
            try {
                defaultMQAdminExt.updateBrokerConfig(brokerAddr, properties);
            } catch (RemotingConnectException | RemotingSendRequestException | RemotingTimeoutException ex) {
                log.warn("change role failed, broker maybe offline, brokerAddr:{}, id:{}, role:{}", brokerAddr, newId, newRole, ex);
            } catch (Exception ex) {
                log.warn("change role failed, unknown exception, stop. brokerAddr:{}, id:{}, role:{}", brokerAddr, newId, newRole, ex);
                return false;
            }
        }

        //check status if updated
        boolean isRoleUpdated = false;
        for (int i = 0; i < namesrvController.getNamesrvConfig().getRoleCheckTimesMax(); i++) {
            try {
                Thread.sleep(namesrvController.getNamesrvConfig().getRoleCheckWaitMs());
            } catch (Exception ex) {
                log.warn("exception in sleep", ex);
            }
            BrokerData brokerData = namesrvController.getRouteInfoManager().getBrokerData(brokerName);
            if (brokerData == null || brokerData.getBrokerAddrs() == null) {
                continue;
            }
            for (Map.Entry<Long, String> entry : brokerData.getBrokerAddrs().entrySet()) {
                if (brokerAddr.equals(entry.getValue())) {
                    if (newId != entry.getKey()) {
                        log.info("brokerAddr:{} of brokerName:{} change to be:{}", brokerAddr, brokerName, entry.getKey());
                        isRoleUpdated = true;
                        break;
                    } else {
                        continue;
                    }
                }
            }
        }

        //delete old info if not update
        if (!isRoleUpdated) {
            namesrvController.getRouteInfoManager().destroyByBrokerAddr(brokerAddr);
            log.info("destroy info or broker addr:{}", brokerAddr);
        }

        log.info("brokerName:{}, brokerAddr{}, change to {}, id:{}", brokerName, brokerAddr, newRole, newId);
        return true;
    }
}
