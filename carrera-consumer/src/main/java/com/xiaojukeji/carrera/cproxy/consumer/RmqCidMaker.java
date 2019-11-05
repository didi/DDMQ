package com.xiaojukeji.carrera.cproxy.consumer;

import com.xiaojukeji.carrera.cproxy.utils.MixAll;

/**
 * Author: zanglei@didiglobal.com
 * Date: 2019-11-05
 * Time: 17:49
 */
public class RmqCidMaker {

    /**
     *
     * @param bucketNum
     * @param group
     * @param brokerCluster
     * @param proxyInstance
     * @param extraInfo
     * @return
     */
    public static String makeCid(int bucketNum, String group, String brokerCluster, String proxyCluster, String proxyInstance, String extraInfo) {
        String extra = extraInfo == null ? "" : extraInfo;

        int hashId = (group + brokerCluster + proxyInstance + extra).hashCode();

        int bucket = (hashId % bucketNum + bucketNum) % bucketNum;

        return bucket + MixAll.INSTANCE_SPLITER + proxyInstance + MixAll.INSTANCE_SPLITER + proxyCluster + MixAll.INSTANCE_SPLITER + brokerCluster + ("".equals(extra) ? "" : MixAll.INSTANCE_SPLITER + extra);
    }

}
