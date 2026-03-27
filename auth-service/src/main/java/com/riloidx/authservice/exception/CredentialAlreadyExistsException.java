package com.riloidx.authservice.exception;

public class CredentialAlreadyExistsException extends RuntimeException {
    public CredentialAlreadyExistsException(String field, String value) {
        super("Credential with " + field + "=" + value + " already exists");
    }
}
