package com.innowise.userservice.exception;

public class PaymentCardAlreadyExistsException extends RuntimeException {
    public PaymentCardAlreadyExistsException(String field, String value) {
        super("Payment card with " + field + "=" + value + " already exists");
    }
}
