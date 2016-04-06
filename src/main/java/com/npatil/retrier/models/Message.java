package com.npatil.retrier.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.*;

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
public class Message {
    @JsonProperty
    private String messageId;

    @JsonProperty
    private String groupId;

    @JsonProperty
    @NonNull
    private RetryRequest retryRequest;

    @JsonProperty
    private RetryRequest retryFailureRequest;

}
