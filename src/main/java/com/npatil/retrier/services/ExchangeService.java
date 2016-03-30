package com.npatil.retrier.services;


import com.npatil.retrier.models.Exchange;

import java.io.IOException;

/**
 * Created by nikhil.p on 31/03/16.
 */
public interface ExchangeService {

    void createExchange(Exchange exchange) throws IOException;
}
