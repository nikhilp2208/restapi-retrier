# restapi-retrier
A simple Retrier Service to retry the rest calls with configurable intervals between retries.

## Requirements
* Java 8
* [RabbitMQ](https://www.rabbitmq.com/download.html)
* [Redis](http://redis.io/topics/quickstart)


## Setup

* Update the appropriate configs in retrier.yml
* Start the RabbitMQ server, Redis server, Redis sentinels
* Start the restapi-retrier server
* Create `default` retry workflow using the [Create Retry workflow](#create-retry-workflow) API


## APIs

### Create Retry workflow

```
POST http://localhost:8200/retry_workflow

HEADERS: 
Content-Type: application/json

{
    "name": "test",
    "retry_delays": [5000,10000,15000]
}
```
`name` : Name of the retry workflow

`retry_delays` : Array of retry intervals, specified in milliseconds. The number of retries will be equal to the number of elements in this array.

### Schedule a Retry Request

```
POST http://localhost:8200/retry

HEADERS: 
Content-Type: application/json
x-retry-workflow: test

{
    "message_id" : "foo",
    "group_id" : "bar",
    "retry_request": {
        "request_type": "POST",
        "request_body": {
            "key":"success",
            "test":"test"
        },
        "url": "http://localhost/",
        "headers": {"header1":["success"]}
    },
    "retry_failure_request": {
        "request_type": "POST",
        "request_body":  {
            "key":"failed",
            "abc":"abc"
        },
        "url": "http://localhost/",
        "headers": {"header1":["failed"]}
    }
}
```

`x-retry-workflow`: Name of the retry workflow to use for retrying. If this header is not passed, `default` workflow will be used by default. So ensure you create default workflow during inital setup.

`message_id`: unique id for a message.

`group_id` **(Currently grouping is not supported)**: ID to group the messages. 
Eg: If the first message in the group fails in the first retry and is moved to the second retry queue, subsequent messages with same group id will be directly moved to the second retry queue where the first message exists. This way the ordering of messages within a group will be maintained. 

`retry_request`: The request which has to be retried.
<br>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;`request_type`: Allowed request types "POST", "PUT", "GET".
<br>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;`request_body`: JSON request body.
<br>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;`url`: Request end point.
<br>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;`headers`: Request headers.

`retry_failure_request`: The fallback request which has to be executed if the all the retries fail.


## Implementation Details

### Retry Workflow
Retry workflow is a series of rabbitmq queues that the retry request flows through. 
There are two types of queues in the retry workflow:
* Delay Queue (*prefixed with DQ*)
* Retry Queue (*prefixed with RQ*)

### Delay Queue
It is the queue where the delay will be introduced between subsequent retries. It uses `x-dead-letter-exchange` and `x-message-ttl` to delay the messages before pushing them to the next retry queue. For more information on this, check [this link](http://yuserinterface.com/dev/2013/01/08/how-to-schedule-delay-messages-with-rabbitmq-using-a-dead-letter-exchange/). There won't be any consumers for this queue. 

### Retry Queue
There will be consumers running for Retry Queues. Consumers will pick the message from this queue and make the http call with headers and body specified in the `retry_request`. If the response recieved is 2xx, then `ack` will be sent to the queue. Else `nack` will be sent to the queue and the message will be moved to the next *Delay queue* through `dead-letter-exchange`.
<br>
If the retry queue is final queue in the workflow, in case non-2xx response on the `retry_request`, the `retry_failure_request` will be executed. If `retry_failure_request` is not specified, the message will be ignored and `nack` will be sent. Since there won't be any `dead-letter-exchange` associated with the last queue in workflow, this message will be discarded.



Note: In both Delay queues and Retry queues `x-dead-letter-routing-key` will be set to name of the next retry queue/delay queue respectively, as "queue_name" is being used as the routing key to push to the queue.

![retry_workflow](retry_workflow.png?raw=true)
