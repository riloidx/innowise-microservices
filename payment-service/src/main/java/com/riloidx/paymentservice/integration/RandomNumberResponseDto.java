package com.riloidx.paymentservice.integration;

public record RandomNumberResponseDto(
        String status,
        int min,
        int max,
        int random
) {
}
