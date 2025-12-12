package com.innowise.userservice.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "cache")
@Data
public class CacheProperties {
    private Map<String, Duration> ttl = new HashMap<>();
}
