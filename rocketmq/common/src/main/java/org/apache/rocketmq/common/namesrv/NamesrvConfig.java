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

/**
 * $Id: NamesrvConfig.java 1839 2013-05-16 02:12:02Z vintagewang@apache.org $
 */
package org.apache.rocketmq.common.namesrv;

import java.io.File;
import org.apache.rocketmq.common.MixAll;
import org.apache.rocketmq.common.constant.LoggerName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NamesrvConfig {
    private static final Logger log = LoggerFactory.getLogger(LoggerName.NAMESRV_LOGGER_NAME);

    private String rocketmqHome = System.getProperty(MixAll.ROCKETMQ_HOME_PROPERTY, System.getenv(MixAll.ROCKETMQ_HOME_ENV));
    private String kvConfigPath = System.getProperty("user.home") + File.separator + "namesrv" + File.separator + "kvConfig.json";
    private String configStorePath = System.getProperty("user.home") + File.separator + "namesrv" + File.separator + "namesrv.properties";
    private String productEnvName = "center";
    private boolean clusterTest = false;
    private boolean orderMessageEnable = false;
    private boolean roleSwitchEnable = false;
    private boolean roleAutoSwitchEnable = false;
    private int maxIdForRoleSwitch = 99;
    private String clusterName = null;
    private String zkPath = null;
    private String masterType = "SYNC_MASTER";
    private long detectIntervalMs = 5 * 1000;
    private long enableValidityPeriodMs = 3 * 5 * 1000;
    private long detectMessageSendTimeoutMs = 500;
    private int roleCheckTimesMax = 10;
    private long roleCheckWaitMs = 100;
    private int detectRecordCount = 10;
    private int lastContinuousFailCount = 5;
    private float unhealthyRateNsDetect = 0.6f;
    private float unhealthyRateAllNs = 2 / 3.0f;

    public boolean isOrderMessageEnable() {
        return orderMessageEnable;
    }

    public void setOrderMessageEnable(boolean orderMessageEnable) {
        this.orderMessageEnable = orderMessageEnable;
    }

    public String getRocketmqHome() {
        return rocketmqHome;
    }

    public void setRocketmqHome(String rocketmqHome) {
        this.rocketmqHome = rocketmqHome;
    }

    public String getKvConfigPath() {
        return kvConfigPath;
    }

    public void setKvConfigPath(String kvConfigPath) {
        this.kvConfigPath = kvConfigPath;
    }

    public String getProductEnvName() {
        return productEnvName;
    }

    public void setProductEnvName(String productEnvName) {
        this.productEnvName = productEnvName;
    }

    public boolean isClusterTest() {
        return clusterTest;
    }

    public void setClusterTest(boolean clusterTest) {
        this.clusterTest = clusterTest;
    }

    public String getConfigStorePath() {
        return configStorePath;
    }

    public void setConfigStorePath(final String configStorePath) {
        this.configStorePath = configStorePath;
    }

    public boolean isRoleSwitchEnable() {
        return roleSwitchEnable;
    }

    public void setRoleSwitchEnable(boolean roleSwitchEnable) {
        this.roleSwitchEnable = roleSwitchEnable;
    }

    public boolean isRoleAutoSwitchEnable() {
        return roleAutoSwitchEnable;
    }

    public void setRoleAutoSwitchEnable(boolean roleAutoSwitchEnable) {
        this.roleAutoSwitchEnable = roleAutoSwitchEnable;
    }

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public String getZkPath() {
        return zkPath;
    }

    public void setZkPath(String zkPath) {
        this.zkPath = zkPath;
    }

    public String getMasterType() {
        return masterType;
    }

    public void setMasterType(String masterType) {
        this.masterType = masterType;
    }

    public long getDetectIntervalMs() {
        return detectIntervalMs;
    }

    public void setDetectIntervalMs(long detectIntervalMs) {
        this.detectIntervalMs = detectIntervalMs;
    }

    public long getEnableValidityPeriodMs() {
        return enableValidityPeriodMs;
    }

    public void setEnableValidityPeriodMs(long enableValidityPeriodMs) {
        this.enableValidityPeriodMs = enableValidityPeriodMs;
    }

    public long getDetectMessageSendTimeoutMs() {
        return detectMessageSendTimeoutMs;
    }

    public void setDetectMessageSendTimeoutMs(long detectMessageSendTimeoutMs) {
        this.detectMessageSendTimeoutMs = detectMessageSendTimeoutMs;
    }

    public int getRoleCheckTimesMax() {
        return roleCheckTimesMax;
    }

    public void setRoleCheckTimesMax(int roleCheckTimesMax) {
        this.roleCheckTimesMax = roleCheckTimesMax;
    }

    public long getRoleCheckWaitMs() {
        return roleCheckWaitMs;
    }

    public void setRoleCheckWaitMs(long roleCheckWaitMs) {
        this.roleCheckWaitMs = roleCheckWaitMs;
    }

    public int getDetectRecordCount() {
        return detectRecordCount;
    }

    public void setDetectRecordCount(int detectRecordCount) {
        this.detectRecordCount = detectRecordCount;
    }

    public int getLastContinuousFailCount() {
        return lastContinuousFailCount;
    }

    public void setLastContinuousFailCount(int lastContinuousFailCount) {
        this.lastContinuousFailCount = lastContinuousFailCount;
    }

    public float getUnhealthyRateNsDetect() {
        return unhealthyRateNsDetect;
    }

    public void setUnhealthyRateNsDetect(float unhealthyRateNsDetect) {
        this.unhealthyRateNsDetect = unhealthyRateNsDetect;
    }

    public float getUnhealthyRateAllNs() {
        return unhealthyRateAllNs;
    }

    public void setUnhealthyRateAllNs(float unhealthyRateAllNs) {
        this.unhealthyRateAllNs = unhealthyRateAllNs;
    }

    public int getMaxIdForRoleSwitch() {
        return maxIdForRoleSwitch;
    }

    public void setMaxIdForRoleSwitch(int maxIdForRoleSwitch) {
        this.maxIdForRoleSwitch = maxIdForRoleSwitch;
    }
}
