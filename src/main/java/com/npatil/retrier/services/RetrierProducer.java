package com.npatil.retrier.services;

import com.google.inject.Inject;
import com.npatil.retrier.models.InternalQueue;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPool;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by nikhil.p on 31/03/16.
 */
@Slf4j
public class RetrierProducer {
    private ObjectPool<Channel> channelPool;
    private final int PERSISTENCE_MESSAGE = 2;

    @Inject
    public RetrierProducer(GenericObjectPool<Channel> channelPool) {
        this.channelPool = channelPool;
    }

    public void send(String retryWorkflowName, InternalQueue queue, String message) {
        try{
            Channel channel = channelPool.borrowObject();
            Map<String, Object> headers = new HashMap<String, Object>();
            AMQP.BasicProperties.Builder props = new AMQP.BasicProperties.Builder().contentType("text/plain").deliveryMode(PERSISTENCE_MESSAGE).headers(headers);
            channel.basicPublish(retryWorkflowName, queue.getName(), props.build(), message.getBytes());
            log.info(message + " sent");
            channelPool.returnObject(channel);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}