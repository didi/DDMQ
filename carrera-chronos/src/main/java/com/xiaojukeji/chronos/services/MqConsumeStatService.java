package com.xiaojukeji.chronos.services;

import com.xiaojukeji.chronos.config.ConfigManager;
import com.xiaojukeji.chronos.config.PullConfig;
import com.xiaojukeji.chronos.ha.MasterElection;
import com.xiaojukeji.chronos.utils.JsonUtils;
import com.xiaojukeji.chronos.utils.ZkUtils;
import com.xiaojukeji.carrera.consumer.thrift.ConsumeStats;
import com.xiaojukeji.carrera.consumer.thrift.client.CarreraConfig;
import com.xiaojukeji.carrera.consumer.thrift.client.CarreraConsumer;
import com.xiaojukeji.chronos.utils.Constants;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;


public class MqConsumeStatService {
    private static final Logger LOGGER = LoggerFactory.getLogger(MqConsumeStatService.class);

    private static volatile MqConsumeStatService instance = null;

    private final PullConfig pullConfig = ConfigManager.getConfig().getPullConfig();
    private CarreraConsumer consumers = null;

    private MqConsumeStatService() {
        CarreraConfig carreraConfig = new CarreraConfig();
        carreraConfig.setServers(pullConfig.getCproxyAddrs());
        carreraConfig.setGroupId(pullConfig.getInnerGroup());
        carreraConfig.setRetryInterval(pullConfig.getRetryIntervalMs());
        carreraConfig.setTimeout(pullConfig.getTimeoutMs());
        carreraConfig.setMaxBatchSize(pullConfig.getMaxBatchSize());
        consumers = new CarreraConsumer(carreraConfig);
        LOGGER.info("init carrera consumer for consumer stats, carreraConfig:{}", carreraConfig);
    }

    public void uploadOffsetsToZk() {
        try {
            List<ConsumeStats> consumeStatsList = consumers.getConsumeStats(pullConfig.getInnerTopic());
            if (consumeStatsList != null) {
                for (ConsumeStats consumeStats : consumeStatsList) {
                    if (pullConfig.getInnerGroup().equals(consumeStats.getGroup())) {
                        final Map<String, Long> map = consumeStats.getConsumeOffsets();
                        LOGGER.info("pull consumer stats for upload offsets to zk node, group:{}", consumeStats.getGroup());
                        ZkUtils.createOrUpdateValue(Constants.OFFSET_ZK_PATH, JsonUtils.toJsonString(map));
                    }
                }
            }
        } catch (TException e) {
            LOGGER.error("error while consumer stats, err:{}", e.getMessage(), e);
        }
    }

    public void stop() {
        final long start = System.currentTimeMillis();

        /* if master, upload offsets before stop */
        if (MasterElection.isMaster()) {
            uploadOffsetsToZk();
        }

        consumers.stop();
        LOGGER.info("MqConsumeStatService carrera consumer has stopped, cost:{}ms", System.currentTimeMillis() - start);
    }

    public static MqConsumeStatService getInstance() {
        if (instance == null) {
            synchronized (MqConsumeStatService.class) {
                if (instance == null) {
                    instance = new MqConsumeStatService();
                }
            }
        }
        return instance;
    }
}