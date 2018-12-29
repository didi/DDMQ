package com.xiaojukeji.carrera.cproxy.actions.redis;

import redis.clients.jedis.Jedis;


public class RedisMset implements RedisCommand {
    String[] kvs;

    public RedisMset(String[] kvs) {
        this.kvs = kvs;
    }

    @Override
    public String getCommandName() {
        return "mset";
    }

    @Override
    public String getKey() {
        return null;
    }

    @Override
    public String execute(Jedis jedis) {
        return jedis.mset(kvs);
    }
}