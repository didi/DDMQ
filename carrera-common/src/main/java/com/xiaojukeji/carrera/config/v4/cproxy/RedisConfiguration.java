package com.xiaojukeji.carrera.config.v4.cproxy;

import java.util.List;

import com.xiaojukeji.carrera.config.ConfigurationValidator;
import com.xiaojukeji.carrera.utils.CommonFastJsonUtils;
import org.apache.commons.collections4.CollectionUtils;
import redis.clients.jedis.JedisPoolConfig;


public class RedisConfiguration implements ConfigurationValidator, Cloneable {
    private String password;
    private List<String> hosts;
    private int timeout = 5000;
    private JedisPoolConfig jedisPool = new JedisPoolConfig();

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public List<String> getHosts() {
        return hosts;
    }

    public void setHosts(List<String> hosts) {
        this.hosts = hosts;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public JedisPoolConfig getJedisPool() {
        return jedisPool;
    }

    public void setJedisPool(JedisPoolConfig jedisPool) {
        this.jedisPool = jedisPool;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RedisConfiguration that = (RedisConfiguration) o;

        if (timeout != that.timeout) return false;
        if (password != null ? !password.equals(that.password) : that.password != null) return false;
        if (hosts != null ? !hosts.equals(that.hosts) : that.hosts != null) return false;
        return jedisPool != null ? jedisPool.equals(that.jedisPool) : that.jedisPool == null;
    }

    @Override
    public int hashCode() {
        int result = password != null ? password.hashCode() : 0;
        result = 31 * result + (hosts != null ? hosts.hashCode() : 0);
        result = 31 * result + timeout;
        result = 31 * result + (jedisPool != null ? jedisPool.hashCode() : 0);
        return result;
    }

    @Override
    public RedisConfiguration clone() {
        return CommonFastJsonUtils.toObject(CommonFastJsonUtils.toJsonString(this), RedisConfiguration.class);
    }

    @Override
    public boolean validate() throws ConfigException {
        if (CollectionUtils.isEmpty(hosts)) throw new ConfigException("[RedisConfig] hosts is empty");
        if (timeout < 0) throw new ConfigException("[RedisConfig] timeout<0, timeout=" + timeout);
        return true;
    }
}