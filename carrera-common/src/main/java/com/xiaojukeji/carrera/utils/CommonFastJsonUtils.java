package com.xiaojukeji.carrera.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONPath;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;


public class CommonFastJsonUtils {
    private static Logger LOGGER = LoggerFactory.getLogger(CommonFastJsonUtils.class);

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

    public static boolean setValueByPath(JSONObject jsonObject, String path, Object value) {
        return jsonPathCache.getUnchecked(path).set(jsonObject, value);
    }

    public static String toJsonString(Object obj) {
        return JSON.toJSONString(obj, SerializerFeature.WriteDateUseDateFormat, SerializerFeature.WriteMapNullValue, SerializerFeature.DisableCircularReferenceDetect);
    }

    public static String toJsonStringDefault(Object obj) {
        return JSON.toJSONString(obj);
    }

    public static <T> T toObject(String json, Class<T> cls) {
        try {
            return JSON.parseObject(json, cls);
        } catch (Exception e) {
            LOGGER.error("toObject exception, err:{}", e.getMessage(), e);
        }
        return null;
    }

    public static <T> T toObject(InputStream is, Class<T> cls) {
        try {
            return JSON.parseObject(is, cls);
        } catch (Exception e) {
            LOGGER.error("toObject exception, err:{}", e.getMessage(), e);
        }
        return null;
    }

    public static <T> T toObject(String json, TypeReference<T> typeReference) {
        try {
            return JSON.parseObject(json, typeReference);
        } catch (Exception e) {
            LOGGER.error("toObject exception, err:{}", e.getMessage(), e);
        }
        return null;
    }
}