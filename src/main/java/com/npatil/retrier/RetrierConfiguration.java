package com.npatil.retrier;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.npatil.retrier.core.configuration.CacheConfiguration;
import com.npatil.retrier.core.configuration.ConsumerConfiguration;
import com.npatil.retrier.models.Queue;
import com.sun.istack.internal.NotNull;
import io.codemonastery.dropwizard.rabbitmq.ConnectionFactory;
import io.dropwizard.Configuration;
import io.dropwizard.client.JerseyClientConfiguration;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.Valid;
import java.util.Map;

/**
 * Created by nikhil.p on 31/03/16.
 */
@Getter
@Setter
public class RetrierConfiguration extends Configuration {

    @Valid
    @NotNull
    private ConnectionFactory rabbitMqConnection = new ConnectionFactory();

    @JsonProperty
    @NonNull
    @NotEmpty
    private String name;

    private int aggregatorPoolSize = 16;

    @JsonProperty
    private Map<String,Queue> queues;

    @Valid
    @javax.validation.constraints.NotNull
    @JsonProperty
    private JerseyClientConfiguration jerseyClient = new JerseyClientConfiguration();

    @Valid
    @NonNull
    @JsonProperty
    private CacheConfiguration cache;

    @Valid
    @NonNull
    @JsonProperty
    private ConsumerConfiguration consumer;
}
