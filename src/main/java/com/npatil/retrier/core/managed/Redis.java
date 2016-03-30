package com.npatil.retrier.core.managed;

import com.google.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisSentinelPool;

import javax.inject.Singleton;
import java.util.Objects;

/**
 * Created by nikhil.p on 31/03/16.
 */
@Slf4j
@Singleton
public class Redis {

    private final JedisSentinelPool jedisSentinelPool;

    @Inject
    public Redis(JedisSentinelPool jedisSentinelPool) {
        this.jedisSentinelPool = jedisSentinelPool;
    }

    public Jedis getResource() {
        return jedisSentinelPool.getResource();
    }

    public void ping() throws Exception {
        try (Jedis jedis = jedisSentinelPool.getResource()) {
            jedis.ping();
        }
    }

    public void destroy() {
        if (!Objects.isNull(jedisSentinelPool)) jedisSentinelPool.destroy();
    }
}
