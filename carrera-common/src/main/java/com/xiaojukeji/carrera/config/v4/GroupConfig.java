package com.xiaojukeji.carrera.config.v4;


import com.google.common.collect.Lists;
import com.xiaojukeji.carrera.config.ConfigurationValidator;
import com.xiaojukeji.carrera.config.v4.cproxy.RedisConfiguration;
import com.xiaojukeji.carrera.config.v4.cproxy.UpstreamTopic;
import com.xiaojukeji.carrera.utils.PropertyUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;


public class GroupConfig implements ConfigurationValidator, Cloneable {
    private static final String VALID_PATTERN_STR = "^[%|a-zA-Z0-9_-]+$";
    private static final Pattern PATTERN = Pattern.compile(VALID_PATTERN_STR);
    private static final int CHARACTER_MAX_LENGTH = 255;

    private String group;
    private List<UpstreamTopic> topics;
    private int asyncThreads = 8;

    private RedisConfiguration redisConfig;

    private List<String> alarmGroup;
    private boolean enableAlarm = true;
    private long delayTimeThreshold = 300000L;
    private long committedLagThreshold = 10000L;

    private int delayRequestHandlerThreads = -1;

    public GroupConfig() {
    }

    public GroupConfig(String group, List<UpstreamTopic> topic) {
        this.group = group;
        this.topics = topic;
    }

    public RedisConfiguration getRedisConfig() {
        return redisConfig;
    }

    public void setRedisConfig(RedisConfiguration redisConfig) {
        this.redisConfig = redisConfig;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public List<UpstreamTopic> getTopics() {
        return topics;
    }

    public void setTopics(List<UpstreamTopic> topics) {
        this.topics = topics;
    }

    public int getAsyncThreads() {
        return asyncThreads;
    }

    public void setAsyncThreads(int asyncThreads) {
        this.asyncThreads = asyncThreads;
    }

    public boolean isEnableAlarm() {
        return enableAlarm;
    }

    public void setEnableAlarm(boolean enableAlarm) {
        this.enableAlarm = enableAlarm;
    }

    public long getDelayTimeThreshold() {
        return delayTimeThreshold;
    }

    public void setDelayTimeThreshold(long delayTimeThreshold) {
        this.delayTimeThreshold = delayTimeThreshold;
    }

    public long getCommittedLagThreshold() {
        return committedLagThreshold;
    }

    public void setCommittedLagThreshold(long committedLagThreshold) {
        this.committedLagThreshold = committedLagThreshold;
    }

    public List<String> getAlarmGroup() {
        return alarmGroup;
    }

    public void setAlarmGroup(List<String> alarmGroup) {
        this.alarmGroup = alarmGroup;
    }

    public int getDelayRequestHandlerThreads() {
        return delayRequestHandlerThreads;
    }

    public void setDelayRequestHandlerThreads(int delayRequestHandlerThreads) {
        this.delayRequestHandlerThreads = delayRequestHandlerThreads;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GroupConfig that = (GroupConfig) o;
        return asyncThreads == that.asyncThreads &&
                enableAlarm == that.enableAlarm &&
                delayTimeThreshold == that.delayTimeThreshold &&
                committedLagThreshold == that.committedLagThreshold &&
                delayRequestHandlerThreads == that.delayRequestHandlerThreads &&
                Objects.equals(group, that.group) &&
                Objects.equals(topics, that.topics) &&
                Objects.equals(redisConfig, that.redisConfig) &&
                Objects.equals(alarmGroup, that.alarmGroup);
    }

    public boolean bizEquals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GroupConfig that = (GroupConfig) o;
        boolean ret = asyncThreads == that.asyncThreads &&
                delayRequestHandlerThreads == that.delayRequestHandlerThreads &&
                Objects.equals(group, that.group) &&
                Objects.equals(redisConfig, that.redisConfig);
        if (!ret)
            return false;
        if (that.topics.size() != topics.size())
            return false;
        for (UpstreamTopic thatTopic : that.topics) {
            boolean flag = false;
            for (UpstreamTopic oldtopic : topics) {
                if (thatTopic.bizEquals(oldtopic)) {
                    flag = true;
                }
            }
            if (!flag)
                return false;
        }

        return true;
    }

    @Override
    public int hashCode() {

        return Objects.hash(group, topics, asyncThreads, redisConfig, alarmGroup, enableAlarm, delayTimeThreshold, committedLagThreshold, delayRequestHandlerThreads);
    }

    @Override
    public String toString() {
        return "GroupConfig{" +
                "group='" + group + '\'' +
                ", topics=" + topics +
                ", asyncThreads=" + asyncThreads +
                ", redisConfig=" + redisConfig +
                ", alarmGroup=" + alarmGroup +
                ", enableAlarm=" + enableAlarm +
                ", delayTimeThreshold=" + delayTimeThreshold +
                ", committedLagThreshold=" + committedLagThreshold +
                ", delayRequestHandlerThreads=" + delayRequestHandlerThreads +
                '}';
    }

    @Override
    public boolean validate() throws ConfigurationValidator.ConfigException {
        try {
            if (!checkGroup()) {
                throw new ConfigException("[GroupConfig] group error");
            } else if (this.asyncThreads <= 0) {
                throw new ConfigException("[GroupConfig] asyncThreads <= 0");
            }

            if (!CollectionUtils.isEmpty(this.topics)) {
                int lowLevelCnt = 0;
                int enabledTopicCnt = 0;
                for (UpstreamTopic upstreamTopic : this.topics) {
                    if(!upstreamTopic.isEnabled()) {
                        continue;
                    }
                    enabledTopicCnt++;
                    if (!upstreamTopic.validate()) {
                        return false;
                    }
                    if (upstreamTopic.checkLowLevel()) {
                        lowLevelCnt++;
                    }
                }
                if (lowLevelCnt != 0 && lowLevelCnt != enabledTopicCnt) {
                    throw new ConfigException("[GroupConfig] One group can not mix low-level and non-low-level topics.");
                }
            }
            if (redisConfig != null && !redisConfig.validate()) {
                throw new ConfigException("[GroupConfig] redisConfig error, redisConfig=" + this.redisConfig);
            }
            if (delayTimeThreshold < 0 || committedLagThreshold < 0) {
                throw new ConfigException("[GroupConfig] delayTimeThreshold or committedLagThreshold error, delayTimeThreshold=" + this.delayTimeThreshold + ", committedLagThreshold=" + committedLagThreshold);
            }
        } catch (ConfigException e) {
            throw new ConfigException(e.getMessage() + ", group=" + this.group);
        }

        return true;
    }

    @Override
    public GroupConfig clone() {
        GroupConfig group = new GroupConfig();
        PropertyUtils.copyNonNullProperties(group, this);
        if (this.topics != null) {
            group.topics = new ArrayList<>();
            for (UpstreamTopic topic : this.topics) {
                group.topics.add(topic.clone());
            }
        }
        if (redisConfig != null) {
            group.redisConfig = redisConfig.clone();
        }

        if (alarmGroup != null) {
            group.alarmGroup = Lists.newArrayList(alarmGroup);
        }
        return group;
    }

    private boolean checkGroup() {
        return StringUtils.isNotEmpty(this.group) &&
                StringUtils.length(this.group) <= CHARACTER_MAX_LENGTH &&
                PATTERN.matcher(this.group).matches();
    }

    public boolean checkLowLevel() {
        for (UpstreamTopic topic : topics) {
            if (topic.checkLowLevel()) {
                return true;
            }
        }
        return false;
    }

}