package com.innowise.orderservice.dto.request;

import com.innowise.orderservice.enums.OrderStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;

import java.util.List;

public record OrderUpdateDto(
        OrderStatus status,

        @Size(min = 1, message = "Order must contain at least one item")
        @Valid
        List<OrderItemDto> items
) {}
