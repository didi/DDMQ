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

package org.apache.rocketmq.common;

import com.alibaba.fastjson.JSON;
import org.junit.Test;



public class TopicConfigTest {
    @Test
    public void name() throws Exception {
        TopicConfig topicConfig = new TopicConfig();
        topicConfig.setTopicName("t-name");
        topicConfig.setReadQueueNums(8);
        topicConfig.setWriteQueueNums(2);
        topicConfig.setPerm(2);
        System.out.println(topicConfig);

        String json = JSON.toJSONString(topicConfig);
        System.out.println(json);

        System.out.println(JSON.parseObject(json, TopicConfig.class));

        json = "{\n" +
            "                        \"order\":false,\n" +
            "                        \"perm\":6,\n" +
            "                        \"readQueueNums\":2,\n" +
            "                        \"topicFilterType\":\"SINGLE_TAG\",\n" +
            "                        \"topicName\":\"%RETRY%cg_iapetos\",\n" +
            "                        \"topicSysFlag\":0,\n" +
            "                        \"writeQueueNums\":4\n" +
            "                }";
        System.out.println(JSON.parseObject(json, TopicConfig.class));
    }
}