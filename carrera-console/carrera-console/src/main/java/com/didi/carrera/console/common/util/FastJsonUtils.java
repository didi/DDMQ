package com.didi.carrera.console.common.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.fastjson.serializer.SerializerFeature;


public class FastJsonUtils {

    public static String toJsonString(Object obj) {
        return JSON.toJSONString(obj, SerializerFeature.WriteDateUseDateFormat, SerializerFeature.WriteMapNullValue, SerializerFeature.DisableCircularReferenceDetect);
    }

    public static String toJson(Object obj) {
        return JSON.toJSONString(obj);
    }

    public static <T> T toObject(String json, Class<T> cls) {
        return JSON.parseObject(json, cls);
    }

    public static <T> T toObject(String json, TypeReference<T> typeReference) {
        return JSON.parseObject(json, typeReference);
    }

}