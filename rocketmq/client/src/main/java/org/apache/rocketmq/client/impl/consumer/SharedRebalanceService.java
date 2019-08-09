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
package org.apache.rocketmq.client.impl.consumer;

import io.netty.util.internal.ConcurrentSet;
import java.util.Set;
import org.apache.rocketmq.client.impl.factory.MQClientInstance;
import org.apache.rocketmq.client.log.ClientLogger;
import org.apache.rocketmq.common.ServiceThread;
import org.slf4j.Logger;

public class SharedRebalanceService extends ServiceThread {
    private static long waitInterval =
        Long.parseLong(System.getProperty(
            "rocketmq.client.rebalance.waitInterval", "20000"));
    private final Logger log = ClientLogger.getLog();
    private final Set<MQClientInstance> mqClientInstanceMap = new ConcurrentSet<MQClientInstance>();

    public SharedRebalanceService() {}

    public void add(MQClientInstance mqClientFactory) {
        this.mqClientInstanceMap.add(mqClientFactory);
    }

    public void remove(MQClientInstance mqClientFactory) {
        this.mqClientInstanceMap.remove(mqClientFactory);
    }

    @Override
    public void run() {
        log.info(this.getServiceName() + " service started");

        while (!this.isStopped()) {
            this.waitForRunning(waitInterval);
            for (MQClientInstance mqClientInstance : mqClientInstanceMap) {
                mqClientInstance.doRebalance();
            }
        }

        log.info(this.getServiceName() + " service end");
    }

    @Override
    public String getServiceName() {
        return SharedRebalanceService.class.getSimpleName();
    }
}
