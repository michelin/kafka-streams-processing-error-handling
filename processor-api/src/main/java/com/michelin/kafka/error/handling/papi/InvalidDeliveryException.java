package com.michelin.kafka.error.handling.papi;

public class InvalidDeliveryException extends RuntimeException {

    public InvalidDeliveryException(String message) {
        super(message);
    }
}
