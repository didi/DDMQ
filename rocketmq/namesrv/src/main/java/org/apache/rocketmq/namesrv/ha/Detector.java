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

import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.impl.CommunicationMode;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.apache.rocketmq.common.MixAll;
import org.apache.rocketmq.common.constant.LoggerName;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageDecoder;
import org.apache.rocketmq.common.message.MessageQueue;
import org.apache.rocketmq.common.protocol.ResponseCode;
import org.apache.rocketmq.common.protocol.header.SendMessageRequestHeader;
import org.apache.rocketmq.common.protocol.route.BrokerData;
import org.apache.rocketmq.namesrv.NamesrvController;
import org.apache.rocketmq.namesrv.routeinfo.RouteInfoManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;


public class Detector {
    private static final Logger log = LoggerFactory.getLogger(LoggerName.NAMESRV_LOGGER_NAME);

    private final RouteInfoManager routeInfoManager;
    private final NamesrvController namesrvController;
    private DefaultMQProducer producer;
    private HealthStatus healthStatus;

    public Detector(NamesrvController namesrvController) {
        this.namesrvController = namesrvController;
        this.routeInfoManager = namesrvController.getRouteInfoManager();
        healthStatus = new HealthStatus(namesrvController);
    }

    public void start() throws Exception {
    }

    public HealthStatus detectLiveStat() throws Exception {
        if (producer == null) {
            producer = new DefaultMQProducer("BrokerDetector");
            producer.setNamesrvAddr("127.0.0.1:9876");
            producer.start();
        }

        if (routeInfoManager == null) {
            log.warn("routeInfoManager is null");
            return null;
        }

        HashMap<String, Set<String>> clusterAddrTable = routeInfoManager.getClusterAddrTable();
        for (Map.Entry<String, Set<String>> entry : clusterAddrTable.entrySet()) {
            String clusterName = entry.getKey();
            log.info("begin to detect cluster:{}", clusterName);
            for (String brokerName : entry.getValue()) {
                log.info("begin to detect broker:{}", brokerName);
                BrokerData brokerData = routeInfoManager.getBrokerData(brokerName);
                if (brokerData == null) {
                    log.warn("get broker data failed, broker name:{}", brokerName);
                    continue;
                }
                String brokerAddr = brokerData.getBrokerAddrs() == null ? null : brokerData.getBrokerAddrs().get(MixAll.MASTER_ID);
                if (brokerAddr == null) {
                    log.warn("no master of broker name:{}", brokerName);
                    healthStatus.updateHealthStatus(clusterName, brokerName, false);
                    continue;
                }

                String topicName = MixAll.LIVE_STATE_DETECT_TOPIC;
                Message msg = new Message(topicName, String.valueOf(System.currentTimeMillis()).getBytes());
                msg.setWaitStoreMsgOK(false);//do not wait sync to slave
                MessageQueue mq = new MessageQueue(topicName, brokerName, 0);
                SendMessageRequestHeader requestHeader = getMessageRequestHeader(msg, mq);

                boolean sendOk = true;
                try {
                    SendResult sendResult = producer.getDefaultMQProducerImpl().getmQClientFactory().getMQClientAPIImpl().sendMessage(
                        brokerAddr,
                        mq.getBrokerName(),
                        msg,
                        requestHeader,
                        namesrvController.getNamesrvConfig().getDetectMessageSendTimeoutMs(),
                        CommunicationMode.SYNC,
                        null,
                        null);
                    if (sendResult.getSendStatus() != SendStatus.SEND_OK) {
                        log.error("send message failed, status:{}", sendResult.getSendStatus());
                        sendOk = false;
                    }
                } catch (MQBrokerException brokerException) {
                    if (brokerException.getResponseCode() == ResponseCode.NO_PERMISSION || brokerException.getResponseCode() == ResponseCode.TOPIC_NOT_EXIST) {
                        log.warn("brokerAddr {} is not writable, cause:{}", brokerAddr, brokerException.getErrorMessage());
                    } else {
                        log.error("send message failed", brokerException);
                        sendOk = false;
                    }
                } catch (Exception ex) {
                    log.error("send message failed", ex);
                    sendOk = false;
                }

                healthStatus.updateHealthStatus(clusterName, brokerName, sendOk);
            }
            log.info("detect cluster:{} end", clusterName);
        }

        //delete not in routeInfoManager
        healthStatus.deleteDead();
        return healthStatus;
    }

    public HealthStatus getHealthStatus() {
        return healthStatus;
    }

    public void reset(String clusterName, String brokerName) {
        healthStatus.reset(clusterName, brokerName);
    }

    private SendMessageRequestHeader getMessageRequestHeader(Message msg, MessageQueue mq) {
        SendMessageRequestHeader requestHeader = new SendMessageRequestHeader();
        requestHeader.setProducerGroup(producer.getProducerGroup());
        requestHeader.setTopic(msg.getTopic());
        requestHeader.setDefaultTopic(producer.getCreateTopicKey());
        requestHeader.setDefaultTopicQueueNums(producer.getDefaultTopicQueueNums());
        requestHeader.setQueueId(mq.getQueueId());
        requestHeader.setSysFlag(0);
        requestHeader.setBornTimestamp(System.currentTimeMillis());
        requestHeader.setFlag(msg.getFlag());
        requestHeader.setProperties(MessageDecoder.messageProperties2String(msg.getProperties()));
        requestHeader.setReconsumeTimes(0);
        requestHeader.setUnitMode(producer.isUnitMode());
        return requestHeader;
    }

    public void shutdown() {
        if (producer != null) {
            producer.shutdown();
        }
    }
}
