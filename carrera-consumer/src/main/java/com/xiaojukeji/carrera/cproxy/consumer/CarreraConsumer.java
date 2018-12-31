package com.xiaojukeji.carrera.cproxy.consumer;

import com.google.common.collect.Sets;
import com.xiaojukeji.carrera.config.v4.cproxy.KafkaConfiguration;
import com.xiaojukeji.carrera.config.v4.cproxy.RocketmqConfiguration;
import com.xiaojukeji.carrera.config.v4.cproxy.UpstreamTopic;
import com.xiaojukeji.carrera.cproxy.consumer.exception.CarreraClientException;
import com.xiaojukeji.carrera.cproxy.consumer.handler.AsyncMessageHandler;
import com.xiaojukeji.carrera.cproxy.consumer.offset.CarreraOffsetManager;
import com.xiaojukeji.carrera.cproxy.config.ConsumerGroupConfig;
import com.xiaojukeji.carrera.cproxy.actions.Action;
import com.xiaojukeji.carrera.cproxy.actions.ActionBuilder;
import com.xiaojukeji.carrera.cproxy.utils.ConfigUtils;
import com.xiaojukeji.carrera.cproxy.utils.LogUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;


public class CarreraConsumer implements AsyncMessageHandler {
    public static final Logger LOGGER = LoggerFactory.getLogger(CarreraConsumer.class);

    private ConsumerGroupConfig config;

    private BaseCarreraConsumer consumer;
    private Map<String, Action> actionMap;
    private Set<UpstreamJob> workingJobs;

    private Map<String, UpstreamTopic> upstreamTopicMap = new ConcurrentHashMap<>();

    public CarreraConsumer(ConsumerGroupConfig config) {
        this.config = config;
        for (UpstreamTopic upstreamTopic : config.getGroupConfig().getTopics()) {
            upstreamTopicMap.put(upstreamTopic.getTopic(), upstreamTopic);
        }
    }

    public void start() throws CarreraClientException {
        LogUtils.logMainInfo("CarreraConsumer.start, group:{}, brokerCluster:{}.",
                config.getGroup(), config.getBrokerCluster());
        workingJobs = Sets.newConcurrentHashSet();
        buildActionMap();

        //rocketmq
        if (config.getcProxyConfig().getRocketmqConfigs().containsKey(config.getBrokerCluster())) {
            RocketmqConfiguration rocketmqConfiguration = config.getcProxyConfig()
                    .getRocketmqConfigs().get(config.getBrokerCluster());

            if (ConfigUtils.satisfyNewRmqConsumer(config.getGroupConfig())) {
                LOGGER.debug("open a CarreraNewRocketMqConsumer client. group:{}.", config.getGroup());
                consumer = new CarreraNewRocketMqConsumer(config.getBrokerCluster(),
                        config.getGroup(), config.getGroupConfig(), config.getcProxyConfig(),
                        rocketmqConfiguration, this, config.getMaxConsumeLagMap(), config.getTotalThreads());
            } else {
                consumer = new CarreraRocketMqConsumer(config.getBrokerCluster(),
                        config.getGroup(), config.getGroupConfig(), config.getcProxyConfig(),
                        rocketmqConfiguration, this, config.getMaxConsumeLagMap(), config.getTotalThreads());
            }
        }

        //kafka
        if (config.getcProxyConfig().getKafkaConfigs().containsKey(config.getBrokerCluster())) {
            KafkaConfiguration kafkaConfiguration = config.getcProxyConfig().getKafkaConfigs().get(config.getBrokerCluster());
            consumer = new CarreraKafkaConsumer(config.getBrokerCluster(),
                    config.getGroup(), config.getGroupConfig(), config.getcProxyConfig(),
                    kafkaConfiguration, this, config.getMaxConsumeLagMap(), config.getTopicCount(), config.getTopicMap());
        }

        consumer.enableOffsetAutoCommit(CarreraOffsetManager.getInstance().getScheduler());
        consumer.startConsume();
    }

    public void buildActionMap() {
        actionMap = new HashMap<>();
        config.getGroupConfig().getTopics().stream().map(UpstreamTopic::getActions).forEach(actionList -> actionList.forEach(action -> actionMap.computeIfAbsent(action,
                actionName -> ActionBuilder.newAction(config, actionName))
        ));
    }

    public void stop() {
        LogUtils.logMainInfo("CarreraConsumer.stop;group:{}, brokerCluster:{}", config.getGroup(), config.getBrokerCluster());
        if (consumer != null) {
            consumer.shutdown();
        }
        LOGGER.info("terminateJobs for group:{}, cnt:{}", config.getGroup(), workingJobs.size());
        workingJobs.forEach(UpstreamJob::markTerminate);

        if (actionMap != null) {
            actionMap.values().forEach(Action::shutdown);
        }
    }

    public Map<String, Action> getActionMap() {
        return actionMap;
    }

    @Override
    public void process(CommonMessage message, ConsumeContext context, ResultCallBack resultCallBack) {
        if (upstreamTopicMap.containsKey(message.getTopic())) {
            UpstreamJob job = new UpstreamJob(this, upstreamTopicMap.get(message.getTopic()), message, context, resultCallBack);
            workingJobs.add(job);
            job.registerJobFinishedCallback(this::onJobFinish);
            job.execute();
        } else {
            resultCallBack.setResult(true);
        }
    }

    public void onJobFinish(UpstreamJob job, boolean success) {
        if (!workingJobs.remove(job)) {
            LOGGER.warn("job is not in working jobs, job={}", job);
        }
    }

    /**
     * 尝试热更新配置,后续优化，减少 consumer 重启。
     *
     * @param config
     * @return 是否成功
     */
    public boolean tryUpdate(ConsumerGroupConfig config) {
        return false;
    }

    public void logActionMetric() {
        actionMap.values().forEach(Action::logMetrics);
    }

    public BaseCarreraConsumer getConsumer() {
        return consumer;
    }

    @Override
    public String toString() {
        return "CarreraConsumer@" + config.getBrokerCluster() + "@" + config.getGroup();
    }

    public String getBrokerCluster() {
        return this.config.getBrokerCluster();
    }

    public ConsumerGroupConfig getConfig() {
        return config;
    }
}