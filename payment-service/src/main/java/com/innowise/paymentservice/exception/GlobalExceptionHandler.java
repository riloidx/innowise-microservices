package com.innowise.paymentservice.exception;


import com.innowise.paymentservice.dto.response.ErrorResponse;
import com.innowise.paymentservice.dto.response.ValidationErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static com.innowise.paymentservice.util.ErrorResponseHelper.buildErrorResponse;
import static com.innowise.paymentservice.util.ErrorResponseHelper.buildValidationErrorResponse;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException e,
                                                                                         HttpServletRequest request) {
        var body = buildValidationErrorResponse(e, request);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(ExternalServiceException.class)
    public ResponseEntity<ErrorResponse> handleExternalServiceException(ExternalServiceException e,
                                                                        HttpServletRequest request) {
        var body = buildErrorResponse(e, HttpStatus.BAD_GATEWAY, request);

        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(body);
    }
}
