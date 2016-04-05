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


## Example APIs

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
