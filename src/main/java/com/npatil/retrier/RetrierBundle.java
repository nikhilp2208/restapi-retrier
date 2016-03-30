package com.npatil.retrier;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import io.dropwizard.ConfiguredBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import lombok.Getter;

import java.util.concurrent.ScheduledExecutorService;

/**
 * Created by nikhil.p on 31/03/16.
 */
public class RetrierBundle implements ConfiguredBundle<RetrierConfiguration> {

    private volatile Channel channel;

    @Getter
    private Connection connection;

    @Override
    public void run(RetrierConfiguration configuration, Environment environment) throws Exception {
        final ScheduledExecutorService scheduledExecutorService = environment.lifecycle()
                .scheduledExecutorService("executor")
                .build();

        configuration.getRabbitMqConnection()
                .buildRetryInitialConnect(environment, scheduledExecutorService, "restapi-retrier", this::connected);
    }

    private void connected(Connection connection) throws Exception {
        this.connection = connection;
        this.channel = connection.createChannel();
    }

    @Override
    public void initialize(Bootstrap<?> bootstrap) {

    }
}
