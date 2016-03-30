package com.npatil.retrier.services;

import com.npatil.retrier.models.InternalQueue;
import com.npatil.retrier.models.Queue;

import java.io.IOException;
import java.util.List;

/**
 * Created by nikhil.p on 31/03/16.
 */
public interface QueueService {

    /**
     * @param queue
     * @param internalQueue
     * @return
     */
    boolean createInternalQueue(Queue queue, InternalQueue internalQueue);

    /**
     * @param queue
     * @param retryCount
     * @return
     */
    InternalQueue getInternalQueue(Queue queue, int retryCount) throws IOException;

    /**
     * @param queueName
     * @return
     * @throws IOException
     */
    InternalQueue getPublishQueue(String queueName) throws IOException;

    /**
     * @param queueName
     * @param retryCount
     * @return
     * @throws IOException
     */
    InternalQueue getInternalQueue(String queueName, int retryCount) throws IOException;

    /**
     * @param queue
     * @return
     */
    List<InternalQueue> getInternalQueues(Queue queue);
}
