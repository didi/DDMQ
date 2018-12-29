package com.xiaojukeji.carrera.cproxy.actions.groovy;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.xiaojukeji.carrera.cproxy.actions.hbase.HbaseCommand;
import com.xiaojukeji.carrera.cproxy.actions.redis.RedisDelete;
import com.xiaojukeji.carrera.cproxy.actions.redis.RedisHdel;
import com.xiaojukeji.carrera.cproxy.actions.redis.RedisHmset;
import com.xiaojukeji.carrera.cproxy.actions.redis.RedisHset;
import com.xiaojukeji.carrera.cproxy.actions.redis.RedisMset;
import com.xiaojukeji.carrera.cproxy.actions.redis.RedisSet;
import com.xiaojukeji.carrera.cproxy.consumer.CommonMessage;
import com.xiaojukeji.carrera.cproxy.consumer.ConsumeContext;
import com.xiaojukeji.carrera.cproxy.consumer.UpstreamJob;
import org.apache.hadoop.hbase.client.Append;
import org.apache.hadoop.hbase.client.Delete;
import org.apache.hadoop.hbase.client.Increment;
import org.apache.hadoop.hbase.client.Put;

import java.util.HashMap;
import java.util.Map;


public class GroovyContext {
    private UpstreamJob job;

    public GroovyContext(UpstreamJob job) {
        super();
        this.job = job;
    }

    public String getKey() {
        return job.getMsgKey();
    }

    public CommonMessage getMessage() {
        return job.getCommonMessage();
    }

    public ConsumeContext getContext() {
        return job.getContext();
    }

    public void putHttpHeader(String key, String value) {
        job.addHttpHeader(key, value);
    }

    public void putFormParam(String key, String value) {
        job.addFormParam(key, value);
    }

    public void putQueryParam(String key, String value) {
        job.addQueryParam(key, value);
    }

    public void redisDel(String... key) {
        job.addRedisCmd(new RedisDelete(key));
    }

    public void redisSet(String key, String value) {
        job.addRedisCmd(new RedisSet(key, value));
    }

    public void redisSet(String key, String value, String nxpx) {
        job.addRedisCmd(new RedisSet(key, value, nxpx));
    }

    public void redisSet(String key, String value, String nxpx, String expx, long time) {
        job.addRedisCmd(new RedisSet(key, value, nxpx, expx, time));
    }

    public void redisHmset(String key, Map<String, String> hash) {
        job.addRedisCmd(new RedisHmset(key, hash));
    }

    public void redisMset(String... kvs) {
        job.addRedisCmd(new RedisMset(kvs));
    }

    public void redisHset(String key, String field, String value) {
        job.addRedisCmd(new RedisHset(key, field, value));
    }

    public void redisHdel(String key, String... fields) {
        job.addRedisCmd(new RedisHdel(key, fields));
    }

    public void hbasePut(String tableName, Put put) {
        job.addHbaseCmd(new HbaseCommand(tableName, put));
    }

    public void hbaseDelete(String tableName, Delete delete) {
        job.addHbaseCmd(new HbaseCommand(tableName, delete));
    }

    public void hbaseAppend(String tableName, Append append) {
        job.addHbaseCmd(new HbaseCommand(tableName, append));
    }

    public void hbaseIncrement(String tableName, Increment increment) {
        job.addHbaseCmd(new HbaseCommand(tableName, increment));
    }

    public Map<String, JSONObject> indexBinlogColumns(JSONArray columns) {
        Map<String, JSONObject> ret = new HashMap<>();
        for (Object column : columns) {
            JSONObject obj = (JSONObject) column;
            ret.put(obj.getString("n"), obj);
        }
        return ret;
    }
}