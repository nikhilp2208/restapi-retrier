package com.npatil.retrier.services;

import com.npatil.retrier.models.InternalQueue;
import com.npatil.retrier.models.RetryWorkflow;

import java.io.IOException;
import java.util.List;

/**
 * Created by nikhil.p on 31/03/16.
 */
public interface QueueService {

    /**
     * @param retryWorkflow
     * @param internalQueue
     * @return
     */
    boolean createInternalQueue(RetryWorkflow retryWorkflow, InternalQueue internalQueue);

    /**
     * @param retryWorkflowName
     * @return
     * @throws IOException
     */
    InternalQueue getPublishQueue(String retryWorkflowName) throws IOException;

    /**
     * @param retryWorkflowName
     * @param retryCount
     * @return
     * @throws IOException
     */
    InternalQueue getInternalQueue(String retryWorkflowName, int retryCount) throws IOException;

    /**
     * @param retryWorkflow
     * @return
     */
    List<InternalQueue> getInternalQueues(RetryWorkflow retryWorkflow);
}
