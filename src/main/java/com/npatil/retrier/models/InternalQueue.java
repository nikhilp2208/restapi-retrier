package com.npatil.retrier.models;

import com.npatil.retrier.enums.InternalQueueType;
import lombok.*;

/**
 * Created by nikhil.p on 31/03/16.
 */
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class InternalQueue {

    private String name;

    private Long delay;

    private String deadLetterRoutingKey;

    private InternalQueueType type;

    private boolean endQueue;

    public InternalQueue(Builder builder) {
        this.name = builder.name;
        this.delay = builder.delay;
        this.deadLetterRoutingKey = builder.deadLetterRoutingKey;
        this.type = builder.type;
        this.endQueue = builder.endQueue;
    }

    public static class Builder {
        private final String name;
        private long delay;
        private String deadLetterRoutingKey;
        private InternalQueueType type;
        private boolean endQueue;

        public Builder(String name) {
            this.name = name;
        }

        public Builder type(InternalQueueType type) {
            this.type = type;
            return this;
        }

        public Builder delay(Long delay) {
            this.delay = delay;
            return this;
        }

        public Builder deadLetterRoutingKey(String deadLetterRoutingKey) {
            this.deadLetterRoutingKey = deadLetterRoutingKey;
            return this;
        }

        public Builder endQueue(boolean endQueue) {
            this.endQueue = endQueue;
            return this;
        }

        public InternalQueue build() {
            return new InternalQueue(this);
        }
    }
}
