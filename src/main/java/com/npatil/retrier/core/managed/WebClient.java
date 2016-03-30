package com.npatil.retrier.core.managed;

import com.google.inject.Inject;
import io.dropwizard.lifecycle.Managed;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Singleton;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.util.concurrent.Future;

@Slf4j
@Singleton
@Getter
public class WebClient implements Managed {

    private final Client client;


    @Inject
    public WebClient(Client client) {
        this.client = client;
    }

    @Override
    public void start() throws Exception {
        log.info("Starting web client without auth");
    }

    @Override
    public void stop() throws Exception {
        log.info("Stopping web client");
        client.close();
    }

    public Response buildPost(String url, MultivaluedMap<String, Object> headers, Object message) throws Exception {
        WebTarget webtarget = client.target(url);
        return webtarget.request().headers(headers).post(Entity.entity(message,MediaType.APPLICATION_JSON_TYPE+ ";charset=UTF-8"));
    }

    public Response buildPut(String url, MultivaluedMap<String, Object> headers, Object message) {
        WebTarget webtarget = client.target(url);
        return webtarget.request().accept(MediaType.APPLICATION_JSON_TYPE).headers(headers).buildPut(Entity.json(message)).invoke();
    }

    public Future<Response> buildFuturePost(String url, MultivaluedMap<String, Object> headers, Object message) {
        WebTarget webtarget = client.target(url);
        return webtarget.request().accept(MediaType.APPLICATION_JSON_TYPE).headers(headers).buildPost(Entity.json(message)).submit();
    }

    public Response buildGet(String url, MultivaluedMap<String, Object> headers) {
        WebTarget webTarget = client.target(url);
        return webTarget.request().accept(MediaType.APPLICATION_JSON_TYPE).headers(headers).buildGet().invoke();
    }

    public Future<Response> buildFutureGet(String url, MultivaluedMap<String, Object> headers) {
        WebTarget webTarget = client.target(url);
        return webTarget.request().accept(MediaType.APPLICATION_JSON_TYPE).headers(headers).buildGet().submit();
    }
}
