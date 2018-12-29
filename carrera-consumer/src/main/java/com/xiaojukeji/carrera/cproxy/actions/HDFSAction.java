package com.xiaojukeji.carrera.cproxy.actions;

import com.xiaojukeji.carrera.config.v4.cproxy.UpstreamTopic;
import com.xiaojukeji.carrera.cproxy.actions.hdfs.HDFSWriter;
import com.xiaojukeji.carrera.cproxy.config.ConsumerGroupConfig;
import com.xiaojukeji.carrera.cproxy.consumer.UpstreamJob;
import com.xiaojukeji.carrera.cproxy.utils.LogUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class HDFSAction implements Action {

    public static final Logger LOGGER = LoggerFactory.getLogger(HDFSAction.class);

    private static final Logger METRIC_LOGGER = LogUtils.METRIC_LOGGER;

    private String group;

    private Map<String/* topic */, HDFSWriter> writerMap = new ConcurrentHashMap<>();

    public HDFSAction(ConsumerGroupConfig consumerGroupConfig) {
        for (UpstreamTopic upstreamTopic : consumerGroupConfig.getTopicMap().values()) {
            HDFSWriter writer = new HDFSWriter(consumerGroupConfig.getGroup(), upstreamTopic.getTopic(), upstreamTopic.getHdfsConfiguration());
            writerMap.put(upstreamTopic.getTopic(), writer);
        }
        group = consumerGroupConfig.getGroup();
    }

    @Override
    public Status act(UpstreamJob job, byte[] bytes) {
        try {
            String topic = job.getTopic();
            HDFSWriter hdfsWriter = writerMap.get(topic);
            hdfsWriter.doWrite(job, bytes);
            return Status.ASYNCHRONIZED;
        } catch (Exception e) {
            LogUtils.logErrorInfo("HDFS_error", "write hdfs error, job=" + job);
            return Status.FAIL;
        }
    }

    @Override
    public void logMetrics() {
        for (HDFSWriter writer : writerMap.values()) {
            if (writer != null) {
                METRIC_LOGGER.info("[HDFS_METRIC]groupId={}, topic={}, dirSize={}, fileSize={}", group, writer.getTopic(),
                        writer.getDataFileManager().getDirSize(), writer.getDataFileManager().getFileSize());
            }
        }
    }

    @Override
    public void shutdown() {
        writerMap.values().stream().forEach(hdfsWriter -> hdfsWriter.stop());
    }
}
