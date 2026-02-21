package com.innowise.userservice.integration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers(disabledWithoutDocker = true)
public abstract class BaseIntegrationTest {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @Container
    @ServiceConnection
    static final GenericContainer<?> redis = new GenericContainer<>("redis:7").
            withExposedPorts(6379);

    @Autowired
    protected MockMvc mockMvc;

    // Admin token that never expires
    protected static final String ADMIN_TOKEN = "eyJhbGciOiJIUzUxMiJ9.eyJyb2xlIjoiQURNSU4iLCJ1c2VySWQiOjEsInN1YiI6ImFkbWluIiwiaWF0IjoxNzY0MDc1MjEwLCJleHAiOjkwMTc2NDA3NTIxMH0._u94HFfBSYpdCfsRoLvP_dfg8YlEg6vMKot0H0VWYWg0kJ3JfsaDcBd5MDpPmNCSGr2MgfATiW7vn0gveQCY5g";

    protected HttpHeaders createAuthHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-User-Id", "1");
        headers.set("X-User-Role", "ADMIN");
        return headers;
    }
}
