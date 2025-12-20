package com.innowise.paymentservice.service;

import com.innowise.paymentservice.entity.Payment;
import com.innowise.paymentservice.entity.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.Instant;

public interface PaymentService {
    Payment create(Payment payment);

    Page<Payment> findAll(Long orderId, Long userId, PaymentStatus paymentStatus, Pageable pageable);

    BigDecimal getTotalSum(Instant start, Instant end, Long userId);
}
