package com.xiaojukeji.carrera.cproxy.consumer;

import com.xiaojukeji.carrera.config.v4.CProxyConfig;
import com.xiaojukeji.carrera.cproxy.consumer.offset.CarreraOffsetManager;
import com.xiaojukeji.carrera.cproxy.concurrent.CarreraExecutors;
import com.xiaojukeji.carrera.cproxy.config.ConsumerGroupConfig;
import com.xiaojukeji.carrera.cproxy.utils.LogUtils;
import com.xiaojukeji.carrera.cproxy.utils.MetricUtils;
import com.xiaojukeji.carrera.cproxy.utils.StringUtils;
import com.xiaojukeji.carrera.cproxy.utils.TimeUtils;
import org.slf4j.Logger;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import static org.slf4j.LoggerFactory.getLogger;


public class ConsumerManager {

    public static final Logger LOGGER = getLogger(ConsumerManager.class);
    private static final Logger METRIC_LOGGER = LogUtils.METRIC_LOGGER;

    private final ScheduledExecutorService scheduler = CarreraExecutors.newScheduledThreadPool(4, "ConsumerPoolScheduler");
    private final ScheduledExecutorService hangCheckerScheduler = CarreraExecutors.newSingleThreadScheduledExecutor("HangChecker");
    private final Queue<Long> hangCheckerInfoQ = new ConcurrentLinkedQueue<>();
    private long lastHangCheckTime = TimeUtils.getCurTime();
    private long lastLogCostTime = 0;

    private Map<String/*group*/, ConsumerGroupManager> consumerMap = new ConcurrentHashMap<>();

    public void start() {
        startScheduledTask();
    }

    public synchronized void shutdown() {
        ExecutorService es = Executors.newFixedThreadPool(256);
        List<Future> shutdownFutures = consumerMap.values().stream().map(consumerGroupManager -> es.submit(consumerGroupManager::shutdown)).collect(Collectors.toList());
        for (Future future : shutdownFutures) {
            try {
                future.get();
            } catch (InterruptedException | ExecutionException e) {
                LogUtils.MAIN_LOGGER.error("shutdown failed.");
            }
        }
        consumerMap.clear();
        es.shutdown();
        scheduler.shutdown();
        hangCheckerScheduler.shutdown();
    }

    private void startScheduledTask() {
        hangCheckerScheduler.scheduleAtFixedRate(() -> hangCheckerInfoQ.add(TimeUtils.getCurTime()), 100, 100, TimeUnit.MILLISECONDS);
        scheduler.scheduleAtFixedRate(this::logAliveConsumers, 30, 30, TimeUnit.SECONDS);
        scheduler.scheduleAtFixedRate(this::logAliveInfo, 1, 1, TimeUnit.SECONDS);
        scheduler.scheduleAtFixedRate(this::logActionMetrics, 1, 1, TimeUnit.SECONDS);
        scheduler.scheduleAtFixedRate(this::logMonitorMetrics, 5, 10, TimeUnit.SECONDS);
        scheduler.scheduleAtFixedRate(this::checkConsumerTimeout, 10, ConsumerGroupManager.CONSUMER_KEEP_ALIVE_TIME / 1000, TimeUnit.SECONDS);
        scheduler.scheduleAtFixedRate(this::updateOffsetManager, 1, 10, TimeUnit.SECONDS);
    }

    private synchronized void logAliveConsumers() {
        consumerMap.forEach((key, value) -> LogUtils.logMainInfo("consumer group:{} is alive. consumer state:{}. contain consumers:{}",
                key, value.getState(), value.getConsumers()));
    }

    private void logAliveInfo() {
        StringBuilder sb = new StringBuilder();
        int cnt = 0;
        while (!hangCheckerInfoQ.isEmpty()) {
            cnt++;
            long t = hangCheckerInfoQ.poll();
            sb.append(t - lastHangCheckTime).append(",");
            lastHangCheckTime = t;
        }
        long start = TimeUtils.getCurTime();
        LOGGER.info("[CARRERA_CONSUMER_PROXY_ACTIVE],hangChecker[{}]:{} lastLogCostTime={}", cnt, sb, lastLogCostTime);
        LogUtils.METRIC_LOGGER.info("[CARRERA_CONSUMER_PROXY_ACTIVE],hangChecker[{}]:{} lastLogCostTime={}", cnt, sb, lastLogCostTime);
        lastLogCostTime = TimeUtils.getElapseTime(start);
    }

    private void logMonitorMetrics() {
        try {
            MetricUtils.monitorMetric.forEach((group, topicMap) ->
                    topicMap.forEach((topic, typeMap) ->
                            typeMap.forEach((type, value) ->
                                    METRIC_LOGGER.info("MonitorMetrics[group|topic:{}|{},type:{},value:{}]",
                                            group, topic, type, value.getAndSet(0)))));
        } catch (Exception e) {
            LOGGER.error("exception when logMonitorMetrics", e);
        }
    }

    private void logActionMetrics() {
        consumerMap.values().forEach(ConsumerGroupManager::logActionMetrics);
    }

    private synchronized void checkConsumerTimeout() {
        consumerMap.values().forEach(ConsumerGroupManager::tryShutdown);
    }

    private synchronized void updateOffsetManager() {
        List<CarreraConsumer> consumerList = consumerMap.values().stream().map(ConsumerGroupManager::getConsumers)
                .flatMap(Collection::stream).collect(Collectors.toList());
        CarreraOffsetManager.getInstance().updateConsumers(consumerList);
    }

    public void tryCreateConsumer(String group) {
        if (StringUtils.isEmpty(group)) {
            return;
        }

        ConsumerGroupManager consumerGroupMgr = consumerMap.get(group);
        if (consumerGroupMgr == null) {
            return;
        }

        consumerGroupMgr.tryStart();
    }

    public synchronized ConsumerGroupManager addOrUpdateConsumer(ConsumerGroupConfig config) {
        LogUtils.logMainInfo("ConsumerManager.addOrUpdateConsumer, group:{}, config:{}", config.getGroup(), config.getGroupConfig());
        String group = config.getGroup();

        ConsumerGroupManager consumerGroupMgr;
        if (consumerMap.containsKey(config.getGroup())) {
            consumerGroupMgr = consumerMap.get(group);
        } else {
            consumerGroupMgr = new ConsumerGroupManager(config);
        }

        consumerGroupMgr.updateConfig(config);

        consumerMap.put(group, consumerGroupMgr);
        return consumerGroupMgr;
    }

    public synchronized void stopAndRemoveConsumer(String group) {
        LogUtils.logMainInfo("ConsumerManager.stopAndRemoveConsumer, group:{}.", group);
        ConsumerGroupManager consumerGroupMgr = consumerMap.remove(group);
        if (consumerGroupMgr != null) {
            consumerGroupMgr.shutdown();
            consumerMap.remove(consumerGroupMgr.getGroup());
        }
    }

    public synchronized void updateCproxyConfig(CProxyConfig cProxyConfig) {
        LogUtils.logMainInfo("Update CproxyConfig for all consumers. consumer num:{}.", consumerMap.values().size());
        this.consumerMap.values().forEach(consumer -> {
            ConsumerGroupConfig newConf = consumer.getConfig().clone();
            CProxyConfig newCproxyConf = cProxyConfig.clone();
            newConf.setcProxyConfig(newCproxyConf);
            consumer.updateConfig(newConf);
        });
    }

    public LowLevelCarreraConsumer getLowLevelConsumer(String group, String cluster) {
        ConsumerGroupManager cg = consumerMap.get(group);
        return cg == null ? null : cg.getLowLevelCarreraConsumer(group, cluster);
    }

    public synchronized Map<String, ConsumerGroupConfig> getConsumerGroupConfigSnapshot() {
        return consumerMap.entrySet()
                .stream().collect(Collectors.toConcurrentMap(Map.Entry::getKey, x->x.getValue().getConfig()));
    }

    private ConsumerManager() {}

    private static class Singleton {
        private static ConsumerManager INSTANCE = new ConsumerManager();
    }

    public static ConsumerManager getInstance() {
        return Singleton.INSTANCE;
    }
}