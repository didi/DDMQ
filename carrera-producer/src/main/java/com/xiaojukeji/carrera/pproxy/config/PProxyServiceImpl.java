package com.xiaojukeji.carrera.pproxy.config;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.xiaojukeji.carrera.biz.ProxyService;
import com.xiaojukeji.carrera.biz.ZkService;
import com.xiaojukeji.carrera.biz.ZkServiceImpl;
import com.xiaojukeji.carrera.config.v4.PProxyConfig;
import com.xiaojukeji.carrera.config.v4.TopicConfig;
import com.xiaojukeji.carrera.dynamic.ParameterDynamicZookeeper;
import com.xiaojukeji.carrera.pproxy.utils.LogUtils;
import org.apache.commons.collections4.CollectionUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class PProxyServiceImpl implements ProxyService<TopicConfig, PProxyConfig> {

    private volatile Map<String, ParameterDynamicZookeeper.DataChangeCallback<TopicConfig>> currentIndexMap = new HashMap<>();

    private ZkService zkService;

    public PProxyServiceImpl(String zkHost, boolean isConfigCentre) throws Exception {
        this.zkService = new ZkServiceImpl(zkHost, isConfigCentre);
    }

    @Override
    public void getAndWatchIndex(Set<String> indexs,
                                 ParameterDynamicZookeeper.DataChangeCallback<TopicConfig> callback) throws Exception {
        Set<String> oldIndexs = Sets.newHashSet(currentIndexMap.keySet());

        Set<String> addIndexs = Sets.newHashSet(indexs);
        addIndexs.removeAll(oldIndexs);

        Set<String> deleteIndexs = Sets.newHashSet(oldIndexs);
        deleteIndexs.removeAll(indexs);

        for (String addIndex : addIndexs) {
            try {
                zkService.getAndWatchTopic(addIndex, callback);
                currentIndexMap.put(addIndex, callback);
            } catch (Exception ex){
                LogUtils.logError("PProxyServiceImpl.getAndWatchIndex", "get config failed", ex);
            }
        }

        for (String deleteIndex : deleteIndexs) {
            zkService.removeTopicWatch(deleteIndex, currentIndexMap.remove(deleteIndex));
        }
    }

    @Override
    public void getAndWatchProxy(String instance,
                                 ParameterDynamicZookeeper.DataChangeCallback<PProxyConfig> callback) throws Exception {
        zkService.getAndWatchPProxy(instance, callback);
    }

    @Override
    public void shutdown() {
        zkService.shutdown();
    }

    public PProxyConfig getPProxy(String instance) throws Exception {
        return zkService.getPProxy(instance);
    }

    public List<TopicConfig> getTopicConfig(Set<String> topics) {
        List<TopicConfig> topicConfigs = Lists.newArrayList();
        if (CollectionUtils.isEmpty(topics)) {
            return topicConfigs;
        }

        for (String topic : topics) {
            TopicConfig config = zkService.getTopic(topic);
            if (config != null) {
                topicConfigs.add(config);
            }
        }

        return topicConfigs;
    }
}