package com.innowise.userservice.exception;

public class PaymentCardNotFoundException extends RuntimeException {
    public PaymentCardNotFoundException(String field, String value) {
        super("Payment card with " + field + "=" + value + " not found");
    }
}
