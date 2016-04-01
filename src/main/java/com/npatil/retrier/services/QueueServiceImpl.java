package com.npatil.retrier.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Joiner;
import com.google.inject.Inject;
import com.npatil.retrier.core.redis.Redis;
import com.npatil.retrier.enums.InternalQueueType;
import com.npatil.retrier.models.InternalQueue;
import com.npatil.retrier.models.RetryWorkflow;
import com.rabbitmq.client.Channel;
import org.apache.commons.pool2.impl.GenericObjectPool;
import redis.clients.jedis.Jedis;

import java.io.IOException;
import java.util.*;

import static com.npatil.retrier.utils.RetrierUtils.Keys.unconsumed;
import static com.npatil.retrier.utils.RetrierUtils.Keys.internalQueuePrefix;

/**
 * Created by nikhil.p on 31/03/16.
 */
public class QueueServiceImpl implements QueueService {

    private Channel channel;
    private Redis redis;

    @Inject
    public QueueServiceImpl(GenericObjectPool<Channel> channelPool, Redis redis) throws Exception {
        this.channel = channelPool.borrowObject();
        this.redis = redis;
    }

    @Override
    public boolean createInternalQueue(RetryWorkflow retryWorkflow, InternalQueue internalQueue) {
        try {
            Map<String,Object> args = new HashMap<>();

            if(Objects.nonNull(internalQueue.getDeadLetterRoutingKey())) {
                args.put("x-dead-letter-routing-key",internalQueue.getDeadLetterRoutingKey());
                args.put("x-dead-letter-exchange",retryWorkflow.getExchange().getName());
            }

            if (internalQueue.getDelay() != 0)
                args.put("x-message-ttl",internalQueue.getDelay());

            channel.queueDeclare(internalQueue.getName(), true, false, false, args);
            channel.queueBind(internalQueue.getName(), retryWorkflow.getExchange().getName(), internalQueue.getName());
            ObjectMapper objectMapper = new ObjectMapper();
            try (Jedis jedis = redis.getResource()) {
                jedis.set(Joiner.on(".").join(internalQueuePrefix,internalQueue.getName()), objectMapper.writeValueAsString(internalQueue));
                jedis.rpush(retryWorkflow.getName(),internalQueue.getName());
                if (internalQueue.getType().equals(InternalQueueType.RETRY))
                    jedis.sadd(unconsumed,internalQueue.getName());
            }

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    public InternalQueue getPublishQueue(String retryWorkflowName) throws IOException {
        try (Jedis jedis = redis.getResource()) {
            String internalQueueName = jedis.lindex(retryWorkflowName, 0);
            ObjectMapper objectMapper = new ObjectMapper();
            InternalQueue internalQueue = objectMapper.readValue(jedis.get(Joiner.on(".").join(internalQueuePrefix, internalQueueName)), InternalQueue.class);
            return internalQueue;
        }
    }

    @Override
    public InternalQueue getInternalQueue(String retryWorkflowName, int retryCount) throws IOException {
        try (Jedis jedis = redis.getResource()) {
            String internalQueueName = jedis.lindex(retryWorkflowName, retryCount);
            ObjectMapper objectMapper = new ObjectMapper();
            InternalQueue internalQueue = objectMapper.readValue(jedis.get(Joiner.on(".").join(internalQueuePrefix, internalQueueName)), InternalQueue.class);
            return internalQueue;
        }
    }

    @Override
    public List<InternalQueue> getInternalQueues(RetryWorkflow retryWorkflow) {
        int retryCount = retryWorkflow.getRetryDelays().length;
        List<InternalQueue> internalQueues = new ArrayList<>();

        for (int i = 1; i <= retryCount; i++) {
            InternalQueue delayQueue = new InternalQueue.Builder(Joiner.on("_").join("DQ",retryWorkflow.getName(),i)).type(InternalQueueType.DELAY).delay(retryWorkflow.getRetryDelays()[i-1]).deadLetterRoutingKey(Joiner.on("_").join("RQ",retryWorkflow.getName(),i)).endQueue(false).build();
            InternalQueue retryQueue = new InternalQueue.Builder(Joiner.on("_").join("RQ",retryWorkflow.getName(),i)).type(InternalQueueType.RETRY).delay(0L).build();

            if (i != retryCount) {
                retryQueue.setDeadLetterRoutingKey(Joiner.on("_").join("DQ",retryWorkflow.getName(),(i+1)));
                retryQueue.setEndQueue(false);
            } else {
                retryQueue.setEndQueue(true);
            }

            internalQueues.add(delayQueue);
            internalQueues.add(retryQueue);
        }

        return internalQueues;
    }
}
