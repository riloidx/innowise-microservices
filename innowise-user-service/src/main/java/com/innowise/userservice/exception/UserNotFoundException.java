package com.innowise.userservice.exception;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String field, String value) {
        super("User with " + field + "=" + value + " not found");
    }
}
