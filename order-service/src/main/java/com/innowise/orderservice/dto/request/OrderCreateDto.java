package com.innowise.orderservice.dto.request;


import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record OrderCreateDto(
        @NotNull(message = "User ID must not be null")
        Long userId,

        @NotNull(message = "Order items list must not be null")
        @Size(min = 1, message = "Order must contain at least one item")
        @Valid
        List<OrderItemDto> items
) {}
