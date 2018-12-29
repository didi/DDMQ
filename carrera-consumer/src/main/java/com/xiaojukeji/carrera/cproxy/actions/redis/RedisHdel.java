package com.xiaojukeji.carrera.cproxy.actions.redis;

import redis.clients.jedis.Jedis;

import java.util.Arrays;


public class RedisHdel implements RedisCommand {
    private String key;
    private String[] fields;

    public RedisHdel(String key, String... fields) {
        this.key = key;
        this.fields = fields;
    }

    @Override
    public String toString() {
        return "RedisHdel{" +
                "key='" + key + '\'' +
                ", fields=" + Arrays.toString(fields) +
                '}';
    }

    @Override
    public String getCommandName() {
        return "hdel";
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public String execute(Jedis jedis) {
        return String.valueOf(jedis.hdel(key, fields));
    }
}