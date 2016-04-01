package com.npatil.retrier.callables;

import com.google.inject.Inject;
import com.npatil.retrier.core.managed.ConsumerManager;
import com.npatil.retrier.core.redis.Redis;
import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.Jedis;

import java.io.IOException;

import static com.npatil.retrier.utils.RetrierUtils.Keys.unconsumed;

/**
 * Created by nikhil.p on 31/03/16.
 */
@Slf4j
public class ConsumerPollerTaskImpl implements ConsumerPollerTask {

    private final Redis redis;
    private final ConsumerManager consumerManager;

    @Inject
    public ConsumerPollerTaskImpl(Redis redis, ConsumerManager consumerManager) {
        this.redis = redis;
        this.consumerManager = consumerManager;
    }

    @Override
    public void run() {
        log.info("Polling for new Consumers");
        try (Jedis jedis = redis.getResource()) {
            if (jedis.scard(unconsumed) > 0) {
                try {
                    consumerManager.startConsumer();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
