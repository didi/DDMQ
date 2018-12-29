package com.xiaojukeji.chronos.utils;

import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class JsonUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(JsonUtils.class);

    public static String toJsonString(Object obj) {
        try {
            return JSONObject.toJSONString(obj);
        } catch (Exception e) {
            LOGGER.error("error while toJsonString, err:{}", e.getMessage(), e);
        }
        return null;
    }

    public static <T> T fromJsonString(byte[] jsonString, Class<T> clz) {
        try {
            return JSONObject.parseObject(jsonString, clz);
        } catch (Exception e) {
            LOGGER.error("error while fromJsonString, err:{}", e.getMessage(), e);
        }
        return null;
    }

    public static <T> T fromJsonString(String jsonString, Class<T> clz) {
        try {
            return JSONObject.parseObject(jsonString, clz);
        } catch (Exception e) {
            LOGGER.error("error while fromJsonString, err:{}", e.getMessage(), e);
        }
        return null;
    }
}