package com.innowise.authservice.exception;


import com.innowise.authservice.dto.response.ErrorResponse;
import com.innowise.authservice.dto.response.ValidationErrorResponse;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static com.innowise.authservice.util.ErrorResponseHelper.buildErrorResponse;
import static com.innowise.authservice.util.ErrorResponseHelper.buildValidationErrorResponse;


@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException e,
                                                                                         HttpServletRequest request) {
        log.warn("Validation error on request to {}: {}", request.getRequestURI(), e.getMessage());
        var body = buildValidationErrorResponse(e, request);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponse> handleBadRequestException(BadRequestException e,
                                                                   HttpServletRequest request) {
        log.warn("Bad request on {}: {}", request.getRequestURI(), e.getMessage());
        var body = buildErrorResponse(e, HttpStatus.BAD_REQUEST, request);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(CredentialNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleCredentialNotFound(RuntimeException e,
                                                                  HttpServletRequest request) {
        log.warn("Credential not found on {}: {}", request.getRequestURI(), e.getMessage());
        var body = buildErrorResponse(e, HttpStatus.NOT_FOUND, request);

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    @ExceptionHandler({
            UnauthorizedException.class,
            CredentialAlreadyExistsException.class,
            InvalidCredentialsException.class
    })
    public ResponseEntity<ErrorResponse> handleConflict(RuntimeException e,
                                                        HttpServletRequest request) {
        log.warn("Conflict on {}: {}", request.getRequestURI(), e.getMessage());
        var body = buildErrorResponse(e, HttpStatus.CONFLICT, request);

        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(RuntimeException e,
                                                            HttpServletRequest request) {
        log.warn("Access denied on {}: {}", request.getRequestURI(), e.getMessage());
        var body = buildErrorResponse(e, HttpStatus.FORBIDDEN, request);

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body);
    }

    @ExceptionHandler({
            AuthenticationException.class,
            ExpiredJwtException.class
    })
    public ResponseEntity<ErrorResponse> handleUnauthorized(RuntimeException e,
                                                            HttpServletRequest request) {
        log.warn("Unauthorized access on {}: {}", request.getRequestURI(), e.getMessage());
        var body = buildErrorResponse(e, HttpStatus.UNAUTHORIZED, request);

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e,
                                                         HttpServletRequest request) {
        log.error("Internal server error on {}: {}", request.getRequestURI(), e.getMessage(), e);
        var body = buildErrorResponse(e, HttpStatus.INTERNAL_SERVER_ERROR, request);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}
