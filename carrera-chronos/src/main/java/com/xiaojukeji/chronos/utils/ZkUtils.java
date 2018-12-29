package com.xiaojukeji.chronos.utils;

import com.google.common.base.Charsets;
import com.netflix.config.ConfigurationManager;
import com.netflix.config.DynamicLongProperty;
import com.netflix.config.DynamicPropertyFactory;
import com.netflix.config.DynamicStringProperty;
import com.netflix.config.DynamicWatchedConfiguration;
import com.netflix.config.source.ZooKeeperConfigurationSource;
import com.xiaojukeji.chronos.config.ConfigManager;
import com.xiaojukeji.chronos.config.ZkConfig;
import com.xiaojukeji.chronos.ha.MasterElection;
import com.xiaojukeji.chronos.services.MetaService;
import org.apache.commons.lang3.StringUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.imps.CuratorFrameworkState;
import org.apache.curator.retry.BoundedExponentialBackoffRetry;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;


public class ZkUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(ZkUtils.class);

    private static final ZkConfig zkConfig = ConfigManager.getConfig().getZkConfig();
    private static volatile CuratorFramework curatorClient;

    public static final DynamicStringProperty offsetsProperty =
            DynamicPropertyFactory.getInstance().getStringProperty(zkConfig.getOffsetsProp(), null, new Runnable() {
                @Override
                public void run() {
                    if (MasterElection.isBackup()) {
                        final String offsetsJson = offsetsProperty.getValue();
                        LOGGER.info("succ download offsets from zk node, zkPath:{}, zkValue:{}", Constants.OFFSET_ZK_PATH, offsetsJson);
                        if (StringUtils.isNotBlank(offsetsJson)) {
                            JsonUtils.fromJsonString(offsetsJson, Map.class).forEach((k, v) -> {
                                MetaService.getZkQidOffsets().put((String) k, Long.parseLong(String.valueOf(v)));
                            });
                        }
                    }
                }
            });

    public static final DynamicLongProperty seekTimestampProperty =
            DynamicPropertyFactory.getInstance().getLongProperty(zkConfig.getSeekTimestampProp(), 0, new Runnable() {
                @Override
                public void run() {
                    if (MasterElection.isBackup()) {
                        final long seekTimestamp = seekTimestampProperty.getValue();
                        LOGGER.info("succ download seekTimestamp from zk node, zkPath:{}, zkValue:{}", Constants.SEEK_TIMESTAMP_ZK_PATH, seekTimestamp);
                        if (seekTimestamp != 0) {
                            MetaService.setZkSeekTimestamp(seekTimestamp);
                        }
                    }
                }
            });

    public static void init() {
        try {
            curatorClient = CuratorFrameworkFactory
                    .builder()
                    .connectString(zkConfig.getZkAddrs())
                    .sessionTimeoutMs(zkConfig.getZkSessionTimeoutMs())
                    .retryPolicy(new BoundedExponentialBackoffRetry(zkConfig.getBaseSleepTimeMs(), zkConfig.getMaxSleepMs(), zkConfig.getMaxRetries()))
                    .build();

            if (curatorClient.getState() == CuratorFrameworkState.LATENT) {
                curatorClient.start();
            }

            ZooKeeperConfigurationSource zkConfigSource = new ZooKeeperConfigurationSource(curatorClient, Constants.META_BASE_ZK_PATH);
            zkConfigSource.start();
            DynamicWatchedConfiguration zkDynamicConfig = new DynamicWatchedConfiguration(zkConfigSource);
            ConfigurationManager.install(zkDynamicConfig);
        } catch (Exception e) {
            LOGGER.error("ZkUtils getCuratorClient err:{}", e.getMessage(), e);
        }
    }

    public static CuratorFramework getCuratorClient() {
        return curatorClient;
    }

    public static void close() {
        if (curatorClient != null) {
            curatorClient.close();
        }
    }

    /**
     * 添加或更新zookeeper节点
     *
     * @param zkPath
     * @param zkValue
     */
    public static void createOrUpdateValue(final String zkPath, final String zkValue) {
        try {
            Stat stat = getCuratorClient().checkExists().forPath(zkPath);
            if (stat == null) {
                curatorClient.create().creatingParentsIfNeeded().forPath(zkPath, zkValue.getBytes(Charsets.UTF_8));
                LOGGER.info("succ create zk node, zkPath:{}, zkValue:{}", zkPath, zkValue);
            } else {
                curatorClient.setData().forPath(zkPath, zkValue.getBytes(Charsets.UTF_8));
                LOGGER.info("succ update zk node, zkPath:{}, zkValue:{}", zkPath, zkValue);
            }

        } catch (Exception e) {
            LOGGER.info("error while create or update zk node, zkPath:{}, zkValue:{}, err:{}", zkPath, zkValue, e.getMessage(), e);
        }
    }
}