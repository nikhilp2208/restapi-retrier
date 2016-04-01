package com.npatil.retrier.resources;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.npatil.retrier.core.redis.Redis;
import com.npatil.retrier.enums.ExchangeType;
import com.npatil.retrier.models.Exchange;
import com.npatil.retrier.models.RetryWorkflow;
import com.npatil.retrier.services.ExchangeService;
import com.npatil.retrier.services.QueueService;
import com.google.common.base.Joiner;
import com.google.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.Jedis;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Objects;

import static com.npatil.retrier.utils.RetrierUtils.Keys.workflowPrefix;

/**
 * Created by nikhil.p on 31/03/16.
 */

@Slf4j
@Path("/retry_workflow")
@Consumes(MediaType.APPLICATION_JSON)
public class RetryWorkflowResource {
    private QueueService queueService;
    private ExchangeService exchangeService;
    private Redis redis;

    @Inject
    public RetryWorkflowResource(QueueService queueService, ExchangeService exchangeService, Redis redis) {
        this.queueService = queueService;
        this.exchangeService = exchangeService;
        this.redis = redis;
    }

    @POST
    public void createRetryWorkflow(RetryWorkflow retryWorkflow) throws Exception {
        String workflow;
        try (Jedis jedis = redis.getResource()) {
            workflow = jedis.get(Joiner.on(".").join("workflow", retryWorkflow.getName()));
        }

        if(Objects.nonNull(workflow)) {
            throw new WebApplicationException("workflow already exists!", Response.Status.CONFLICT);
        }

        Exchange exchange = new Exchange(retryWorkflow.getName(), ExchangeType.DIRECT);
        exchangeService.createExchange(exchange);
        retryWorkflow.setExchange(exchange);
        retryWorkflow.setInternalQueues(queueService.getInternalQueues(retryWorkflow));

        retryWorkflow.getInternalQueues().forEach(internalQueue -> {
            queueService.createInternalQueue(retryWorkflow, internalQueue);
        });

        try (Jedis jedis = redis.getResource()) {
            jedis.set(Joiner.on(".").join(workflowPrefix, retryWorkflow.getName()), new ObjectMapper().writeValueAsString(retryWorkflow));
        }
    }
}
