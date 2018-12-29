package com.xiaojukeji.carrera.cproxy.actions;

import com.alibaba.fastjson.JSONObject;
import com.xiaojukeji.carrera.cproxy.consumer.UpstreamJob;
import com.xiaojukeji.carrera.cproxy.utils.JsonUtils;
import com.xiaojukeji.carrera.cproxy.utils.LogUtils;
import com.xiaojukeji.carrera.cproxy.utils.MetricUtils;
import org.apache.commons.collections4.MapUtils;

import java.util.Map;


public class QueryParamsExtractAction implements Action {

    @Override
    public Status act(UpstreamJob job, JSONObject jsonObject) {
        Map<String, String> queryParamsConfig = job.getUpstreamTopic().getQueryParams();
        if (MapUtils.isEmpty(queryParamsConfig)) {
            LogUtils.logErrorInfo("QueryParamsExtract_error","Config error. topic.params is empty! job={}", job.info());
            MetricUtils.qpsAndFilterMetric(job, MetricUtils.ConsumeResult.INVALID);
            return Status.FAIL;
        }
        queryParamsConfig.forEach((key, path) -> {
            Object content = JsonUtils.getValueByPath(jsonObject, path);
            job.addQueryParam(key, content.toString());
        });
        return Status.CONTINUE;
    }
}