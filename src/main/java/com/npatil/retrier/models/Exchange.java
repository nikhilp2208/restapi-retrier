package com.npatil.retrier.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.npatil.retrier.enums.ExchangeType;
import lombok.*;

/**
 * Created by nikhil.p on 31/03/16.
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class Exchange {
    @JsonProperty
    @NonNull
    private String name;

    @JsonProperty
    @NonNull
    private ExchangeType type;
}
