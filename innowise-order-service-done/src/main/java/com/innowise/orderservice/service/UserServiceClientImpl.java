package com.innowise.orderservice.service;

import com.innowise.orderservice.config.UserServiceProperties;
import com.innowise.orderservice.dto.response.UserResponseDto;
import com.innowise.orderservice.exception.ExternalUserNotFoundException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class UserServiceClientImpl implements UserServiceClient {

    private final WebClient userServiceWebClient;
    private final UserServiceProperties userServiceProperties;


    @Override
    @CircuitBreaker(name = "userService", fallbackMethod = "fallbackUser")
    public UserResponseDto getUserById(long id) {
        return userServiceWebClient.get()
                .uri(uri -> uri
                        .path("/api/users/{id}")
                        .build(id))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + userServiceProperties.token())
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError,  response -> {
                    if (response.statusCode().equals(HttpStatus.NOT_FOUND)) {
                        return Mono.error(new ExternalUserNotFoundException("id", String.valueOf(id)));
                    }
                    return response.createException().flatMap(Mono::error);
                })
                .bodyToMono(UserResponseDto.class)
                .block();
    }

    public UserResponseDto fallbackUser(long id, Throwable ex) {
        if (ex instanceof ExternalUserNotFoundException) {
            throw (ExternalUserNotFoundException) ex;
        }

        return new UserResponseDto(
                id,
                "unknown",
                "Unknown",
                "unavailable"
        );
    }
}
