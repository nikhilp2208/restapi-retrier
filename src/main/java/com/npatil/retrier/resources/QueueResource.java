package com.npatil.retrier.resources;

import com.google.inject.Inject;
import com.npatil.retrier.models.InternalQueue;
import com.npatil.retrier.models.Queue;
import com.npatil.retrier.services.QueueService;
import lombok.extern.slf4j.Slf4j;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import java.util.List;

/**
 * Created by nikhil.p on 31/03/16.
 */
@Slf4j
@Path("/queue")
@Consumes(MediaType.APPLICATION_JSON)
public class QueueResource {
    private QueueService queueService;

    @Inject
    public QueueResource(QueueService queueService) {
        this.queueService = queueService;
    }

    @POST
    public void createQueue(Queue queue) throws Exception {
        List<InternalQueue> internalQueues = queueService.getInternalQueues(queue);

        internalQueues.forEach(internalQueue -> {
            queueService.createInternalQueue(queue, internalQueue);
        });
    }
}
