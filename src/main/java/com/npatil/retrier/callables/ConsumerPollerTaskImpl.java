package com.npatil.retrier.callables;

import com.google.inject.Inject;
import com.npatil.retrier.core.managed.ConsumerManager;
import com.npatil.retrier.core.redis.RedisManager;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

/**
 * Created by nikhil.p on 31/03/16.
 */
@Slf4j
public class ConsumerPollerTaskImpl implements ConsumerPollerTask {

    private final RedisManager redisManager;
    private final ConsumerManager consumerManager;

    @Inject
    public ConsumerPollerTaskImpl(RedisManager redisManager, ConsumerManager consumerManager) {
        this.redisManager = redisManager;
        this.consumerManager = consumerManager;
    }

    @Override
    public void run() {
        log.info("Polling for new Consumers");
        if (redisManager.getSetSize("unconsumed") > 0) {
            try {
                consumerManager.startConsumer();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
