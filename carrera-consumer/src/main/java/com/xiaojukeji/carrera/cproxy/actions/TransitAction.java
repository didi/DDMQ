package com.xiaojukeji.carrera.cproxy.actions;

import com.alibaba.fastjson.JSONObject;
import com.xiaojukeji.carrera.cproxy.consumer.UpstreamJob;
import com.xiaojukeji.carrera.cproxy.utils.JsonUtils;
import com.xiaojukeji.carrera.cproxy.utils.LogUtils;
import com.xiaojukeji.carrera.cproxy.utils.MetricUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;
import java.util.Map.Entry;


public class TransitAction implements Action {

    @Override
    public Status act(UpstreamJob job, JSONObject jsonObject) {
        JSONObject ret;
        Map<String, String> transits = job.getUpstreamTopic().getTransit();
        String retPath = transits.get("$");
        if (retPath != null) {
            Object value = JsonUtils.getValueByPath(jsonObject, retPath);
            if (value instanceof JSONObject) {
                ret = (JSONObject) value;
            } else {
                LogUtils.logErrorInfo("Transit_error","the node is not a map; xpath:{}, type:{}, job:{}", retPath, value.getClass().getName(), job.info());
                MetricUtils.qpsAndFilterMetric(job, MetricUtils.ConsumeResult.INVALID);
                return Status.FAIL;
            }
        } else {
            ret = new JSONObject();
        }
        for (Entry<String, String> entry : transits.entrySet()) {
            if (StringUtils.equals(entry.getKey(), "$")) {
                continue;
            }
            Object value = JsonUtils.getValueByPath(jsonObject, entry.getValue());
            if (value != null) {
                JsonUtils.setValueByPath(ret, entry.getKey(), value);
            }
        }

        job.setData(ret);
        return Status.CONTINUE;
    }
}