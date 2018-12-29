package com.xiaojukeji.carrera.cproxy.actions.redis;

import redis.clients.jedis.Jedis;


public class RedisSet implements RedisCommand {
    public String key;
    public String value;
    public String nxpx;
    public String expx;
    public long time;


    public RedisSet(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public RedisSet(String key, String value, String nxpx) {
        this.key = key;
        this.value = value;
        this.nxpx = nxpx;
    }

    public RedisSet(String key, String value, String nxpx, String expx, long time) {
        this.key = key;
        this.value = value;
        this.nxpx = nxpx;
        this.expx = expx;
        this.time = time;
    }

    @Override
    public String toString() {
        return "RedisSet{" +
                "key='" + key + '\'' +
                ", value='" + value + '\'' +
                (nxpx != null ? ", nxpx='" + nxpx + '\'' : "") +
                (expx != null ? ", expx='" + expx + '\'' + ", time=" + time : "") +
                '}';
    }

    @Override
    public String getCommandName() {
        return "set";
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public String execute(Jedis jedis) {
        if (expx != null) {
            return jedis.set(key, value, nxpx, expx, time);
        } else if (nxpx != null) {
            return jedis.set(key, value, nxpx);
        } else {
            return jedis.set(key, value);
        }
    }
}