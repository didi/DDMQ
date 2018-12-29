package com.xiaojukeji.carrera.cproxy.actions;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.xiaojukeji.carrera.cproxy.consumer.UpstreamJob;
import com.xiaojukeji.carrera.cproxy.utils.JsonUtils;
import com.xiaojukeji.carrera.cproxy.utils.LogUtils;
import com.xiaojukeji.carrera.cproxy.utils.MetricUtils;
import com.xiaojukeji.carrera.cproxy.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class JsonAction implements Action {
    private static final Logger LOGGER = LoggerFactory.getLogger(JsonAction.class);

    @Override
    public Status act(UpstreamJob job, byte[] bytes) {
        try {
            JSONObject jsonObject = JSON.parseObject(StringUtils.newString(bytes));
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("send content {}", JsonUtils.toJsonString(jsonObject));
            }
            if(jsonObject == null) {
                return Status.FINISH;
            }

            job.setData(jsonObject);
            return Status.CONTINUE;
        } catch (JSONException e) {
            MetricUtils.qpsAndFilterMetric(job, MetricUtils.ConsumeResult.INVALID);
            LogUtils.logErrorInfo("Json_error", String.format("parse data to JSON failed. job=%s", job.info()));
            LOGGER.debug(String.format("parse data to JSON failed. job=%s, error detail:", job.info()), e);
            return Status.FAIL;
        }
    }
}