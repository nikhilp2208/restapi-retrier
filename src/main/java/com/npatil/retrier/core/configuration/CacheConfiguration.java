package com.npatil.retrier.core.configuration;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import javax.validation.Valid;
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
public class CacheConfiguration {

    @JsonProperty
    @NonNull
    private String master;

    @JsonProperty
    @Valid
    @NonNull
    private String sentinels;

    @JsonProperty
    @Valid
    @NonNull
    private String password;


    @Min(2)
    @Max(4000)
    @JsonProperty
    private int timeout = 2;

    @Min(0)
    @Max(15)
    @JsonProperty
    private int db = 0;

    @Min(8)
    @Max(4096)
    @JsonProperty
    private int maxThreads = 8;

}
