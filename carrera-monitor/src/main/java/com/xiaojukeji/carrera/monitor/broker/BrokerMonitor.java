
package com.xiaojukeji.carrera.monitor.broker;

import com.xiaojukeji.carrera.monitor.config.MonitorConfig;
import com.xiaojukeji.carrera.monitor.BaseConfigMonitor;
import com.xiaojukeji.carrera.monitor.utils.ExecutorUtils;
import com.xiaojukeji.carrera.config.v4.BrokerConfig;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class BrokerMonitor extends BaseConfigMonitor {

    private final static Logger LOGGER = LoggerFactory.getLogger(BrokerMonitor.class);

    private ExecutorService executor = ExecutorUtils.newFixedThreadPool(100, "BrokerMonitor", 200);

    private BrokerMonitorItem monitorItem = null;

    @Override
    protected void initMonitor(String broker, BrokerConfig brokerConfig) throws Exception {
        doMonitor(broker, brokerConfig);
    }

    public BrokerMonitor(MonitorConfig monitorConfig) {
        super("Broker", monitorConfig);
    }

    private void doMonitor(String broker, BrokerConfig config) throws InterruptedException {
        if (monitorItem != null) {
            // stop first.
            LOGGER.info("Stop old monitor broker: {}", broker);
            monitorItem.stop();
        }

        BrokerMonitorItem item = new BrokerMonitorItem(broker, config);
        try {
            item.start();
        } catch (Exception e) {
            LOGGER.error("broker monitor start exception, broker=" + broker, e);
        }
    }

    @Override
    public void shutdown() {
        ExecutorUtils.shutdown(executor);
        monitorItem.stop();
        super.shutdown();
    }

    class BrokerMonitorItem {
        private String broker;
        private BrokerConfig config;
        private volatile boolean isRunning = false;
        private ScheduledExecutorService scheduledExecutor = Executors.newSingleThreadScheduledExecutor();

        public BrokerMonitorItem(String broker, BrokerConfig config) {
            this.broker = broker;
            this.config = config;
        }

        public void start() {
            isRunning = true;

            scheduledExecutor.submit(() -> {
                while (isRunning) {

                    monitorNamesvr();
                    monitorBroker();

                    try {
                        Thread.sleep(10000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    LOGGER.info("broker<{}> [Active]", broker);
                }
            });
        }

        public void stop() {
            isRunning = false;
            ExecutorUtils.shutdown(scheduledExecutor);
        }

        private void monitorBroker() {
            if (MapUtils.isEmpty(config.getBrokers()) || StringUtils.isBlank(config.getBrokerClusterAddrs())) {

                return;
            }

            String nameSvr = config.getBrokerClusterAddrs().split(";")[0]; // use first namesvr.
            for (Map.Entry<String, Set<String>> entry : config.getBrokers().entrySet()) {
                String master = entry.getKey();
                Set<String> slaves = entry.getValue();
                executor.execute(() -> {
                    int j = 0;
                    for (; j < 2; ++j) {
                        try {
                            long masterOffset = Utils.checkReceive(broker, nameSvr, master);
                            if (masterOffset <= 0) {
                                continue;
                            }
                            Utils.checkSend(broker, nameSvr, master);
                            if (CollectionUtils.isNotEmpty(slaves)) {
                                for (String slave : slaves) {
                                    long slaveOffset = Utils.checkReceive(broker, nameSvr, slave);
                                    LOGGER.info("ReplicaDelayCheck broker={}, address={}->{}, masterOffset={}, slaveOffset={}, delayNum={}", broker, master.split(":")[0], slave.split(":")[0], masterOffset, slaveOffset, (masterOffset - slaveOffset));

                                    if (slaveOffset <= 0) {
                                        continue;
                                    }
                                    if (masterOffset - slaveOffset > 60) {
                                        LOGGER.error(String.format("[AlarmReplicaDelayErr] broker=%s, address=%s->%s, delayNum=%s", broker, master.split(":")[0], slave.split(":")[0], (masterOffset - slaveOffset)));
                                    }
                                }
                            }
                            break;
                        } catch (Throwable e) {
                            LOGGER.error("broker check broker exception, broker=" + broker, e);
                        }
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                        }
                    }
                    if (j == 2) {
                        LOGGER.error(String.format("[AlarmCheckBrokerErr] broker=%s, namesvr=%s", broker, nameSvr));
                    }
                });
            }
        }

        private void monitorNamesvr() {
            if (StringUtils.isBlank(config.getBrokerClusterAddrs())) {
                LOGGER.info("broker:{}, brokerClusterAddrs is empty", config.getBrokerCluster());
                return;
            }

            for (String nameSvr : config.getBrokerClusterAddrs().split(";")) {
                executor.execute(() -> {
                    int j = 0;
                    for (; j < 2; ++j) {

                        try {
                            Utils.checkNameSvr(nameSvr, broker);
                            LOGGER.info(String.format("[NameSvrCheck] broker=%s, namesvr=%s", broker, nameSvr));
                            break;
                        } catch (Throwable e) {
                            LOGGER.error("broker checkNameSvr exception, broker=" + broker + ", namesvr=" + nameSvr, e);
                        }
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            LOGGER.error("broker checkNameSvr Thread.sleep exception, broker=" + broker, e);
                        }
                    }
                    if (j == 2) {
                        LOGGER.error(String.format("[AlarmNameSvrErr] broker=%s, namesvr=%s", broker, nameSvr));
                    }
                });
            }
        }
    }
}
