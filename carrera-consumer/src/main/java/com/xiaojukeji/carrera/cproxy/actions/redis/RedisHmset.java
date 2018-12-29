package com.xiaojukeji.carrera.cproxy.actions.redis;

import redis.clients.jedis.Jedis;

import java.util.Map;


public class RedisHmset implements RedisCommand {
    public String key;
    public Map<String, String> hash;

    public RedisHmset(String key, Map<String, String> hash) {
        this.key = key;
        this.hash = hash;
    }

    @Override
    public String toString() {
        return "RedisHmset{" +
                "key='" + key + '\'' +
                ", hash=" + hash +
                '}';
    }

    @Override
    public String getCommandName() {
        return "hmset";
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public String execute(Jedis jedis) {
        return jedis.hmset(key, hash);
    }
}