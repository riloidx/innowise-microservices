package com.riloidx.orderservice.exception;

public class ExternalUserNotFoundException extends RuntimeException {
    public ExternalUserNotFoundException(String field, String value) {
        super("User with " + field + "=" + value + " not found");
    }
}
