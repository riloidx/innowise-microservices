package com.riloidx.orderservice.exception;

public class ItemNotFoundException extends RuntimeException {
    public ItemNotFoundException(String field, String value) {
        super("Item with " + field + "=" + value + " not found");
    }
}
