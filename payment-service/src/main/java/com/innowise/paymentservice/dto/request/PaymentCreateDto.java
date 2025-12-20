package com.innowise.paymentservice.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record PaymentCreateDto(
        @NotNull(message = "Order id is required")
        Long orderId,

        @NotNull(message = "User id is required")
        Long userId,

        @NotNull(message = "Amount is required")
        @Positive(message = "Amount must be greater than zero")
        BigDecimal paymentAmount
) {}
