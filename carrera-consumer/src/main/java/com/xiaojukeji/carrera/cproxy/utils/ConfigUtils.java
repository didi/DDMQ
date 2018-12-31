package com.xiaojukeji.carrera.cproxy.utils;

import com.xiaojukeji.carrera.config.v4.CProxyConfig;
import com.xiaojukeji.carrera.config.v4.GroupConfig;
import com.xiaojukeji.carrera.config.v4.cproxy.UpstreamTopic;
import com.xiaojukeji.carrera.cproxy.consumer.ConfigManager;
import com.xiaojukeji.carrera.cproxy.actions.ActionBuilder;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;
import java.util.function.Function;


public class ConfigUtils {

    public static GroupConfig findGroupByName(String groupName, ConfigManager configManager) {
        groupName = StringUtils.trim(groupName);
        if (configManager == null) {
            return null;
        }
        Map<String, GroupConfig> groups = configManager.getCurGroupConfigMap();
        return groups == null ? null : groups.get(groupName);
    }

    public static boolean isKafkaMQCluster(CProxyConfig cProxyConfig, String brokerCluster) {
        if (cProxyConfig == null || StringUtils.isEmpty(brokerCluster)) {
            return false;
        }
        return cProxyConfig.getKafkaConfigs().containsKey(brokerCluster);
    }

    public static boolean isRmqMQCluster(CProxyConfig cProxyConfig, String brokerCluster) {
        if (cProxyConfig == null || StringUtils.isEmpty(brokerCluster)) {
            return false;
        }
        return cProxyConfig.getRocketmqConfigs().containsKey(brokerCluster);
    }

    public static <T> T getDefaultConfig(String configKey, T defaultValue, Function<String, T> converter) {
        String value = System.getProperty(configKey);
        if (value == null) {
            return defaultValue;
        }
        try {
            return converter.apply(value);
        } catch (Throwable t) {
            LogUtils.LOGGER.error("ConfigUtils.getDefaultConfig", "convert error, value=" + value, t);
        }
        return defaultValue;
    }

    public static boolean getDefaultConfig(String configKey, boolean defaultValue) {
        return getDefaultConfig(configKey, defaultValue, Boolean::valueOf);
    }

    public static boolean satisfyNewRmqConsumer(GroupConfig groupConfig) {
        for (UpstreamTopic topic : groupConfig.getTopics()) {
            for (String action : topic.getActions()) {
                if(action.equals(ActionBuilder.GROOVY) ||
                        action.equals(ActionBuilder.REDIS) ||
                        action.equals(ActionBuilder.ASYNC_HTTP)) {
                    return false;
                }
            }
        }
        return true;
    }

    public static String genGroupBrokerCluster(String group, String brokerCluster) {
        return group + '@' + brokerCluster;
    }

    public static boolean brokerClusterIsUpdated(CProxyConfig oldcProxyConf, CProxyConfig newcProxyConf) {
        if (oldcProxyConf == null) {
            return newcProxyConf != null;
        }
        if (newcProxyConf == null) {
            return false;
        }

        if (!oldcProxyConf.getKafkaConfigs().equals(newcProxyConf.getKafkaConfigs())) {
            return true;
        }

        return !oldcProxyConf.getRocketmqConfigs().equals(newcProxyConf.getRocketmqConfigs());

    }
}