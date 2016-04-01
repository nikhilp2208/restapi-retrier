package com.npatil.retrier;

import com.codahale.metrics.MetricRegistry;
import com.google.common.collect.Sets;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.google.inject.Singleton;
import com.npatil.retrier.callables.ConsumerPollerTask;
import com.npatil.retrier.callables.ConsumerPollerTaskImpl;
import com.npatil.retrier.core.configuration.ConsumerConfiguration;
import com.npatil.retrier.core.configuration.RedisConfiguration;
import com.npatil.retrier.core.managed.ConsumerManager;
import com.npatil.retrier.core.redis.Redis;
import com.npatil.retrier.factories.ChannelPoolFactory;
import com.npatil.retrier.services.ExchangeService;
import com.npatil.retrier.services.ExchangeServiceImpl;
import com.npatil.retrier.services.QueueService;
import com.npatil.retrier.services.QueueServiceImpl;
import com.rabbitmq.client.Channel;
import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.client.JerseyClientConfiguration;
import io.dropwizard.setup.Environment;
import lombok.AllArgsConstructor;
import org.apache.commons.pool2.impl.GenericObjectPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisSentinelPool;

import javax.ws.rs.client.Client;
import java.util.concurrent.ScheduledThreadPoolExecutor;

/**
 * Created by nikhil.p on 31/03/16.
 */
@AllArgsConstructor
public class RetrierModule extends AbstractModule {
    private RetrierBundle retrierBundle;

    @Override
    protected void configure() {
        bind(Redis.class).in(Scopes.SINGLETON);
        bind(ConsumerManager.class).in(Scopes.SINGLETON);
        bind(QueueService.class).to(QueueServiceImpl.class);
        bind(ExchangeService.class).to(ExchangeServiceImpl.class);
        bind(ConsumerPollerTask.class).to(ConsumerPollerTaskImpl.class);
    }

    @Provides
    @Singleton
    public MetricRegistry provideMetricRegistry(Environment environment) {
        return environment.metrics();
    }

    @Provides
    @Singleton
    public JedisSentinelPool provideJedisSentinelPool(RetrierConfiguration configuration) {
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        RedisConfiguration redisConfiguration = configuration.getCache();
        poolConfig.setMaxTotal(redisConfiguration.getMaxThreads());
        return new JedisSentinelPool(redisConfiguration.getMaster(), Sets.newHashSet(redisConfiguration.getSentinels().split(",")), poolConfig, redisConfiguration.getTimeout(), redisConfiguration.getPassword(), redisConfiguration.getDb());
    }

    @Provides
    @Singleton
    public ConsumerConfiguration provideConsumerConfiguration(RetrierConfiguration configuration) {
        return configuration.getConsumer();
    }

    @Provides
    @Singleton
    public GenericObjectPool<Channel> provideChannelPool() {
        return new GenericObjectPool<Channel>(new ChannelPoolFactory(retrierBundle.getConnection()));
    }

    @Provides
    @Singleton
    public Client provideJerseyClient(RetrierConfiguration configuration, Environment environment) {
        JerseyClientConfiguration clientConfiguration = configuration.getJerseyClient();
        clientConfiguration.setGzipEnabled(false);
        clientConfiguration.setGzipEnabledForRequests(false);
        clientConfiguration.setChunkedEncodingEnabled(false);
        return new JerseyClientBuilder(environment).using(clientConfiguration).build(configuration.getName());
    }

    @Provides @Singleton
    public ScheduledThreadPoolExecutor provideScheduledThreadPoolExecutor(RetrierConfiguration retrierProducerConfiguration) {
        return new ScheduledThreadPoolExecutor(retrierProducerConfiguration.getAggregatorPoolSize());
    }
}
