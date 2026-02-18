package com.innowise.paymentservice.dto.response;

import java.math.BigDecimal;
import java.time.Instant;

public record PaymentResponseDto(
        String id,
        Long orderId,
        Long userId,
        String status,
        Instant timestamp,
        BigDecimal paymentAmount
) {
}
