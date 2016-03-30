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
public class EndRetrierConsumer extends DefaultConsumer{

    WebClient webClient;
    RedisManager redisManager;
    ConsumerConfiguration consumerConfiguration;

    public EndRetrierConsumer(Channel channel, WebClient webClient, RedisManager redisManager, ConsumerConfiguration consumerConfiguration) {
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

            Response response = null;
            try {
                response = deliverMessage(message.getRetryRequest());
            } catch (Exception e) {
                e.printStackTrace();
            }


            if (Objects.nonNull(response) && response.getStatusInfo().getFamily().equals(Response.Status.Family.SUCCESSFUL)) {
                getChannel().basicAck(envelope.getDeliveryTag(), false);
            } else {
                try {
                    deliverMessage(message.getRetryFailureRequest());
                    getChannel().basicAck(envelope.getDeliveryTag(), false);
                } catch (Exception e) {
                    getChannel().basicNack(envelope.getDeliveryTag(), false, true);
                    e.printStackTrace();

                }
            }
        }
    }

    private Response deliverMessage(RetryRequest retryRequest) throws Exception {
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
