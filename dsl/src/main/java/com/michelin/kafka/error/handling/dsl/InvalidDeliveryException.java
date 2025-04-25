package com.michelin.kafka.error.handling.dsl;

public class InvalidDeliveryException extends RuntimeException {

    public InvalidDeliveryException(String message) {
        super(message);
    }
}
