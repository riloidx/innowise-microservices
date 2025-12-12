package com.innowise.orderservice.exception;

public class ItemAlreadyExistsException extends RuntimeException {
    public ItemAlreadyExistsException(String field, String value) {
        super("Item with " + field + "=" + value + " already exists");
    }
}
