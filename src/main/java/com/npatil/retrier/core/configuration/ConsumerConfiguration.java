package com.npatil.retrier.core.configuration;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

/**
 * Created by nikhil.p on 31/03/16.
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class ConsumerConfiguration {

    @Min(0)
    @Max(100000)
    @JsonProperty
    private int pollerDelay;

    @JsonProperty
    private boolean groupingEnabled;
}
