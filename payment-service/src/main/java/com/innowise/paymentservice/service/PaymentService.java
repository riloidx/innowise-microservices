package com.innowise.paymentservice.service;

import com.innowise.paymentservice.dto.request.PaymentCreateDto;
import com.innowise.paymentservice.dto.response.PaymentResponseDto;
import com.innowise.paymentservice.dto.response.TotalSum;
import com.innowise.paymentservice.entity.PaymentStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public interface PaymentService {
    PaymentResponseDto create(PaymentCreateDto paymentCreateDto);

    List<PaymentResponseDto> findByUserId(Long userId);

    List<PaymentResponseDto> findByOrderId(Long orderId);

    List<PaymentResponseDto> findByStatus(PaymentStatus status);

    TotalSum getTotalSum(Instant start, Instant end, Long userId);

    TotalSum getTotalSumAdmin(Instant start, Instant end);
}
