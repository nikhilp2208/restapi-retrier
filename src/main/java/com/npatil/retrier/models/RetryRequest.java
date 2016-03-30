package com.npatil.retrier.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.*;

import javax.ws.rs.core.MultivaluedHashMap;

/**
 * Created by nikhil.p on 31/03/16.
 */

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategy.LowerCaseWithUnderscoresStrategy.class)
@ToString
public class RetryRequest {
    @JsonProperty
    @NonNull
    private String requestType;

    @JsonProperty
    @NonNull
    private Object requestBody;

    @JsonProperty
    @NonNull
    private String url;

    @JsonProperty
    private MultivaluedHashMap<String,Object> headers;
//
//    @NonNull
//    private String queue;
}
