package com.innowise.orderservice.service;

import com.innowise.orderservice.config.FeignConfig;
import com.innowise.orderservice.dto.response.UserResponseDto;
import com.innowise.orderservice.exception.ExternalUserNotFoundException;
import feign.FeignException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "user-service", url = "${integration.user-service.url}", configuration = FeignConfig.class)
public interface UserServiceClient {

    @GetMapping("/api/users/{id}")
    @CircuitBreaker(name = "userService", fallbackMethod = "fallbackUser")
    UserResponseDto getUserById(@PathVariable("id") long id);

    default UserResponseDto fallbackUser(long id, Throwable ex) {
        if (ex instanceof FeignException.NotFound) {
            throw new ExternalUserNotFoundException("id", String.valueOf(id));
        }
        return new UserResponseDto(id, "unknown", "Unknown", "unavailable");
    }
}
