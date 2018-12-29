package com.xiaojukeji.carrera.cproxy.actions;

import com.alibaba.fastjson.JSONObject;
import com.xiaojukeji.carrera.config.AppendContext;
import com.xiaojukeji.carrera.cproxy.consumer.UpstreamJob;
import com.xiaojukeji.carrera.cproxy.utils.JsonUtils;
import com.xiaojukeji.carrera.cproxy.utils.StringUtils;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.collections4.CollectionUtils;


public class FormParamsExtractAction implements Action {

    private static final String CARRERA_TOPIC = "carrera_topic";
    private static final String CARRERA_GROUP = "carrera_group";
    private static final String CARRERA_OFFSET = "carrera_offset";
    private static final String CARRERA_SOURCE = "carrera_source";
    private static final String CARRERA_QID = "carrera_qid";
    private static final String CARRERA_TOKEN = "carrera_token";

    public static final String FORM_PARAMS_KEY = "params";
    public static final String CARRERA_MSG_KEY = "carrera_msg_key";
    public static final String CARRERA_PROPERTIES = "carrera_properties";
    public static final String CARRERA_REQ_CNT = "carrera_req_cnt";

    @Override
    public Status act(UpstreamJob job, byte[] bytes) {
        return setFormParams(job, StringUtils.newString(bytes));
    }

    @Override
    public Status act(UpstreamJob job, JSONObject jsonObject) {
        return setFormParams(job, JsonUtils.toJsonString(jsonObject));
    }

    private Status setFormParams(UpstreamJob job, String content) {
        job.addFormParam(FORM_PARAMS_KEY, content);
        appendContextInFormParams(job);
        return Status.CONTINUE;
    }

    public static void appendContextInFormParams(UpstreamJob job) {
        if (CollectionUtils.isNotEmpty(job.getUpstreamTopic().getAppendContext())) {
            boolean msgKeyAdded = false;
            for (AppendContext context : job.getUpstreamTopic().getAppendContext()) {
                switch (context) {
                    case TOPIC:
                        job.addFormParam(CARRERA_TOPIC, job.getTopic());
                        break;
                    case GROUP:
                        job.addFormParam(CARRERA_GROUP, job.getGroupId());
                        break;
                    case OFFSET:
                        job.addFormParam(CARRERA_OFFSET,  Long.toString(job.getContext().getOffset()));
                        break;
                    case SOURCE:
                        job.addFormParam(CARRERA_SOURCE,  job.getContext().getSource().name());
                        break;
                    case QID:
                        job.addFormParam(CARRERA_QID,  job.getQid());
                        break;
                    case TOKEN:
                        job.addFormParam(CARRERA_TOKEN,  DigestUtils.md5Hex(job.getMsgKey() + job.getTokenKey()));
                    case MSG_KEY:
                        if (!msgKeyAdded) {
                            job.addFormParam(CARRERA_MSG_KEY,  job.getMsgKey());
                            msgKeyAdded = true;
                        }
                        break;
                    case PROPERTIES:
                        job.addFormParam(CARRERA_PROPERTIES, JsonUtils.toJsonString(job.getContext().getProperties()));
                        break;
                }
            }
        }
    }
}