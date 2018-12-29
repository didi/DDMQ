package com.xiaojukeji.carrera.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.fastjson.serializer.SerializerFeature;


public class FastJsonUtils {
    public static String toJsonString(Object obj) {
        return JSONObject.toJSONString(obj, SerializerFeature.WriteDateUseDateFormat, SerializerFeature.WriteMapNullValue, SerializerFeature.DisableCircularReferenceDetect);
    }

    public static <T> T parseObject(String json, TypeReference<T> type){
        return JSON.parseObject(json, type);
    }
}