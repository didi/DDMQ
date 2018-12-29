package com.xiaojukeji.carrera.cproxy.actions.redis;

import redis.clients.jedis.Jedis;


public interface RedisCommand {
    String getCommandName();

    String getKey();

    String execute(Jedis jedis);
}