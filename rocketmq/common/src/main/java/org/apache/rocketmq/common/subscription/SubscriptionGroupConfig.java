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

package org.apache.rocketmq.common.subscription;

import com.alibaba.fastjson.annotation.JSONField;
import org.apache.rocketmq.common.MixAll;

public class SubscriptionGroupConfig {

    @JSONField(serialize = false)
    private String groupName;

    public String getG() {
        return groupName;
    }

    public void setG(String groupName) {
        this.groupName = groupName;
    }

    @JSONField(serialize = false)
    private boolean consumeEnable = true;

    public Boolean getCe() {
        return consumeEnable ? null : Boolean.FALSE;
    }

    public void setCe(boolean consumeEnable) {
        this.consumeEnable = consumeEnable;
    }

    @JSONField(serialize = false)
    private boolean consumeFromMinEnable = true;

    public Boolean getCme() {
        return consumeFromMinEnable ? null : Boolean.FALSE;
    }

    public void setCme(boolean consumeFromMinEnable) {
        this.consumeFromMinEnable = consumeFromMinEnable;
    }

    @JSONField(serialize = false)
    private boolean consumeBroadcastEnable = true;

    public Boolean getCbe() {
        return consumeBroadcastEnable ? null : Boolean.FALSE;
    }

    public void setCbe(Boolean consumeBroadcastEnable) {
        this.consumeBroadcastEnable = consumeBroadcastEnable;
    }

    @JSONField(serialize = false)
    private int retryQueueNums = 1;

    public Integer getRq() {
        return retryQueueNums == 1 ? null : retryQueueNums;
    }

    public void setRq(int retryQueueNums) {
        this.retryQueueNums = retryQueueNums;
    }

    @JSONField(serialize = false)
    private int retryMaxTimes = 16;

    public Integer getRt() {
        return retryMaxTimes == 16 ? null : retryMaxTimes;
    }

    public void setR(int retryMaxTimes) {
        this.retryMaxTimes = retryMaxTimes;
    }

    @JSONField(serialize = false)
    private long brokerId = MixAll.MASTER_ID;

    public Long getB() {
        return brokerId == MixAll.MASTER_ID ? null : brokerId;
    }

    public void setB(long brokerId) {
        this.brokerId = brokerId;
    }

    @JSONField(serialize = false)
    private long whichBrokerWhenConsumeSlowly = 1;

    public Long getW() {
        return whichBrokerWhenConsumeSlowly == 1 ? null : whichBrokerWhenConsumeSlowly;
    }

    public void setW(Long whichBrokerWhenConsumeSlowly) {
        this.whichBrokerWhenConsumeSlowly = whichBrokerWhenConsumeSlowly;
    }

    @JSONField(serialize = false)
    private boolean notifyConsumerIdsChangedEnable = true;

    public Boolean getN() {
        return notifyConsumerIdsChangedEnable ? null : Boolean.FALSE;
    }

    public void setN(Boolean notifyConsumerIdsChangedEnable) {
        this.notifyConsumerIdsChangedEnable = notifyConsumerIdsChangedEnable;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public boolean isConsumeEnable() {
        return consumeEnable;
    }

    public void setConsumeEnable(boolean consumeEnable) {
        this.consumeEnable = consumeEnable;
    }

    public boolean isConsumeFromMinEnable() {
        return consumeFromMinEnable;
    }

    public void setConsumeFromMinEnable(boolean consumeFromMinEnable) {
        this.consumeFromMinEnable = consumeFromMinEnable;
    }

    public boolean isConsumeBroadcastEnable() {
        return consumeBroadcastEnable;
    }

    public void setConsumeBroadcastEnable(boolean consumeBroadcastEnable) {
        this.consumeBroadcastEnable = consumeBroadcastEnable;
    }

    public int getRetryQueueNums() {
        return retryQueueNums;
    }

    public void setRetryQueueNums(int retryQueueNums) {
        this.retryQueueNums = retryQueueNums;
    }

    public int getRetryMaxTimes() {
        return retryMaxTimes;
    }

    public void setRetryMaxTimes(int retryMaxTimes) {
        this.retryMaxTimes = retryMaxTimes;
    }

    public long getBrokerId() {
        return brokerId;
    }

    public void setBrokerId(long brokerId) {
        this.brokerId = brokerId;
    }

    public long getWhichBrokerWhenConsumeSlowly() {
        return whichBrokerWhenConsumeSlowly;
    }

    public void setWhichBrokerWhenConsumeSlowly(long whichBrokerWhenConsumeSlowly) {
        this.whichBrokerWhenConsumeSlowly = whichBrokerWhenConsumeSlowly;
    }

    public boolean isNotifyConsumerIdsChangedEnable() {
        return notifyConsumerIdsChangedEnable;
    }

    public void setNotifyConsumerIdsChangedEnable(final boolean notifyConsumerIdsChangedEnable) {
        this.notifyConsumerIdsChangedEnable = notifyConsumerIdsChangedEnable;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (brokerId ^ (brokerId >>> 32));
        result = prime * result + (consumeBroadcastEnable ? 1231 : 1237);
        result = prime * result + (consumeEnable ? 1231 : 1237);
        result = prime * result + (consumeFromMinEnable ? 1231 : 1237);
        result = prime * result + (notifyConsumerIdsChangedEnable ? 1231 : 1237);
        result = prime * result + ((groupName == null) ? 0 : groupName.hashCode());
        result = prime * result + retryMaxTimes;
        result = prime * result + retryQueueNums;
        result =
            prime * result + (int) (whichBrokerWhenConsumeSlowly ^ (whichBrokerWhenConsumeSlowly >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        SubscriptionGroupConfig other = (SubscriptionGroupConfig) obj;
        if (brokerId != other.brokerId)
            return false;
        if (consumeBroadcastEnable != other.consumeBroadcastEnable)
            return false;
        if (consumeEnable != other.consumeEnable)
            return false;
        if (consumeFromMinEnable != other.consumeFromMinEnable)
            return false;
        if (groupName == null) {
            if (other.groupName != null)
                return false;
        } else if (!groupName.equals(other.groupName))
            return false;
        if (retryMaxTimes != other.retryMaxTimes)
            return false;
        if (retryQueueNums != other.retryQueueNums)
            return false;
        if (whichBrokerWhenConsumeSlowly != other.whichBrokerWhenConsumeSlowly)
            return false;
        if (notifyConsumerIdsChangedEnable != other.notifyConsumerIdsChangedEnable)
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "SubscriptionGroupConfig [groupName=" + groupName + ", consumeEnable=" + consumeEnable
            + ", consumeFromMinEnable=" + consumeFromMinEnable + ", consumeBroadcastEnable="
            + consumeBroadcastEnable + ", retryQueueNums=" + retryQueueNums + ", retryMaxTimes="
            + retryMaxTimes + ", brokerId=" + brokerId + ", whichBrokerWhenConsumeSlowly="
            + whichBrokerWhenConsumeSlowly + ", notifyConsumerIdsChangedEnable="
            + notifyConsumerIdsChangedEnable + "]";
    }
}
