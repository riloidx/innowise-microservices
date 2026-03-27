package com.riloidx.orderservice.dto.response;

import com.riloidx.orderservice.enums.OrderStatus;

import java.math.BigDecimal;
import java.util.List;

public record OrderResponseDto(
        Long id,
        OrderStatus status,
        Boolean deleted,
        BigDecimal totalPrice,
        List<OrderItemResponseDto> orderItems
) {}
