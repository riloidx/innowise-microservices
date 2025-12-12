package com.innowise.authservice.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "jwt")
@Data
public class JwtProperties {
    private Token access;
    private Token refresh;

    @Data
    public static class Token {
        private String key;
        private long expirationMs;

    }
}