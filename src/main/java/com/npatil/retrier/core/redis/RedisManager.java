package com.npatil.retrier.core.redis;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.npatil.retrier.core.managed.Redis;
import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.Jedis;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.codahale.metrics.MetricRegistry.name;

/**
 * Created by nikhil.p on 31/03/16.
 */
@Slf4j
@Singleton
public class RedisManager {
    private final Redis redis;
    private final MetricRegistry metricRegistry;

    private final Timer redisReadTimer;
    private final Timer redisWriteTimer;
    private final Timer getLockTimer;
    private final Timer releaseLockTimer;

    @Inject
    public RedisManager(Redis redis, MetricRegistry metricRegistry) {
        this.redis = redis;
        this.metricRegistry = metricRegistry;
        this.redisReadTimer = this.metricRegistry.timer(name(RedisManager.class, "readTime"));
        this.redisWriteTimer = this.metricRegistry.timer(name(RedisManager.class, "writeTime"));
        this.getLockTimer = this.metricRegistry.timer(name(RedisManager.class, "getLock"));
        this.releaseLockTimer = this.metricRegistry.timer(name(RedisManager.class, "releaseLock"));
    }

    public boolean getLock(String key, int timeout) {
        String value = null;
        try (Jedis jedis = redis.getResource(); Timer.Context context = this.getLockTimer.time()) {
            value = jedis.getSet(key, key);
            if (Objects.isNull(value)) jedis.expire(key, timeout);
        } catch (Exception e) { return false; }
        return Objects.isNull(value);
    }

    public boolean getLock(String key) {
        String value = null;
        try (Jedis jedis = redis.getResource(); Timer.Context context = this.getLockTimer.time()) {
            value = jedis.getSet(key, key);
            if (Objects.isNull(value)) jedis.sadd("locks."+ InetAddress.getLocalHost().getHostAddress(),key);
        } catch (Exception e){ return false; }
        return Objects.isNull(value);
    }

    public void releaseLock(String key) {
        log.info("Releasing consumer locks");
        try (Jedis jedis = redis.getResource(); Timer.Context context = this.releaseLockTimer.time()) {
            jedis.del(key);
            jedis.srem("locks."+ InetAddress.getLocalHost().getHostAddress(),key);
            log.info("Released consumer locks");
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public String getValue(String key) {
        try (Jedis jedis = redis.getResource(); Timer.Context context = this.redisReadTimer.time()) {
            return jedis.get(key);
        } catch (Exception e) {
            return null;
        }
    }

    public void setValue(String key, String value) {
        try (Jedis jedis = redis.getResource(); Timer.Context context = this.redisWriteTimer.time()) {
            jedis.set(key, value);
        } catch (Exception e) {
        }
    }

    public void setValue(String key, String value, int timeout) {
        try (Jedis jedis = redis.getResource(); Timer.Context context = this.redisWriteTimer.time()) {
            jedis.setex(key, timeout, value);
        } catch (Exception e) {
        }
    }

    public void addToSet(String key, String value) {
        try (Jedis jedis = redis.getResource(); Timer.Context context = this.redisWriteTimer.time()) {
            jedis.sadd(key, value);
        } catch (Exception e) {
        }
    }

    public List<String> getFromSet(String key) {
        try (Jedis jedis = redis.getResource(); Timer.Context context = this.redisWriteTimer.time()) {
            return new ArrayList<String>(jedis.smembers(key));
        } catch (Exception e) {
        }
        return null;
    }

    public void removeFromSet(String key, String value) {
        try (Jedis jedis = redis.getResource(); Timer.Context context = this.redisWriteTimer.time()) {
            jedis.srem(key, value);
        } catch (Exception e) {
        }
    }

    public String popSet(String key) {
        try (Jedis jedis = redis.getResource(); Timer.Context context = this.redisWriteTimer.time()) {
            return jedis.spop(key);
        } catch (Exception e) {
            return null;
        }
    }

    public Long getSetSize(String key) {
        try (Jedis jedis = redis.getResource(); Timer.Context context = this.redisWriteTimer.time()) {
            return jedis.scard(key);
        } catch (Exception e) {
        }
        return 0L;
    }

    public void addToList(String key, String value) {
        try (Jedis jedis = redis.getResource(); Timer.Context context = this.redisWriteTimer.time()) {
            jedis.rpush(key, value);
        } catch (Exception e) {
        }
    }

    public String getFromList(String key, int index) {
        try (Jedis jedis = redis.getResource(); Timer.Context context = this.redisWriteTimer.time()) {
            return jedis.lindex(key, index);
        } catch (Exception e) {
        }
        return null;
    }

    public Long getListSize(String key) {
        try (Jedis jedis = redis.getResource(); Timer.Context context = this.redisWriteTimer.time()) {
            return jedis.llen(key);
        } catch (Exception e) {
        }
        return 0L;
    }
}
