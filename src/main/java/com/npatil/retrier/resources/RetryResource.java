package com.npatil.retrier.resources;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.npatil.retrier.models.InternalQueue;
import com.npatil.retrier.models.Message;
import com.npatil.retrier.services.QueueService;
import com.npatil.retrier.services.RetrierProducer;
import lombok.extern.slf4j.Slf4j;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import java.util.Objects;

/**
 * Created by nikhil.p on 11/03/16.
 */

@Slf4j
@Path("/retry")
@Consumes(MediaType.APPLICATION_JSON)
public class RetryResource {
    private RetrierProducer producer;
    private QueueService queueService;

    @Inject
    public RetryResource(RetrierProducer producer, QueueService queueService) throws Exception {
        this.producer = producer;
        this.queueService = queueService;
    }

    @POST
    public void retry(Message message, @Context HttpHeaders httpHeaders) throws Exception {
        String retryWorkflowName = httpHeaders.getRequestHeaders().getFirst("x-retry-workflow");
        if (Objects.isNull(retryWorkflowName)) retryWorkflowName = "default";
        InternalQueue internalQueue;
        internalQueue = queueService.getPublishQueue(retryWorkflowName);
        producer.send(retryWorkflowName, internalQueue, new ObjectMapper().writeValueAsString(message));
    }
}
