package com.riloidx.paymentservice.service;

import com.riloidx.paymentservice.dto.request.PaymentCreateDto;
import com.riloidx.paymentservice.dto.response.PaymentResponseDto;
import com.riloidx.paymentservice.dto.response.TotalSum;
import com.riloidx.paymentservice.entity.PaymentStatus;

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
