package com.innowise.orderservice.integration;

import com.innowise.orderservice.dto.response.UserResponseDto;
import com.innowise.orderservice.exception.ExternalUserNotFoundException;
import com.innowise.orderservice.service.UserServiceClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

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

        UserResponseDto user = userServiceClient.getUserById(userId);

        assertEquals(1L, user.id());
        assertEquals("Alice", user.name());
        assertEquals("Smith", user.surname());
        assertEquals("alice@test.com", user.email());

        verify(getRequestedFor(urlPathEqualTo("/api/users/1")));
    }

    @Test
    void getUserByIdShouldThrowExternalUserNotFoundWhen404() {
        long userId = 999L;

        stubFor(get(urlPathEqualTo("/api/users/999"))
                .willReturn(aResponse()
                        .withStatus(404)));

        assertThrows(ExternalUserNotFoundException.class, () ->
                userServiceClient.getUserById(userId)
        );

        verify(getRequestedFor(urlPathEqualTo("/api/users/999")));
    }
}