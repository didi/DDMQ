package com.didi.carrera.console.dao.model.custom;

import java.io.Serializable;

import com.alibaba.fastjson.annotation.JSONField;
import com.didi.carrera.console.common.util.FastJsonUtils;
import com.xiaojukeji.carrera.config.v4.cproxy.RedisConfiguration;
import org.apache.commons.lang3.StringUtils;


public class ConsumeGroupConfig implements Serializable {

    public static final String  key_asyncThreads = "asyncThreads";
    public static final String  key_redisConfigStr = "redisConfigStr";


    private Integer asyncThreads = 8;
    private RedisConfiguration redisConfig;

    public Integer getAsyncThreads() {
        return asyncThreads;
    }

    public void setAsyncThreads(Integer asyncThreads) {
        this.asyncThreads = asyncThreads;
    }

    public RedisConfiguration getRedisConfig() {
        return redisConfig;
    }

    public void setRedisConfig(RedisConfiguration redisConfig) {
        this.redisConfig = redisConfig;
    }

    public void setRedisConfigStr(String redisConfigStr) {
        this.redisConfig = StringUtils.isBlank(redisConfigStr) ? null : FastJsonUtils.toObject(redisConfigStr, RedisConfiguration.class);
    }

    @JSONField(serialize = false)
    public String getRedisConfigStr() {
        return this.redisConfig == null ? null : FastJsonUtils.toJson(redisConfig);
    }

    @Override
    public String toString() {
        return "ConsumeGroupConfig{" +
                "asyncThreads=" + asyncThreads +
                ", redisConfig=" + redisConfig +
                '}';
    }

}