package com.xiaojukeji.carrera.cproxy.actions.redis;

import redis.clients.jedis.Jedis;

import java.util.Arrays;


public class RedisDelete implements RedisCommand {
    public String[] keys;

    public RedisDelete(String... keys) {
        this.keys = keys;
    }

    @Override
    public String toString() {
        return "RedisDelete{" +
                "keys=" + Arrays.toString(keys) +
                '}';
    }

    @Override
    public String getCommandName() {
        return "del";
    }

    @Override
    public String getKey() {
        if (keys == null || keys.length == 0) return "null";
        if (keys.length == 1) return keys[0];
        return Arrays.toString(keys);
    }

    @Override
    public String execute(Jedis jedis) {
        return String.valueOf(jedis.del(keys));
    }
}