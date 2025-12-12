package com.innowise.orderservice.dto.response;

import java.math.BigDecimal;

public record ItemResponseDto(
        Long id,
        String name,
        BigDecimal price
) {}
