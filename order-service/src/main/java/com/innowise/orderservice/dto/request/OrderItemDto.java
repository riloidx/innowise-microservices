package com.innowise.orderservice.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record OrderItemDto(
        @NotNull(message = "Item ID must not be null")
        Long itemId,

        @Min(value = 1, message = "Quantity must be at least 1")
        Integer quantity
) {}