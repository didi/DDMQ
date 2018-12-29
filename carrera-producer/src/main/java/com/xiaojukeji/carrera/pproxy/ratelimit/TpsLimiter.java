package com.xiaojukeji.carrera.pproxy.ratelimit;

import com.xiaojukeji.carrera.pproxy.config.TopicInMgmtConfig;
import com.xiaojukeji.carrera.pproxy.producer.TopicConfigManager;
import com.xiaojukeji.carrera.pproxy.utils.LogUtils;
import com.xiaojukeji.carrera.pproxy.utils.MetricUtils;
import com.google.common.util.concurrent.RateLimiter;
import com.xiaojukeji.carrera.config.v4.pproxy.TopicConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;


public class TpsLimiter implements IGroupRequestLimiter {

    private static final Logger LOGGER = LoggerFactory.getLogger(TpsLimiter.class);

    private volatile double warningRatio;
    private volatile int totalLimit;
    private Map<String, Double> groupLimits;

    private volatile RateLimiter totalCount;
    private volatile RateLimiter totalWarnCount;
    private Map<String, RateLimiter> groupCount;
    private Map<String, RateLimiter> groupWarnCount;
    private volatile TopicConfigManager topicConfigInfo;
    private boolean isStaticMode;

    public TpsLimiter(double warningRatio, int totalLimit, TopicConfigManager config) throws Exception {
        Map<String, Double> groupLimits = new ConcurrentHashMap<>(config.getTopicConfigs().size());

        for (Map.Entry<String, TopicInMgmtConfig> topicConfig : config.getTopicConfigs().entrySet()) {
            double maxTps = 0.0;
            for (TopicConfiguration topicConfiguration : topicConfig.getValue().getClusterConfig().values()) {
                maxTps += topicConfiguration.getMaxTps();
            }
            groupLimits.put(topicConfig.getKey(), maxTps);
        }

        init(warningRatio, totalLimit, groupLimits);
        topicConfigInfo = config;
        isStaticMode = true;
    }
    
    private void init(double warningRatio, int totalLimit, Map<String, Double> groupLimits) throws Exception {
        this.warningRatio = warningRatio;
        this.totalLimit = totalLimit;
        this.groupLimits = groupLimits;
        totalCount = RateLimiter.create(totalLimit * warningRatio);
        totalWarnCount = RateLimiter.create(totalLimit * (1 - warningRatio));
        groupCount = new ConcurrentHashMap<>(groupLimits.size());
        groupWarnCount = new ConcurrentHashMap<>(groupLimits.size());
        groupLimits.forEach((group, limit) -> {
            groupCount.put(group, RateLimiter.create(limit * warningRatio));
            groupWarnCount.put(group, RateLimiter.create(limit * (1 - warningRatio)));
        });
    }

    @Override
    public boolean tryEnter(String group) {
        if (!groupLimits.containsKey(group)) {
            LogUtils.logError("TpsLimiter::tryEnter", "can not find group in groupLimits, Group =" + group);
            return false;
        }
        RateLimiter rl = groupCount.get(group);
        if (rl == null) {
            LogUtils.logError("TpsLimiter::tryEnter", "can not find group in groupCount, Group =" + group);
            return false;
        }
        if (!rl.tryAcquire()) {
            LogUtils.logWarn("TpsLimiter::tryEnter",
                    String.format("Group %s TPS is over warning threshold(%f)!", group, groupLimits.get(group) * warningRatio));
            MetricUtils.incWarnLimitCounter(group);
            rl = groupWarnCount.get(group);
            if (rl == null) {
                LogUtils.logError("TpsLimiter::tryEnter", "can not find group in groupWarnCount, Group =" + group);
                return false;
            }
            if (!rl.tryAcquire()) {
                return false;
            }
        }

        if (!totalCount.tryAcquire()) {
            LogUtils.logWarn("TpsLimiter::tryEnter",
                    String.format("Total TPS is over warning threshold(%f)!", totalLimit * warningRatio));
            MetricUtils.incWarnLimitCounter(group);
            return totalWarnCount.tryAcquire();
        }
        return true;
    }

    @Override
    public void updateConfig(TopicConfigManager config) {
        doUpdateConfig(config, false);
        topicConfigInfo = config;
    }

    @Override
    public void shutdown() {
        // DO NOTHING.
    }

    @Override
    public void updateNodeConfig(double warningRatio, int totalLimit) {
        if (this.totalLimit != totalLimit) {
            totalCount = RateLimiter.create(totalLimit * warningRatio);
            totalWarnCount = RateLimiter.create(totalLimit * (1 - warningRatio));
            this.totalLimit = totalLimit;
        }

        if (this.warningRatio != warningRatio) {
            this.warningRatio = warningRatio;
            if (topicConfigInfo != null) {
                doUpdateConfig(topicConfigInfo, true);
            }
        }
        LOGGER.info("update rate limiter of node, totalCount:{}, warningRatio:{}", this.totalLimit, this.warningRatio);
    }

    public synchronized void doUpdateConfig(TopicConfigManager config, boolean isForced) {
        config.getTopicConfigs().forEach((group, topicConfig) -> {
            double newLimit = getLimitValue(group, topicConfig);

            if (groupLimits.containsKey(group) && groupLimits.get(group) == newLimit && !isForced) {
                return;
            }
            groupLimits.put(group, newLimit);
            RateLimiter rl = groupCount.get(group);
            if (rl == null) {
                rl = RateLimiter.create(newLimit * warningRatio);
            } else {
                rl.setRate(newLimit * warningRatio);
            }
            groupCount.put(group, rl);
            rl = groupWarnCount.get(group);
            if (rl == null) {
                rl = RateLimiter.create(newLimit * (1 - warningRatio));
            } else {
                rl.setRate(newLimit * (1 - warningRatio));
            }
            groupWarnCount.put(group, rl);
        });

        if (groupLimits.size() != config.getTopicConfigs().size()) {
            Set<String> removedGroups = new HashSet<>(groupLimits.keySet());
            removedGroups.removeAll(config.getTopicConfigs().keySet());
            for (String group : removedGroups) {
                groupLimits.remove(group);
                groupCount.remove(group);
                groupWarnCount.remove(group);
            }
        }

        LOGGER.debug("current rate limit:{}", groupLimits);
    }

    private double getLimitValue(String group, TopicInMgmtConfig topicConfig) {
        double newLimit = 0.0;
        if (topicConfig == null) {
            return newLimit;
        }

        if (isStaticMode) {
            for (TopicConfiguration config : topicConfig.getClusterConfig().values()) {
                newLimit += config.getMaxTps();
            }
        } else {
            for (TopicConfiguration config : topicConfig.getClusterConfig().values()) {
                newLimit += config.getTotalMaxTps();
            }
        }
        return newLimit;
    }

    @Override
    public String toString() {
        return "TpsLimiter{" +
                "warningRatio=" + warningRatio +
                ", totalLimit=" + totalLimit +
                ", groupLimits=" + groupLimits +
                ", totalCount=" + totalCount +
                ", totalWarnCount=" + totalWarnCount +
                ", groupCount=" + groupCount +
                ", groupWarnCount=" + groupWarnCount +
                '}';
    }
}