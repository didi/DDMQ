package com.xiaojukeji.carrera.monitor;

import com.xiaojukeji.carrera.monitor.config.MonitorConfig;
import com.xiaojukeji.carrera.monitor.utils.ExecutorUtils;
import com.xiaojukeji.carrera.biz.ZkService;
import com.xiaojukeji.carrera.biz.ZkServiceImpl;
import com.xiaojukeji.carrera.config.v4.BrokerConfig;
import com.xiaojukeji.carrera.dynamic.ParameterDynamicConfig;
import com.xiaojukeji.carrera.dynamic.ParameterDynamicZookeeper;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;


public abstract class BaseConfigMonitor {

    private static final Logger LOGGER = LoggerFactory.getLogger(BaseConfigMonitor.class);

    private MonitorConfig monitorConfig;

    protected ZkService zkService;

    protected ScheduledExecutorService changeMonitorBrokerSingleExecutor;

    private String monitorType;

    protected BrokerConfig brokerConfig;

    private void startWatchBroker(String broker) throws Exception {

        zkService.getAndWatchBroker(broker, new ParameterDynamicZookeeper.DataChangeCallback<BrokerConfig>() {
            @Override
            public void handleDataChange(String dataPath, BrokerConfig data, Stat stat) throws Exception {
                // update config and restart monitor
                brokerConfig = data;
                initMonitor(data.getBrokerCluster(), data);
            }

            @Override
            public void handleDataDeleted(String dataPath) throws Exception {
                // stop monitor

            }
        });
    }

    public BaseConfigMonitor(String monitorType, MonitorConfig monitorConfig) {
        this.monitorType = monitorType;
        this.monitorConfig = monitorConfig;
    }

    public synchronized void start() throws Exception {
        ParameterDynamicConfig dynamicConfig = new ParameterDynamicConfig(monitorConfig.getZookeeperAddr());
        LOGGER.info("dynamicConfig, zk:{}, configCentre:{}", dynamicConfig.getZooKeeperHost(), dynamicConfig.isConfigCentre());
        zkService = new ZkServiceImpl(monitorConfig.getZookeeperAddr(), false);
        changeMonitorBrokerSingleExecutor = Executors.newSingleThreadScheduledExecutor();

        startWatchBroker(monitorConfig.getBroker());
    }

    protected abstract void initMonitor(String broker, BrokerConfig brokerConfig) throws Exception;

    public void shutdown() {
        if (changeMonitorBrokerSingleExecutor != null) {
            ExecutorUtils.shutdown(changeMonitorBrokerSingleExecutor);
        }

        if (zkService != null) {
            zkService.shutdown();
        }
    }
}
