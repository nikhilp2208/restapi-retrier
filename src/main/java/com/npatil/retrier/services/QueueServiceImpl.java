package com.npatil.retrier.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.npatil.retrier.core.redis.RedisManager;
import com.npatil.retrier.enums.InternalQueueType;
import com.npatil.retrier.models.InternalQueue;
import com.npatil.retrier.models.Queue;
import com.rabbitmq.client.Channel;
import org.apache.commons.pool2.impl.GenericObjectPool;

import java.io.IOException;
import java.util.*;

/**
 * Created by nikhil.p on 31/03/16.
 */
public class QueueServiceImpl implements QueueService {

    private Channel channel;
    private RedisManager redisManager;

    @Inject
    public QueueServiceImpl(GenericObjectPool<Channel> channelPool, RedisManager redisManager) throws Exception {
        this.channel = channelPool.borrowObject();
        this.redisManager = redisManager;
    }

    public QueueServiceImpl(Channel channel) {
        this.channel = channel;
    }

    @Override
    public boolean createInternalQueue(Queue queue, InternalQueue internalQueue) {
        try {
            Map<String,Object> args = new HashMap<>();

            if(Objects.nonNull(internalQueue.getDeadLetterRoutingKey())) {
                args.put("x-dead-letter-routing-key",internalQueue.getDeadLetterRoutingKey());
                args.put("x-dead-letter-exchange","retry-exchange");
            }

            if (internalQueue.getDelay() != 0)
                args.put("x-message-ttl",internalQueue.getDelay());

            channel.queueDeclare(internalQueue.getName(), true, false, false, args);
            channel.queueBind(internalQueue.getName(), "retry-exchange", internalQueue.getName());
            ObjectMapper objectMapper = new ObjectMapper();
            redisManager.setValue("internalQueue."+ internalQueue.getName(), objectMapper.writeValueAsString(internalQueue));
            redisManager.addToList(queue.getName(),internalQueue.getName());
            if (internalQueue.getType().equals(InternalQueueType.RETRY))
                redisManager.addToSet("unconsumed",internalQueue.getName());
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    public InternalQueue getInternalQueue(Queue queue, int retryCount) throws IOException {
        String internalQueueName = redisManager.getFromList(queue.getName(),retryCount);
        ObjectMapper objectMapper = new ObjectMapper();
        InternalQueue internalQueue = objectMapper.readValue(redisManager.getValue("internalQueue."+ internalQueueName),InternalQueue.class);
        return internalQueue;
    }

    @Override
    public InternalQueue getPublishQueue(String queueName) throws IOException {
        String internalQueueName = redisManager.getFromList(queueName,0);
        ObjectMapper objectMapper = new ObjectMapper();
        InternalQueue internalQueue = objectMapper.readValue(redisManager.getValue("internalQueue."+ internalQueueName),InternalQueue.class);
        return internalQueue;
    }

    @Override
    public InternalQueue getInternalQueue(String queueName, int retryCount) throws IOException {
        String internalQueueName = redisManager.getFromList(queueName,retryCount);
        ObjectMapper objectMapper = new ObjectMapper();
        InternalQueue internalQueue = objectMapper.readValue(redisManager.getValue("internalQueue."+ internalQueueName),InternalQueue.class);
        return internalQueue;
    }

    @Override
    public List<InternalQueue> getInternalQueues(Queue queue) {
        int retryCount = queue.getRetryDelays().length;
        List<InternalQueue> internalQueues = new ArrayList<>();

        for (int i = 1; i <= retryCount; i++) {
            InternalQueue delayQueue = new InternalQueue.Builder("DQ_"+queue.getName()+"_"+i).type(InternalQueueType.DELAY).delay(queue.getRetryDelays()[i-1]).deadLetterRoutingKey("RQ_"+queue.getName()+"_"+i).endQueue(false).build();
            InternalQueue retryQueue = new InternalQueue.Builder("RQ_"+queue.getName()+"_"+i).type(InternalQueueType.RETRY).delay(0L).build();

            if (i != retryCount) {
                retryQueue.setDeadLetterRoutingKey("DQ_"+queue.getName()+"_"+(i+1));
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
