package io.condense.support;

public class SubscriptionLimitExceededException extends RuntimeException {

    public SubscriptionLimitExceededException(String message) {
        super(message);
    }
}
