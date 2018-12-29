package com.xiaojukeji.chronos.utils;

import com.google.common.base.Joiner;
import com.xiaojukeji.chronos.config.ChronosConfig;
import com.xiaojukeji.chronos.config.ConfigManager;
import com.xiaojukeji.chronos.config.ZkConfig;


public class Constants {
    private static final ChronosConfig chronosConfig = ConfigManager.getConfig();
    private static final ZkConfig zkConfig = chronosConfig.getZkConfig();

    public static final String META_BASE_ZK_PATH = Joiner.on("/").join(zkConfig.getMetaPathPrefix(), chronosConfig.getClusterName(), chronosConfig.getGroupName());
    public static final String SEEK_TIMESTAMP_ZK_PATH = Joiner.on("/").join(META_BASE_ZK_PATH, zkConfig.getSeekTimestampProp());
    public static final String OFFSET_ZK_PATH = Joiner.on("/").join(META_BASE_ZK_PATH, zkConfig.getOffsetsProp());

    public static final String MASTER_PATH = Joiner.on("/").join(zkConfig.getMasterPathPrefix(), chronosConfig.getClusterName(), chronosConfig.getGroupName());

    // 为了迎合rocksdb的默认排序
    public static final int SEGMENT_INDEX_BASE = 10000;
}