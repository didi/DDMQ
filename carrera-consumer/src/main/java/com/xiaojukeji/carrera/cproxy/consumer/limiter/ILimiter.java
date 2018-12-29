package com.xiaojukeji.carrera.cproxy.consumer.limiter;


import com.xiaojukeji.carrera.config.v4.GroupConfig;


public interface ILimiter {

    /**
     *
     * @param groupCluster
     * @param groupConfig
     */
    void initLimiter(String groupCluster, GroupConfig groupConfig);

    /**
     *
     * @param groupCluster
     * @param topic
     * @param permits
     */
    void doBlockLimit(String groupCluster, String topic, int permits) throws InterruptedException;

    /**
     *
     * @param groupCluster
     * @param topic
     * @param permits
     * @return
     */
    boolean doNonBlockLimit(String groupCluster, String topic, int permits);

    /**
     *
     * @param groupCluster
     * @param topic
     * @param permits
     * @return
     */
    void release(String groupCluster, String topic, int permits);
}