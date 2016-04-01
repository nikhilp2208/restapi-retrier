package com.npatil.retrier.core.managed;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Joiner;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import com.npatil.retrier.callables.ConsumerPollerTask;
import com.npatil.retrier.core.configuration.ConsumerConfiguration;
import com.npatil.retrier.core.redis.Redis;
import com.npatil.retrier.models.InternalQueue;
import com.npatil.retrier.services.EndRetrierConsumer;
import com.npatil.retrier.services.RetrierConsumer;
import com.rabbitmq.client.Channel;
import io.dropwizard.lifecycle.Managed;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.pool2.impl.GenericObjectPool;
import redis.clients.jedis.Jedis;

import javax.ws.rs.client.Client;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static com.npatil.retrier.utils.RetrierUtils.Keys.unconsumed;
import static com.npatil.retrier.utils.RetrierUtils.Keys.internalQueuePrefix;

/**
 * Created by nikhil.p on 31/03/16.
 */
@Slf4j
@Singleton
public class ConsumerManager implements Managed {

    private Redis redis;
    private Client client;
    private Channel channel;
    private List<String> lockedQueues = new ArrayList<>();
    private final ScheduledThreadPoolExecutor executor;
    private final Injector injector;
    private ConsumerPollerTask poller;
    private final ConsumerConfiguration consumerConfiguration;

    @Inject
    public ConsumerManager(Redis redis, GenericObjectPool<Channel> channelPool, ScheduledThreadPoolExecutor executor, Injector injector, ConsumerConfiguration consumerConfiguration, Client client) throws Exception {
        this.redis = redis;
        this.channel = channelPool.borrowObject();
        this.executor = executor;
        this.injector = injector;
        this.consumerConfiguration = consumerConfiguration;
        this.client = client;
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
        try (Jedis jedis = redis.getResource()) {
            lockedQueues.forEach(queue -> {
                try {
                    jedis.srem(Joiner.on(".").join("consumers", InetAddress.getLocalHost().getHostAddress()), queue);
                    jedis.sadd(unconsumed, queue);
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                }
            });
        }
        executor.remove(poller);
        executor.shutdownNow();
    }

    public void startConsumer() throws IOException {
        try (Jedis jedis = redis.getResource()) {
            String queue = jedis.spop(unconsumed);
            log.info("Starting consumer for queue: "+ queue);
            jedis.sadd(Joiner.on(".").join("consumers", InetAddress.getLocalHost().getHostAddress()),queue);
            lockedQueues.add(queue);
            ObjectMapper objectMapper = new ObjectMapper();
            InternalQueue internalQueue = objectMapper.readValue(jedis.get(Joiner.on(".").join(internalQueuePrefix,queue)),InternalQueue.class);
            if(internalQueue.isEndQueue()) {
                channel.basicConsume(queue, new EndRetrierConsumer(channel, client, consumerConfiguration));
            } else {
                channel.basicConsume(queue, new RetrierConsumer(channel, client, consumerConfiguration));
            }
        }
    }
}