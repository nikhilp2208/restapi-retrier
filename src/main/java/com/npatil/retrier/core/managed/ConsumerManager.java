package com.npatil.retrier.core.managed;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import com.npatil.retrier.callables.ConsumerPollerTask;
import com.npatil.retrier.core.configuration.ConsumerConfiguration;
import com.npatil.retrier.core.redis.RedisManager;
import com.npatil.retrier.models.InternalQueue;
import com.npatil.retrier.services.EndRetrierConsumer;
import com.npatil.retrier.services.RetrierConsumer;
import com.rabbitmq.client.Channel;
import io.dropwizard.lifecycle.Managed;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.pool2.impl.GenericObjectPool;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by nikhil.p on 31/03/16.
 */
@Slf4j
@Singleton
public class ConsumerManager implements Managed {

    private RedisManager redisManager;
    private WebClient webClient;
    private Channel channel;
    private List<String> lockedQueues = new ArrayList<>();
    private final ScheduledThreadPoolExecutor executor;
    private final Injector injector;
    private ConsumerPollerTask poller;
    private final ConsumerConfiguration consumerConfiguration;

    @Inject
    public ConsumerManager(RedisManager redisManager, GenericObjectPool<Channel> channelPool, ScheduledThreadPoolExecutor executor, Injector injector, ConsumerConfiguration consumerConfiguration, WebClient webClient) throws Exception {
        this.redisManager = redisManager;
        this.channel = channelPool.borrowObject();
        this.executor = executor;
        this.injector = injector;
        this.consumerConfiguration = consumerConfiguration;
        this.webClient = webClient;
    }

    @Override
    public void start() throws Exception {
        log.info("Starting Consumer Manager");
        executor.setRemoveOnCancelPolicy(true);
        executor.setContinueExistingPeriodicTasksAfterShutdownPolicy(false);
        executor.setExecuteExistingDelayedTasksAfterShutdownPolicy(false);

        poller = injector.getInstance(ConsumerPollerTask.class);
        executor.scheduleWithFixedDelay(poller, 1000, consumerConfiguration.getPollerDelay(), TimeUnit.MILLISECONDS);
    }

    @Override
    public void stop() throws Exception {
        log.info("Stopping Consumer Manager");
        lockedQueues.forEach(queue -> {
            redisManager.releaseLock(queue);
            redisManager.addToSet("unconsumed",queue); //remove this or the one above. Redundant locking
        });
        executor.remove(poller);
        executor.shutdownNow();
    }

    public void startConsumer() throws IOException {
        String queue = redisManager.popSet("unconsumed");
        log.info("Starting consumer for queue: "+ queue);
        if(redisManager.getLock(queue)) {
            lockedQueues.add(queue);
            ObjectMapper objectMapper = new ObjectMapper();
            InternalQueue internalQueue = objectMapper.readValue(redisManager.getValue("internalQueue."+ queue),InternalQueue.class);
            if(internalQueue.isEndQueue()) {
                channel.basicConsume(queue, new EndRetrierConsumer(channel, webClient, redisManager, consumerConfiguration));
            } else {
                channel.basicConsume(queue, new RetrierConsumer(channel, webClient, redisManager, consumerConfiguration));
            }
        }
    }
}
