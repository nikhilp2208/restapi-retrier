package com.npatil.retrier.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.npatil.retrier.core.configuration.ConsumerConfiguration;
import com.npatil.retrier.models.Message;
import com.npatil.retrier.models.RetryRequest;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
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

    Client client;
    ConsumerConfiguration consumerConfiguration;

    public RetrierConsumer(Channel channel, Client client, ConsumerConfiguration consumerConfiguration) {
        super(channel);
        this.client = client;
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
                log.info(envelope.getRoutingKey() + ": Delivered message with id: " + message.getMessageId());
                getChannel().basicAck(envelope.getDeliveryTag(), false);
            } else {
                getChannel().basicNack(envelope.getDeliveryTag(), false, false);
                log.info(envelope.getRoutingKey() + ": Delivery failed for message with id: " + message.getMessageId());
            }
        }
    }

    private Response deliverMessage(RetryRequest retryRequest) throws Exception{
        String requestType = retryRequest.getRequestType();
        WebTarget webTarget = client.target(retryRequest.getUrl());
        switch (requestType.toUpperCase()) {
            case "POST" :
                return webTarget.request().headers(retryRequest.getHeaders()).post(Entity.entity(retryRequest.getRequestBody(), MediaType.APPLICATION_JSON_TYPE+ ";charset=UTF-8"));
            case "PUT" :
                return webTarget.request().headers(retryRequest.getHeaders()).post(Entity.entity(retryRequest.getRequestBody(), MediaType.APPLICATION_JSON_TYPE+ ";charset=UTF-8"));
            case "GET" :
                return webTarget.request().headers(retryRequest.getHeaders()).buildGet().invoke();
            default:
                return null;
        }
    }
}
