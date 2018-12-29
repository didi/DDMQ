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

package org.apache.rocketmq.tools.admin;

import java.util.List;
import java.util.Map;
import org.apache.rocketmq.client.impl.MQClientManager;
import org.apache.rocketmq.client.impl.factory.MQClientInstance;
import org.apache.rocketmq.common.MixAll;
import org.apache.rocketmq.common.admin.ConsumeStats;
import org.apache.rocketmq.common.admin.OffsetWrapper;
import org.apache.rocketmq.common.admin.RollbackStats;
import org.apache.rocketmq.common.admin.TopicOffset;
import org.apache.rocketmq.common.admin.TopicStatsTable;
import org.apache.rocketmq.common.message.MessageQueue;
import org.apache.rocketmq.common.protocol.body.ConsumeStatsList;
import org.apache.rocketmq.common.protocol.route.BrokerData;
import org.apache.rocketmq.common.protocol.route.QueueData;
import org.apache.rocketmq.common.protocol.route.TopicRouteData;
import org.junit.After;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;



public class DefaultMQAdminExtImplTest {
    private DefaultMQAdminExt mqAdmin;


    private String topic = "carrera_inspection";
    private String group = "cg_carrera_inspection";

    @Before
    public void setUp() throws Exception {
        mqAdmin = new DefaultMQAdminExt();
        mqAdmin.setNamesrvAddr("127.0.0.1:9876;127.0.0.2:9876");
        mqAdmin.start();
    }

    @After
    public void tearDown() throws Exception {
        mqAdmin.shutdown();
    }

    @Test
    public void testResetOffset() throws Exception {

        TopicRouteData routeInfo = mqAdmin.examineTopicRouteInfo(topic);
        System.out.println();
        for (BrokerData brokerData : routeInfo.getBrokerDatas()) {
            System.out.println(brokerData);
        }
        System.out.println();
        for (QueueData data : routeInfo.getQueueDatas()) {
            MessageQueue mq = new MessageQueue(topic, data.getBrokerName(), 0);
            long offset = mqAdmin.searchOffset(mq, System.currentTimeMillis());
            System.out.println(data + ", offset:" + offset);
        }
        System.out.println();
        List<RollbackStats> r = mqAdmin.resetOffsetByTimestampOld(group, topic, System.currentTimeMillis(), true);
        for (RollbackStats rollbackStats : r) {
            System.out.println(rollbackStats);
        }

    }

    @Test
    public void testNewGroup() throws Exception {
        group = "cg_test_new_rmq_group";
        topic = "test-2";
        TopicStatsTable ts = mqAdmin.examineTopicStats(topic);
        for (Map.Entry<MessageQueue, TopicOffset> messageQueueTopicOffsetEntry : ts.getOffsetTable().entrySet()) {
            System.out.println(messageQueueTopicOffsetEntry);
        }


        TopicRouteData routeInfo = mqAdmin.examineTopicRouteInfo(topic);
        System.out.println();
        MQClientInstance instance = MQClientManager.getInstance().getAndCreateMQClientInstance(mqAdmin);


        for (BrokerData brokerData : routeInfo.getBrokerDatas()) {
            ConsumeStats r = instance.getMQClientAPIImpl().getConsumeStats(brokerData.selectBrokerAddr(), group, topic, 3000);
            System.out.println(brokerData + "consumeStats:" + r.getOffsetTable());
        }

//        List<RollbackStats> r = mqAdmin.resetOffsetByTimestampOld(group, topic, -1, true);
//        for (RollbackStats rollbackStats : r) {
//            System.out.println(rollbackStats);
//        }

//        ConsumeStats cs = mqAdmin.examineConsumeStats(group, topic);
//        for (Map.Entry<MessageQueue, OffsetWrapper> messageQueueOffsetWrapperEntry : cs.getOffsetTable().entrySet()) {
//            System.out.println(messageQueueOffsetWrapperEntry);
//        }
    }
}