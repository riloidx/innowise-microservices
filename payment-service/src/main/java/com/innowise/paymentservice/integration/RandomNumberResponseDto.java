package com.innowise.paymentservice.integration;

public record RandomNumberResponseDto(
        String status,
        int min,
        int max,
        int random
) {
}
