package com.npatil.retrier.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.npatil.retrier.core.configuration.ConsumerConfiguration;
import com.npatil.retrier.core.managed.WebClient;
import com.npatil.retrier.core.redis.RedisManager;
import com.npatil.retrier.models.Message;
import com.npatil.retrier.models.RetryRequest;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.Objects;

/**
 * Created by nikhil.p on 31/03/16.
 */
@Slf4j
@Getter
@Setter
public class RetrierConsumer extends DefaultConsumer{

    WebClient webClient;
    RedisManager redisManager;
    ConsumerConfiguration consumerConfiguration;

    public RetrierConsumer(Channel channel, WebClient webClient, RedisManager redisManager, ConsumerConfiguration consumerConfiguration) {
        super(channel);
        this.webClient = webClient;
        this.redisManager = redisManager;
        this.consumerConfiguration = consumerConfiguration;
    }


    // TODO: Clean up handle delivery
    @Override
    public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
        if (getChannel().isOpen()) {
            final String messageString = new String(body);
            ObjectMapper objectMapper = new ObjectMapper();
            Message message = objectMapper.readValue(messageString, Message.class);

//            long retryCount = (long) properties.getHeaders().get("x-retry-count");
//            String queueName = (String) properties.getHeaders().get("x-retry-queue");
//            log.info("Consumed message from queue: "+ queueName);

            // Will be triggered if the previous message with same groupId is in next retry queue
//            if (consumerConfiguration.isGroupingEnabled() && moveToNextRetry(message.getGroupId(),message.getMessageId())) {
//                reQueueMessage(queueName ,retryCount, message);
//                getChannel().basicAck(envelope.getDeliveryTag(), false);
//                return;
//            }

            Response response = null;
            try {
                response = deliverMessage(message.getRetryRequest());
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (Objects.nonNull(response) && response.getStatusInfo().getFamily().equals(Response.Status.Family.SUCCESSFUL)) {
                getChannel().basicAck(envelope.getDeliveryTag(), false);
            } else {
                getChannel().basicNack(envelope.getDeliveryTag(), false, false);
            }
//                redisManager.removeFromSet(message.getGroupId(),message.getMessageId());
//                log.info("Delivered message:" + message.getMessageId());
//            } else {
//                long maxRetryCount = redisManager.getListSize(queueName);
//                if (retryCount < maxRetryCount) {
//                    reQueueMessage(queueName ,retryCount, message);
//                } else {
//                    deliverMessage(message.getRetryFailureRequest());
//                    redisManager.removeFromSet(message.getGroupId(),message.getMessageId());
//                }
//            }

//            log.info("Processed message from queue: "+ queueName);
        }
    }

//    private boolean moveToNextRetry(String groupId, String messageId) {
//        List<String> storedMessageId = redisManager.getFromSet(groupId);
//        if (Objects.nonNull(storedMessageId) && !storedMessageId.contains(messageId)) {
//            return true;
//        }
//        return false;
//    }

    private Response deliverMessage(RetryRequest retryRequest) throws Exception{
        String requestType = retryRequest.getRequestType();
        switch (requestType.toUpperCase()) {
            case "POST" :
                return webClient.buildPost(retryRequest.getUrl(),retryRequest.getHeaders(), retryRequest.getRequestBody());
            case "PUT" :
                return webClient.buildPut(retryRequest.getUrl(),retryRequest.getHeaders(), retryRequest.getRequestBody());
            case "GET" :
                return webClient.buildGet(retryRequest.getUrl(),retryRequest.getHeaders());
            default:
                return null;
        }
    }
}
