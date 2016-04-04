# restapi-retrier
A simple Retrier Service to retry the rest calls with configurable intervals between retries.

## Requirements
* Java 8
* [RabbitMQ](https://www.rabbitmq.com/download.html)
* [Redis](http://redis.io/topics/quickstart)


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

### Schedule a Retry Request

```
POST http://localhost:8200/retry

HEADERS: 
Content-Type: application/json
x-retry-queue: test

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
