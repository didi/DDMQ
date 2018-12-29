package com.xiaojukeji.carrera.cproxy.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONPath;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class JsonUtils {
    private static Logger LOGGER = LoggerFactory.getLogger(JsonUtils.class);
    private static final int JSON_PATH_CACHE_SIZE = 65536;
    private static final LoadingCache<String, JSONPath> jsonPathCache = CacheBuilder
            .newBuilder()
            .maximumSize(JSON_PATH_CACHE_SIZE)
            .build(new CacheLoader<String, JSONPath>() {
                @Override
                public JSONPath load(String path) {
                    LOGGER.info("create jsonPath object for {}", path);
                    return JSONPath.compile(path);
                }
            });

    public static Object getValueByPath(JSONObject jsonObject, String path) {
        return jsonPathCache.getUnchecked(path).eval(jsonObject);
    }

    public static boolean setValueByPath(JSONObject jsonObject, String path, Object value) {
        return jsonPathCache.getUnchecked(path).set(jsonObject, value);
    }

    public static String toJsonString(Object obj) {
        return JSON.toJSONString(obj, SerializerFeature.WriteMapNullValue);
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