package com.xiaojukeji.carrera.cproxy.actions;

import com.alibaba.fastjson.JSONObject;
import com.xiaojukeji.carrera.cproxy.config.ConsumerGroupConfig;
import com.xiaojukeji.carrera.cproxy.consumer.UpstreamJob;
import com.xiaojukeji.carrera.cproxy.actions.util.UpstreamJobBlockingQueue;
import com.xiaojukeji.carrera.cproxy.actions.util.UpstreamJobExecutorPool;
import com.xiaojukeji.carrera.cproxy.utils.JsonUtils;
import com.xiaojukeji.carrera.cproxy.utils.LogUtils;
import com.xiaojukeji.carrera.cproxy.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.xiaojukeji.carrera.config.v4.cproxy.UpstreamTopic.ORDER_BY_QID;
import static com.xiaojukeji.carrera.config.v4.cproxy.UpstreamTopic.ORDER_BY_KEY;


abstract class OrderAction implements Action {
    protected static final Logger LOGGER = LoggerFactory.getLogger(OrderAction.class);
    protected static final Logger METRIC_LOGGER = LogUtils.METRIC_LOGGER;

    protected UpstreamJobExecutorPool executor;
    protected String group;

    public OrderAction(ConsumerGroupConfig config) {
        group = config.getGroup();
        executor = new UpstreamJobExecutorPool(config);
    }

    @Override
    public Action.Status act(UpstreamJob job) {
        String orderKey = job.getUpstreamTopic().getOrderKey();

        if (StringUtils.isNotBlank(orderKey)) {
            Object orderValue = null;
            if (ORDER_BY_QID.equals(orderKey)) {
                orderValue = job.getTopic() + job.getQid();
            } else if (ORDER_BY_KEY.equals(orderKey)) {
                orderValue = job.getMsgKey();
            } else if (job.getData() instanceof JSONObject) {
                try {
                    orderValue = JsonUtils.getValueByPath((JSONObject) job.getData(), orderKey);
                } catch (Exception e) {
                    LogUtils.logErrorInfo("Order_error",String.format("Get orderKey Exception! orderKey=%s, job=%s", orderKey, job.info()), e);
                }
            }
            return async(job, orderValue);
        } else {
            return async(job, null);
        }
    }

    private Action.Status async(UpstreamJob job, Object orderValue) {
        if (orderValue != null) {
            job.setOrderId(orderValue.hashCode());
        }
        try {
            executor.submit(job);
            return Status.ASYNCHRONIZED;
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
            return Status.FAIL;
        }
    }

    @Override
    public void logMetrics() {
        UpstreamJobBlockingQueue queue = executor.getQueue();
        METRIC_LOGGER.info("[ORDER_METRIC]groupId={}, activeWorkers={}, queueSize={}, readyJobs={}, workingJobs={}",
                group, executor.getActiveThreadNumber(),
                queue.getSize(), queue.getReadyJobNumber(), queue.getWorkingJobNumber());
    }

    @Override
    public void shutdown() {
        LOGGER.info("shutdown executor for {}", group);
        executor.shutdown();
    }
}