package com.innowise.userservice.exception;

import com.innowise.userservice.dto.response.ErrorResponse;
import com.innowise.userservice.dto.response.ValidationErrorResponse;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static com.innowise.userservice.util.ErrorResponseHelper.buildErrorResponse;
import static com.innowise.userservice.util.ErrorResponseHelper.buildValidationErrorResponse;


@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException e,
                                                                                         HttpServletRequest request) {
        var body = buildValidationErrorResponse(e, request);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponse> handleBadRequestException(BadRequestException e,
                                                                   HttpServletRequest request) {
        var body = buildErrorResponse(e, HttpStatus.BAD_REQUEST, request);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler({UserNotFoundException.class, PaymentCardNotFoundException.class})
    public ResponseEntity<ErrorResponse> handlePaymentCardNotFound(RuntimeException e,
                                                                   HttpServletRequest request) {
        var body = buildErrorResponse(e, HttpStatus.NOT_FOUND, request);

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    @ExceptionHandler({UserAlreadyExistsException.class,
            PaymentCardAlreadyExistsException.class,
            PaymentCardLimitExceededException.class})
    public ResponseEntity<ErrorResponse> handleUserAlreadyExists(RuntimeException e,
                                                                 HttpServletRequest request) {
        var body = buildErrorResponse(e, HttpStatus.CONFLICT, request);

        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    @ExceptionHandler({
            UnauthorizedException.class,
            AuthenticationException.class,
            ExpiredJwtException.class,
            JwtException.class
    })
    public ResponseEntity<ErrorResponse> handleUnauthorizedException(RuntimeException e,
                                                                    HttpServletRequest request) {
        var body = buildErrorResponse(e, HttpStatus.UNAUTHORIZED, request);

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
    }

    @ExceptionHandler({
            ForbiddenException.class,
            AccessDeniedException.class
    })
    public ResponseEntity<ErrorResponse> handleForbiddenException(RuntimeException e,
                                                                 HttpServletRequest request) {
        var body = buildErrorResponse(e, HttpStatus.FORBIDDEN, request);

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e,
                                                         HttpServletRequest request) {
        var body = buildErrorResponse(e, HttpStatus.INTERNAL_SERVER_ERROR, request);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}
