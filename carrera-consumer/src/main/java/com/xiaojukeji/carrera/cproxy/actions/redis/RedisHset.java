package com.xiaojukeji.carrera.cproxy.actions.redis;

import redis.clients.jedis.Jedis;


public class RedisHset implements RedisCommand {
    private String key;
    private String field;
    private String value;

    public RedisHset(String key, String field, String value) {
        this.key = key;
        this.field = field;
        this.value = value;
    }

    @Override
    public String toString() {
        return "RedisHset{" +
                "key='" + key + '\'' +
                ", field='" + field + '\'' +
                ", value='" + value + '\'' +
                '}';
    }

    @Override
    public String getCommandName() {
        return "hset";
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public String execute(Jedis jedis) {
        return String.valueOf(jedis.hset(key, field, value));
    }
}