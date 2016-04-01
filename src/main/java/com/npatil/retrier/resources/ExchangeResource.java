package com.npatil.retrier.resources;

import com.google.inject.Inject;
import com.npatil.retrier.models.Exchange;
import com.npatil.retrier.services.ExchangeService;
import lombok.extern.slf4j.Slf4j;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;

/**
 * Created by nikhil.p on 31/03/16.
 */

@Slf4j
@Path("/exchange")
@Consumes(MediaType.APPLICATION_JSON)
public class ExchangeResource {
    private ExchangeService exchangeService;

    @Inject
    public ExchangeResource(ExchangeService exchangeService) {
        this.exchangeService = exchangeService;
    }

    @POST
    public void createExchange(Exchange exchange) throws Exception {
        exchangeService.createExchange(exchange);
    }
}
