package com.xiaojukeji.carrera.cproxy.actions;

import com.alibaba.fastjson.JSONObject;
import com.xiaojukeji.carrera.config.v4.cproxy.RedisConfiguration;
import com.xiaojukeji.carrera.cproxy.config.ConsumerGroupConfig;
import com.xiaojukeji.carrera.cproxy.consumer.UpstreamJob;
import com.xiaojukeji.carrera.cproxy.actions.redis.RedisCommand;
import com.xiaojukeji.carrera.cproxy.utils.LogUtils;
import com.xiaojukeji.carrera.cproxy.utils.MetricUtils;
import com.xiaojukeji.carrera.cproxy.utils.StringUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.RandomUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.util.Pool;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class RedisAction implements Action {
    private static final Logger LOGGER = LoggerFactory.getLogger(UpstreamJob.class);

    private class RedisConnection {
        List<JedisPool> jedisPools = new ArrayList<>();
        RedisConfiguration config;

        synchronized JedisPool pickJedisPool() {
            if (CollectionUtils.isEmpty(jedisPools)) {
                return null;
            } else {
                return jedisPools.get(RandomUtils.nextInt(0, jedisPools.size()));
            }
        }

        synchronized void shutdown() {
            jedisPools.forEach(Pool::close);
            jedisPools.clear();
        }

        synchronized void tryUpdate(RedisConfiguration newConfig) {
            if (Objects.equals(newConfig, config)) return;
            shutdown();

            config = newConfig;
            for (String addr : config.getHosts()) {
                String[] tokens = StringUtils.split(addr, ':');
                String host = tokens[0];
                int port = Integer.parseInt(tokens[1]);
                jedisPools.add(new JedisPool(config.getJedisPool(), host, port, config.getTimeout(), config.getPassword()));
            }
        }
    }

    private RedisConnection connection;

    public RedisAction(ConsumerGroupConfig config) {
        connection = new RedisConnection();
        connection.tryUpdate(config.getGroupConfig().getRedisConfig());
    }

    @Override
    public Status act(UpstreamJob job, JSONObject jsonObject) {
        JedisPool jedisPool = connection.pickJedisPool();
        if (jedisPool == null) {
            LogUtils.logErrorInfo("Redis_error", "get jedisPool failed,job={}", job);
            MetricUtils.qpsAndFilterMetric(job, MetricUtils.ConsumeResult.EXCEPTION);
            return Status.FAIL;
        }
        try (Jedis jedis = jedisPool.getResource()) {
            for (RedisCommand cmd : job.getRedisCommands()) {
                String ret = cmd.execute(jedis);
                LOGGER.debug("redis-cmd:{},return:{},job={}", cmd, ret, job);
            }
        } catch (Exception e) {
            LogUtils.logErrorInfo("Redis_error", "execute redis cmd error,job=" + job, e);
            MetricUtils.qpsAndFilterMetric(job, MetricUtils.ConsumeResult.FAILURE);
            return Status.FAIL;
        }
        MetricUtils.qpsAndFilterMetric(job, MetricUtils.ConsumeResult.SUCCESS);
        return Status.FINISH;
    }

    @Override
    public void shutdown() {
        connection.shutdown();
    }
}