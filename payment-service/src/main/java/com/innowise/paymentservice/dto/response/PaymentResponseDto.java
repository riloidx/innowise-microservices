package com.innowise.paymentservice.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
public class PaymentResponseDto {
    private Long id;
    private Long orderId;
    private Long userId;
    private String status;
    private Instant timestamp;
    private BigDecimal paymentAmount;
}
