package com.npatil.retrier.services;

import com.google.inject.Inject;
import com.npatil.retrier.models.Exchange;
import com.rabbitmq.client.Channel;
import org.apache.commons.pool2.impl.GenericObjectPool;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by nikhil.p on 31/03/16.
 */
public class ExchangeServiceImpl implements ExchangeService {
    private Channel channel;

    @Inject
    public ExchangeServiceImpl(GenericObjectPool<Channel> channelPool) throws Exception {
        this.channel = channelPool.borrowObject();
    }

    @Override
    public void createExchange(Exchange exchange) throws IOException {
        Map<String, Object> args = new HashMap<String, Object>();
        args.put("x-delayed-type", exchange.getType().toString().toLowerCase());
        channel.exchangeDeclare(exchange.getName(),"x-delayed-message",true,false,args);
    }
}
