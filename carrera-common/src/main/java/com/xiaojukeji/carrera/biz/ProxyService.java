package com.xiaojukeji.carrera.biz;

import com.xiaojukeji.carrera.dynamic.ParameterDynamicZookeeper;

import java.util.Set;


public interface ProxyService<I, P> {

    void getAndWatchIndex(Set<String> indexs, ParameterDynamicZookeeper.DataChangeCallback<I> callback) throws Exception;

    void getAndWatchProxy(String instance, ParameterDynamicZookeeper.DataChangeCallback<P> callback) throws Exception;

    void shutdown();
}