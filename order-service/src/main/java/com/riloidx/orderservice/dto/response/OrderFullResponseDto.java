package com.riloidx.orderservice.dto.response;

import com.riloidx.orderservice.enums.OrderStatus;

import java.math.BigDecimal;
import java.util.List;

public record OrderFullResponseDto (
        Long id,
        OrderStatus status,
        Boolean deleted,
        BigDecimal totalPrice,
        UserResponseDto user,
        List<OrderItemResponseDto> orderItems
) {}
