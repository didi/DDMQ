package com.xiaojukeji.carrera.cproxy.consumer;

import com.google.common.collect.Sets;
import com.xiaojukeji.carrera.biz.ProxyService;
import com.xiaojukeji.carrera.biz.ZkService;
import com.xiaojukeji.carrera.biz.ZkServiceImpl;
import com.xiaojukeji.carrera.config.v4.CProxyConfig;
import com.xiaojukeji.carrera.config.v4.GroupConfig;
import com.xiaojukeji.carrera.dynamic.ParameterDynamicZookeeper;
import com.xiaojukeji.carrera.cproxy.utils.LogUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;


public class CProxyServiceImpl implements ProxyService<GroupConfig, CProxyConfig> {

    private volatile Map<String, ParameterDynamicZookeeper.DataChangeCallback<GroupConfig>> currentIndexMap = new HashMap<>();

    private ZkService zkService;

    public CProxyServiceImpl(String zkHost, boolean isConfigCentre) throws Exception {
        this.zkService = new ZkServiceImpl(zkHost, isConfigCentre);
    }

    @Override
    public void getAndWatchIndex(Set<String> indexs, ParameterDynamicZookeeper.DataChangeCallback<GroupConfig> callback) throws Exception {

        Set<String> oldIndexs = Sets.newHashSet(currentIndexMap.keySet());

        Set<String> addIndexs = Sets.newHashSet(indexs);
        addIndexs.removeAll(oldIndexs);

        Set<String> deleteIndexs = Sets.newHashSet(oldIndexs);
        deleteIndexs.removeAll(indexs);

        for (String addIndex : addIndexs) {
            try {
                zkService.getAndWatchGroup(addIndex, callback);
                currentIndexMap.put(addIndex, callback);
            } catch (Throwable e) {
                LogUtils.logErrorInfo("zk_error", "zk watch node error! group:{}, err.msg:{}.", addIndex, e.getMessage(), e);
            }
        }

        for (String deleteIndex : deleteIndexs) {
            try {
                zkService.removeGroupWatch(deleteIndex, currentIndexMap.remove(deleteIndex));
                callback.handleDataDeleted("/" + deleteIndex);
            } catch (Throwable e) {
                LogUtils.logErrorInfo("zk_error", "zk remove watch node error! group:{}, err.msg:{}.", deleteIndex, e.getMessage(), e);
            }
        }
    }

    @Override
    public void getAndWatchProxy(String instance, ParameterDynamicZookeeper.DataChangeCallback<CProxyConfig> callback) throws Exception {
        zkService.getAndWatchCProxy(instance, callback);
    }

    @Override
    public void shutdown() {
        zkService.shutdown();
    }

}