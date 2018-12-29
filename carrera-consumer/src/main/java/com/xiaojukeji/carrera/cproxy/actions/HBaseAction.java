package com.xiaojukeji.carrera.cproxy.actions;

import com.xiaojukeji.carrera.config.v4.cproxy.UpstreamTopic;
import com.xiaojukeji.carrera.cproxy.actions.hbase.HbaseCommand;
import com.xiaojukeji.carrera.cproxy.actions.hbase.HbaseConst;
import com.xiaojukeji.carrera.cproxy.config.ConsumerGroupConfig;
import com.xiaojukeji.carrera.cproxy.consumer.UpstreamJob;
import com.xiaojukeji.carrera.cproxy.utils.LogUtils;
import com.xiaojukeji.carrera.cproxy.utils.MetricUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.Append;
import org.apache.hadoop.hbase.client.Delete;
import org.apache.hadoop.hbase.client.HConnection;
import org.apache.hadoop.hbase.client.HConnectionManager;
import org.apache.hadoop.hbase.client.HTableInterface;
import org.apache.hadoop.hbase.client.Increment;
import org.apache.hadoop.hbase.client.Mutation;
import org.apache.hadoop.hbase.client.Put;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import static com.xiaojukeji.carrera.cproxy.actions.Action.Status.FAIL;
import static com.xiaojukeji.carrera.cproxy.actions.Action.Status.FINISH;


public class HBaseAction implements Action {

    private static final Logger LOGGER = LoggerFactory.getLogger(HBaseAction.class);

    private String group;

    private static class HBaseConnection {

        private HConnection connection;
        private final ConcurrentHashMap<Long/* threadID */, HashMap<String/* tableName */, HTableInterface>> tables = new ConcurrentHashMap<>();

        public HBaseConnection(HConnection connection) {
            this.connection = connection;
        }

        public HTableInterface getTable(String tableName) throws IOException {
            long threadId = Thread.currentThread().getId();
            tables.putIfAbsent(threadId, new HashMap<>());
            HashMap<String, HTableInterface> tableMap = tables.get(threadId);

            HTableInterface table = tableMap.get(tableName);
            if (table == null) {
                table = connection.getTable(tableName);
                table.setAutoFlushTo(true);
                tableMap.put(tableName, table);
            }

            return table;
        }

        public void close() {
            for (HashMap<String, HTableInterface> tableMap : tables.values()) {
                for (HTableInterface table : tableMap.values()) {
                    try {
                        table.close();
                    } catch (IOException e) {
                        LogUtils.logErrorInfo("HBASE_error", "failed to close hbase table", e);
                    }
                }
                tableMap.clear();
            }
            tables.clear();

            if (connection != null) {
                try {
                    connection.close();
                } catch (IOException e) {
                    LogUtils.logErrorInfo("HBASE_error", "fail to close hbase connection", e);
                }
                connection = null;
            }
        }
    }

    private ConcurrentHashMap<String, HBaseConnection> connectionMap = new ConcurrentHashMap<>();

    public HBaseAction(ConsumerGroupConfig consumerGroupConfig) {
        group = consumerGroupConfig.getGroup();

        for (String topic : consumerGroupConfig.getTopicMap().keySet()) {
            UpstreamTopic upstreamTopic = consumerGroupConfig.getTopicMap().get(topic);
            if (upstreamTopic.getHbaseconfiguration() != null) {
                // create hbase connection.
                Configuration configuration = HBaseConfiguration.create();
                configuration.set(HbaseConst.ZK_QUORUM, upstreamTopic.getHbaseconfiguration().getHbaseZK());
                configuration.set(HbaseConst.ZK_CLIENT_PORT, upstreamTopic.getHbaseconfiguration().getHbasePort());
                configuration.set(HbaseConst.CLIENT_BUFFER, upstreamTopic.getHbaseconfiguration().getHbaseBuffer());
                configuration.set(HbaseConst.USER_NAME, upstreamTopic.getHbaseconfiguration().getUser());
                configuration.set(HbaseConst.PASSWORD, upstreamTopic.getHbaseconfiguration().getPassword());

                try {
                    HBaseConnection HBaseConnection = new HBaseConnection(HConnectionManager.createConnection(configuration));
                    connectionMap.put(topic, HBaseConnection);
                } catch (IOException e) {
                    LogUtils.logErrorInfo("HBASE_error", "failed to create hbase connection, ", e);
                }
            }
        }
    }

    @Override
    public Status act(UpstreamJob job, byte[] bytes) {
        HBaseConnection connection = connectionMap.get(job.getTopic());
        if (connection == null) {
            LogUtils.logErrorInfo("HBASE_error", "no hbase connection for topic=" + job.getTopic());
            return FAIL;
        }

        if (CollectionUtils.isNotEmpty(job.getHbaseCommands())) {
            try {
                for (HbaseCommand hbaseCommand : job.getHbaseCommands()) {
                    HTableInterface table = connection.getTable(hbaseCommand.getTableName());
                    Mutation mutation = hbaseCommand.getMutation();

                    if (mutation instanceof Put) {
                        table.put((Put) mutation);
                    } else if (mutation instanceof Delete) {
                        table.delete((Delete) mutation);
                    } else if (mutation instanceof Append) {
                        table.append((Append) mutation);
                    } else if (mutation instanceof Increment) {
                        table.increment((Increment) mutation);
                    }
                }
                MetricUtils.qpsAndFilterMetric(job, MetricUtils.ConsumeResult.SUCCESS);
                return FINISH;
            } catch (IOException e) {
                LogUtils.logErrorInfo("HBASE_error", "job=" + job, e);
                return FAIL;
            }
        } else {
            LogUtils.logErrorInfo("HBASE_error", "no hbase command found, group:{}, topic:{}", group, job.getTopic());
            return FAIL;
        }
    }

    @Override
    public void shutdown() {
        connectionMap.values().stream().forEach(connection -> connection.close());
        connectionMap.clear();
    }
}
