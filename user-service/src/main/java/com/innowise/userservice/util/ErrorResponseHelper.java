package com.innowise.userservice.util;

import com.innowise.userservice.dto.response.ErrorResponse;
import com.innowise.userservice.dto.response.ValidationErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.time.Instant;
import java.util.Map;
import java.util.stream.Collectors;

public class ErrorResponseHelper {
    public static ErrorResponse buildErrorResponse(Exception e,
                                                   HttpStatus httpStatus,
                                                   HttpServletRequest request) {
        return ErrorResponse.builder()
                .timestamp(Instant.now())
                .status(httpStatus.value())
                .error(httpStatus.getReasonPhrase())
                .message(e.getMessage())
                .path(request.getRequestURI())
                .build();
    }

    public static ValidationErrorResponse buildValidationErrorResponse(MethodArgumentNotValidException e,
                                                                       HttpServletRequest request) {
        return ValidationErrorResponse.validationBuilder()
                .timestamp(Instant.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .message(e.getMessage())
                .path(request.getRequestURI())
                .validationErrors(getValidationErrors(e))
                .build();
    }

    public static Map<String, String> getValidationErrors(MethodArgumentNotValidException e) {

        return e.getBindingResult().getFieldErrors().stream()
                .filter(fe -> fe.getDefaultMessage() != null)
                .collect(Collectors.toMap(
                        FieldError::getField,
                        FieldError::getDefaultMessage,
                        (m1, m2) -> m1));
    }
}
