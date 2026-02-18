package com.innowise.orderservice.unit;

import com.innowise.orderservice.config.UserServiceProperties;
import com.innowise.orderservice.dto.response.UserResponseDto;
import com.innowise.orderservice.exception.ExternalUserNotFoundException;
import com.innowise.orderservice.service.UserServiceClientImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class UserServiceClientImplTest {

    @Test
    void fallbackUserShouldReturnUnknownUserOnGenericError() {
        UserServiceProperties properties = new UserServiceProperties("http://localhost", "test-token");
        UserServiceClientImpl service = new UserServiceClientImpl(null, properties);
        
        long userId = 1L;
        RuntimeException genericError = new RuntimeException("Service unavailable");

        Mono<UserResponseDto> result = service.fallbackUser(userId, genericError);

        StepVerifier.create(result)
                .expectNextMatches(user ->
                        user.id() == userId &&
                        "unknown".equals(user.name()) &&
                        "Unknown".equals(user.surname()) &&
                        "unavailable".equals(user.email())
                )
                .verifyComplete();
    }

    @Test
    void fallbackUserShouldPropagateExternalUserNotFoundException() {
        UserServiceProperties properties = new UserServiceProperties("http://localhost", "test-token");
        UserServiceClientImpl service = new UserServiceClientImpl(null, properties);
        
        long userId = 999L;
        ExternalUserNotFoundException notFoundError = new ExternalUserNotFoundException("id", String.valueOf(userId));

        Mono<UserResponseDto> result = service.fallbackUser(userId, notFoundError);

        StepVerifier.create(result)
                .expectError(ExternalUserNotFoundException.class)
                .verify();
    }
}
