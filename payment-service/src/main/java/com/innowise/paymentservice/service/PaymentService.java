package com.innowise.paymentservice.service;

import com.innowise.paymentservice.dto.request.PaymentCreateDto;
import com.innowise.paymentservice.dto.response.PaymentResponseDto;
import com.innowise.paymentservice.entity.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.Instant;

public interface PaymentService {
    PaymentResponseDto create(PaymentCreateDto paymentCreateDto);

    Page<PaymentResponseDto> findAll(Long orderId, Long userId, PaymentStatus paymentStatus, Pageable pageable);

    BigDecimal getTotalSum(Instant start, Instant end, Long userId);
}
