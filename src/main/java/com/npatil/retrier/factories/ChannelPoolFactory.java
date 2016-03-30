package com.npatil.retrier.factories;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;

/**
 * Created by nikhil.p on 31/03/16.
 */
public class ChannelPoolFactory extends BasePooledObjectFactory<Channel> {
    private Connection connection;

    public ChannelPoolFactory(Connection connection) {
        this.connection = connection;

    }

    @Override
    public Channel create() throws Exception {
        return connection.createChannel();
    }

    @Override
    public PooledObject<Channel> wrap(Channel obj) {
        return new DefaultPooledObject<Channel>(obj);
    }
}
