package com.xiaojukeji.carrera.cproxy.consumer;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.xiaojukeji.carrera.config.v4.GroupConfig;
import com.xiaojukeji.carrera.config.v4.cproxy.UpstreamTopic;
import com.xiaojukeji.carrera.cproxy.consumer.exception.CarreraClientException;
import com.xiaojukeji.carrera.cproxy.config.ConsumerGroupConfig;
import com.xiaojukeji.carrera.cproxy.actions.ActionBuilder;
import com.xiaojukeji.carrera.cproxy.utils.LogUtils;
import com.xiaojukeji.carrera.cproxy.utils.MetricUtils;
import com.xiaojukeji.carrera.cproxy.utils.TimeUtils;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static org.slf4j.LoggerFactory.getLogger;


public class ConsumerGroupManager {

    public static final Logger LOGGER = getLogger(ConsumerGroupManager.class);

    private Map<String/*group#brokerCluster*/, CarreraConsumer> groupConsumerMap = Maps.newConcurrentMap();
    private ConsumerGroupConfig config;
    private List<ConsumerGroupConfig> subConfigs;
    private Map<String/*group@cluster*/, LowLevelCarreraConsumer> lowLevelConsumerMap = new ConcurrentHashMap<>();

    private volatile boolean isAllowDynamicStartup = false;
    private volatile long lastRequestTime = 0;

    private volatile ConsumerGroupState state = ConsumerGroupState.SHUTDOWN;

    private enum ConsumerGroupState {
        SHUTDOWN, RUNNING, DISABLED
    }

    protected static final int CONSUMER_KEEP_ALIVE_TIME = 60 * 1000;

    public ConsumerGroupManager(ConsumerGroupConfig config) {
        updateConfig(config);

        MetricUtils.addGroup(config.getGroup());
    }

    protected synchronized List<ConsumerGroupConfig> initConfig(ConsumerGroupConfig config) {
        List<ConsumerGroupConfig> ret = Lists.newArrayList();

        Map<String/*brokerClusters*/, List<UpstreamTopic>/*topics*/> classify = Maps.newHashMap();

        Map<String/*brokerCluster*/, Set<String>/*proxys*/> gatherProxy = Maps.newHashMap();

        for (UpstreamTopic topicConf : config.getGroupConfig().getTopics()) {
            Set<String> proxys = topicConf.getProxies().get(config.getcProxyConfig().getProxyCluster());
            if (proxys != null) {
                for (String proxy : proxys) {
                    gatherProxy.computeIfAbsent(topicConf.getBrokerCluster(), bc -> Sets.newHashSet()).add(proxy);
                }
            }
        }

        for (UpstreamTopic topicConf : config.getGroupConfig().getTopics()) {
            //过滤
            String brokerCluster = topicConf.getBrokerCluster();
            if (gatherProxy.get(brokerCluster).contains(config.getInstance())) {
                if (config.getcProxyConfig().getBrokerClusters().contains(topicConf.getBrokerCluster())) {
                    //分类
                    classify.computeIfAbsent(topicConf.getBrokerCluster(), k -> Lists.newArrayList()).add(topicConf);
                }
            }
        }
        for (Map.Entry<String/*brokerClusters*/, List<UpstreamTopic>> oneConsumerNeed : classify.entrySet()) {
            //构造一份子配置
            ConsumerGroupConfig oneConfig = new ConsumerGroupConfig();

            oneConfig.setInstance(config.getInstance());
            oneConfig.setGroup(config.getGroup());
            oneConfig.setBrokerCluster(oneConsumerNeed.getKey());
            //构造新的groupconfig
            GroupConfig newGroupConfig = config.getGroupConfig().clone();
            newGroupConfig.setTopics(oneConsumerNeed.getValue());

            oneConfig.setGroupConfig(newGroupConfig);
            oneConfig.setcProxyConfig(config.getcProxyConfig());
            oneConfig.setDelayRequestHandlerThreads(config.getDelayRequestHandlerThreads());
            oneConfig.createIndex();

            if (oneConfig.validate()) {
                ret.add(oneConfig);
            }
        }

        return ret;
    }

    private synchronized void start() {
        if (state == ConsumerGroupState.SHUTDOWN && isAllowDynamicStartup) {
            LogUtils.LOGGER.info("start:group:{}, subConfig cnt:{}.", getGroup(), subConfigs.size());
            subConfigs.forEach(subConfig -> tryStartOneConsumer(null, subConfig));
            lastRequestTime = TimeUtils.getCurTime();
            setState(ConsumerGroupState.RUNNING);
            LogUtils.logMainInfo("ConsumerGroup[{}] dynamic start SUCCESS [consumers={}, lowlevelConsumers={}, " +
                            "allow={}, state={}, lastTime={}]", config.getGroup(), groupConsumerMap.values(), lowLevelConsumerMap.keySet(),
                    isAllowDynamicStartup, state, lastRequestTime);
        }
    }

    public void logActionMetrics() {
        groupConsumerMap.values().forEach(CarreraConsumer::logActionMetric);
    }

    public synchronized void shutdown() {
        if (state == ConsumerGroupState.DISABLED) {
            return;
        }
        stopConsumers();
        groupConsumerMap.clear();
        lowLevelConsumerMap.clear();
        setState(ConsumerGroupState.DISABLED);
        MetricUtils.deleteGroup(config.getGroup());
        LogUtils.logMainInfo("ConsumerGroup[{}] shutdown SUCCESS!", config.getGroup());
    }

    private synchronized void stopConsumers() {
        if (state == ConsumerGroupState.RUNNING) {
            for (Iterator<CarreraConsumer> it = groupConsumerMap.values().iterator(); it.hasNext(); ) {
                CarreraConsumer consumer = it.next();
                shutdownConsumer(consumer);
                it.remove();
                if (consumer instanceof LowLevelCarreraConsumer) {
                    lowLevelConsumerMap.remove(consumer.getConfig().getGroupBrokerCluster());
                }
            }
            setState(ConsumerGroupState.SHUTDOWN);
            LogUtils.logMainInfo("ConsumerGroup[{}] stop Consumers SUCCESS Now:[consumers={}, lowlevelConsumers={}, allow={}, state={}, lastTime={}]",
                    getGroup(), groupConsumerMap.values(), lowLevelConsumerMap.keySet(), isAllowDynamicStartup, state, lastRequestTime);
        }
    }

    private void setState(ConsumerGroupState st) {
        state = st;
    }

    public ConsumerGroupState getState() {
        return state;
    }

    public synchronized void tryShutdown() {
        LogUtils.logMainInfo("ConsumerGroup[{}] before tryShutdown [consumers={}, lowlevelConsumers={}, allow={}, state={}, lastTime={}]",
                config.getGroup(), groupConsumerMap.values(), lowLevelConsumerMap.keySet(), isAllowDynamicStartup, state, lastRequestTime);
        if (state == ConsumerGroupState.DISABLED
                || state == ConsumerGroupState.SHUTDOWN
                || !isAllowDynamicStartup
                || TimeUtils.getElapseTime(lastRequestTime) < CONSUMER_KEEP_ALIVE_TIME) {
            return;
        }
        stopConsumers();
    }

    public void tryStart() {
        if (state == ConsumerGroupState.DISABLED || !isAllowDynamicStartup) {
            return;
        }
        if (state == ConsumerGroupState.RUNNING) {
            lastRequestTime = TimeUtils.getCurTime();
        } else {
            start();
        }
    }

    /**
     * 根据config更新consumer，同一个group下可能有多个consumer分别对应不同brokerCluster
     * @param config
     */
    public synchronized void updateConfig(ConsumerGroupConfig config) {
        if (config == null || (this.config != null && this.config.equals(config))) {
            return;
        }

        if (state == ConsumerGroupState.DISABLED) {
            return;
        }

        subConfigs = initConfig(config);
        LogUtils.logMainInfo("ConsumerGroup[{}].update, config cnt:{}.", config.getGroupBrokerCluster(), config.getGroupConfig().getTopics().size());
        this.config = config;
        setAllowDynamicStartup();
        LogUtils.logMainInfo("group:{}, config:{}.", getGroup(), subConfigs.size());

        Map<String/*group#brokerCluster*/, CarreraConsumer> oldGroupConsumerMap = new ConcurrentHashMap<>(groupConsumerMap);
        groupConsumerMap.clear();
        lowLevelConsumerMap.clear();

        if (state == ConsumerGroupState.RUNNING || !isAllowDynamicStartup) {
            for (ConsumerGroupConfig subConfig : subConfigs) {
                tryStartOneConsumer(oldGroupConsumerMap.remove(subConfig.getGroupBrokerCluster()), subConfig);
            }
            setState(ConsumerGroupState.RUNNING);
        }

        for (CarreraConsumer consumer : oldGroupConsumerMap.values()) {
            shutdownConsumer(consumer);
        }

        LogUtils.logMainInfo("ConsumerGroup[{}] update finished", config.getGroupBrokerCluster());
    }

    private void tryStartOneConsumer(CarreraConsumer consumer, ConsumerGroupConfig config) {
        try {
            if (consumer == null) {
                consumer = createConsumer(config);
            } else if (consumer.tryUpdate(config)) {
                LOGGER.info("update config for consumers success, new config={}", config);
            } else if (!config.bizEquals(consumer.getConfig())) {
                shutdownConsumer(consumer);
                consumer = createConsumer(config);
            }

            if (consumer instanceof LowLevelCarreraConsumer) {
                lowLevelConsumerMap.put(config.getGroupBrokerCluster(), (LowLevelCarreraConsumer) consumer);
            }
            groupConsumerMap.put(config.getGroupBrokerCluster(), consumer);
        } catch (CarreraClientException e) {
            LogUtils.logErrorInfo("start_consumer_failed", "start consumers failed!config=" + config, e);
        }
    }

    private CarreraConsumer createConsumer(ConsumerGroupConfig config) throws CarreraClientException {
        assert config != null;
        CarreraConsumer consumer;
        if (config.getGroupConfig().checkLowLevel()) {
            LowLevelCarreraConsumer c = new LowLevelCarreraConsumer(config);
            consumer = c;
        } else {
            consumer = new CarreraConsumer(config);
        }
        try {
            consumer.start();
        } catch (Exception ex) {
            LogUtils.logErrorInfo("consumer_start_failed", "consumers start failed, group:{}", getGroup(), ex);
            consumer.stop();
            throw ex;
        }
        return consumer;
    }

    private void shutdownConsumer(CarreraConsumer consumer) {
        LogUtils.logMainInfo("ConsumerGroupManager.shutdownConsumer, consumers={}", consumer);
        if (consumer != null) {
            consumer.stop();
        }
    }

    private void setAllowDynamicStartup() {
        if (CollectionUtils.isEmpty(config.getGroupConfig().getTopics())) {
            return;
        }
        this.isAllowDynamicStartup = true;
        for (UpstreamTopic topicConf : config.getGroupConfig().getTopics()) {
            if (topicConf.checkLowLevel()) {
                this.isAllowDynamicStartup = false;
                break;
            }

            if (!topicConf.getActions().contains(ActionBuilder.PULL_SERVER)) {
                this.isAllowDynamicStartup = false;
                return;
            }
        }
    }

    public Collection<CarreraConsumer> getConsumers() {
        return groupConsumerMap.values();
    }

    public LowLevelCarreraConsumer getLowLevelCarreraConsumer(String group, String cluster) {
        return lowLevelConsumerMap.get(group + '@' + cluster);
    }

    public ConsumerGroupConfig getConfig() {
        return config;
    }

    public String getGroup() {
        return config.getGroup();
    }

}