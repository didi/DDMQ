package com.xiaojukeji.carrera.monitor.inspection;

import com.xiaojukeji.carrera.monitor.BaseConfigMonitor;
import com.xiaojukeji.carrera.monitor.config.MonitorConfig;
import com.xiaojukeji.carrera.monitor.utils.Const;
import com.xiaojukeji.carrera.monitor.utils.ExecutorUtils;
import com.xiaojukeji.carrera.config.v4.BrokerConfig;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;

import java.util.Collection;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.slf4j.LoggerFactory.getLogger;


public class InspectionMonitor extends BaseConfigMonitor {

    private static final Logger LOGGER = getLogger(InspectionMonitor.class);

    private CarreraDataInspection inspection = null;

    private ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(100);

    public InspectionMonitor(MonitorConfig monitorConfig) {
        super("Inspection", monitorConfig);
    }

    @Override
    protected void initMonitor(String broker, BrokerConfig brokerConfig) throws Exception {
        doMonitor(broker, brokerConfig);
    }

    private void doMonitor(String broker, BrokerConfig config) throws InterruptedException {
        LOGGER.info("broker={}, pproxy={}, cproxy={}", broker, config.getPproxies(), config.getCproxies());

        if (MapUtils.isEmpty(config.getCproxies()) || MapUtils.isEmpty(config.getPproxies())) {
            LOGGER.info("broker<{}> pproxy or cproxy is null, ignore monitor", broker);
            return;
        }

        if (inspection != null) {
            LOGGER.info("Stop old inspection broker<{}>", broker);
            inspection.stop();
        }

        LOGGER.info("Start inspection broker<{}>", broker);

        ClusterConfig clusterConfig = new ClusterConfig();
        clusterConfig.setTopic(Const.DEFAULT_INSPECTION_TOPIC);
        clusterConfig.setGroup(Const.DEFAULT_INSPECTION_GROUP);
        clusterConfig.setPproxyServers(config.getPproxies().values().stream().flatMap(Collection::stream).collect(Collectors.toSet()));
        clusterConfig.setCproxyServers(config.getCproxies().values().stream().flatMap(Collection::stream).collect(Collectors.toSet()));
        CarreraDataInspection inspection = new CarreraDataInspection(broker, clusterConfig, scheduledExecutorService);

        ScheduledFuture scheduledFuture = changeMonitorBrokerSingleExecutor.scheduleAtFixedRate(() -> {
            try {
                inspection.logMetric();
            } catch (Throwable t) {
                LOGGER.error("error in logMetric.", t);
            }
        }, 1, 1, TimeUnit.MINUTES);

        inspection.setScheduledFuture(scheduledFuture);

        try {
            inspection.start();
        } catch (Exception e) {
            LOGGER.error("inspection start exception", e);
        }
    }

    @Override
    public void shutdown() {
        if (inspection != null) {
            inspection.stop();
        }
        ExecutorUtils.shutdown(scheduledExecutorService);
        super.shutdown();
    }
}
