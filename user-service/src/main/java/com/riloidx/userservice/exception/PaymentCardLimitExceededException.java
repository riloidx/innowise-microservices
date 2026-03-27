package com.riloidx.userservice.exception;

public class PaymentCardLimitExceededException extends RuntimeException {
    public PaymentCardLimitExceededException(String message) {
        super(message);
    }
}
