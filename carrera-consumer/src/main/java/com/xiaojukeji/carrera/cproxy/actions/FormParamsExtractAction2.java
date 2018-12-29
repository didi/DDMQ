package com.xiaojukeji.carrera.cproxy.actions;

import com.alibaba.fastjson.JSONObject;
import com.xiaojukeji.carrera.cproxy.consumer.UpstreamJob;
import com.xiaojukeji.carrera.cproxy.utils.JsonUtils;
import com.xiaojukeji.carrera.cproxy.utils.StringUtils;

import java.util.Map;


public class FormParamsExtractAction2 implements Action {

    @Override
    public Status act(UpstreamJob job, byte[] bytes) {
        job.addFormParam(FormParamsExtractAction.FORM_PARAMS_KEY, StringUtils.newString(bytes));
        return Status.CONTINUE;
    }

    @Override
    public Status act(UpstreamJob job, JSONObject jsonObject) {
        for (Map.Entry<String, Object> entry : jsonObject.entrySet()) {
            if (entry.getValue() instanceof String) {
                job.addFormParam(entry.getKey(), (String) entry.getValue());
            } else if (entry.getValue() instanceof Number) {
                job.addFormParam(entry.getKey(), entry.getValue().toString());
            } else {
                job.addFormParam(entry.getKey(), JsonUtils.toJsonString(entry.getValue()));
            }
        }
        FormParamsExtractAction.appendContextInFormParams(job);
        return Status.CONTINUE;
    }
}