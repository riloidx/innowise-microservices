package com.innowise.orderservice.exception;


import com.innowise.orderservice.dto.response.ErrorResponse;
import com.innowise.orderservice.dto.response.ValidationErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static com.innowise.orderservice.util.ErrorResponseHelper.buildErrorResponse;
import static com.innowise.orderservice.util.ErrorResponseHelper.buildValidationErrorResponse;


@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException e,
                                                                                         HttpServletRequest request) {
        var body = buildValidationErrorResponse(e, request);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler({OrderNotFoundException.class, ItemNotFoundException.class})
    public ResponseEntity<ErrorResponse> handleResourceNotFound(RuntimeException e,
                                                                   HttpServletRequest request) {
        var body = buildErrorResponse(e, HttpStatus.NOT_FOUND, request);

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    @ExceptionHandler(ItemAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleResourceAlreadyExists(RuntimeException e,
                                                                 HttpServletRequest request) {
        var body = buildErrorResponse(e, HttpStatus.CONFLICT, request);

        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(RuntimeException e,
                                                                        HttpServletRequest request) {
        var body = buildErrorResponse(e, HttpStatus.BAD_REQUEST, request);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(ExternalUserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFoundException(RuntimeException e,
                                                                     HttpServletRequest request) {
        var body = buildErrorResponse(e, HttpStatus.NOT_FOUND, request);

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e,
                                                         HttpServletRequest request) {
        var body = buildErrorResponse(e, HttpStatus.INTERNAL_SERVER_ERROR, request);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}
