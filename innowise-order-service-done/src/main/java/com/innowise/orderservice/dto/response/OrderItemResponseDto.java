package com.innowise.orderservice.dto.response;

import java.math.BigDecimal;

public record OrderItemResponseDto(
        Long id,
        Integer quantity,
        Long itemId,
        String name,
        BigDecimal price
) {}
