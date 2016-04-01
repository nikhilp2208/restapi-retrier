package com.npatil.retrier.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.*;

import java.util.List;

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
public class RetryWorkflow {
    @JsonProperty
    @NonNull
    private String name;

    @JsonProperty
    @NonNull
    private long[] retryDelays;

    private Exchange exchange;

    private List<InternalQueue> internalQueues;
}
