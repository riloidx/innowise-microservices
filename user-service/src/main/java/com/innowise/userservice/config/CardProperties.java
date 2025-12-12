package com.innowise.userservice.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@ConfigurationProperties(prefix = "user.card")
@Data
public class CardProperties {
    private int maxLimit;
}
