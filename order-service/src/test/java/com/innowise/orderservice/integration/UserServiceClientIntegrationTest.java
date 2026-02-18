package com.innowise.orderservice.integration;

import com.innowise.orderservice.exception.ExternalUserNotFoundException;
import com.innowise.orderservice.service.UserServiceClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.test.StepVerifier;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

class UserServiceClientIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private UserServiceClient userServiceClient;

    @Test
    void getUserByIdShouldReturnUserWhenServiceRespondsSuccessfully() {
        long userId = 1L;

        stubFor(get(urlPathEqualTo("/api/users/1"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(200)
                        .withBody("""
                                {
                                    "id": 1,
                                    "name": "Alice",
                                    "surname": "Smith",
                                    "email": "alice@test.com"
                                }
                                """)));

        StepVerifier.create(userServiceClient.getUserById(userId))
                .assertNext(user -> {
                    assertEquals(1L, user.id());
                    assertEquals("Alice", user.name());
                    assertEquals("Smith", user.surname());
                    assertEquals("alice@test.com", user.email());
                })
                .verifyComplete();

        verify(getRequestedFor(urlPathEqualTo("/api/users/1"))
                .withHeader("Authorization", matching("Bearer .*")));
    }

    @Test
    void getUserByIdShouldThrowExternalUserNotFoundWhen404() {
        long userId = 999L;

        stubFor(get(urlPathEqualTo("/api/users/999"))
                .willReturn(aResponse()
                        .withStatus(404)));

        StepVerifier.create(userServiceClient.getUserById(userId))
                .expectError(ExternalUserNotFoundException.class)
                .verify();

        verify(getRequestedFor(urlPathEqualTo("/api/users/999")));
    }
}
