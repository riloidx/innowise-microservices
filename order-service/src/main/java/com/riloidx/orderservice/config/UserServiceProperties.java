package com.riloidx.orderservice.config;

import org.springframework.boot.context.properties.ConfigurationProperties;


@ConfigurationProperties(prefix = "integration.user-service")
public record UserServiceProperties (
        String url,
        String token
) {}
