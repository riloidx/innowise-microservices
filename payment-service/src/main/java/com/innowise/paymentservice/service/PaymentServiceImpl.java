package com.innowise.paymentservice.service;

import com.innowise.paymentservice.entity.Payment;
import com.innowise.paymentservice.entity.PaymentStatus;
import com.innowise.paymentservice.repository.PaymentRepository;
import com.innowise.paymentservice.specification.PaymentSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements  PaymentService {

    private final PaymentRepository paymentRepo;

    @Override
    public Payment create(Payment payment) {
        return paymentRepo.save(payment);
    }

    @Override
    public Page<Payment> findAll(Long orderId, Long userId, PaymentStatus paymentStatus, Pageable pageable) {
        Specification<Payment> spec = prepareSpecification(orderId, userId, paymentStatus);

        Page<Payment> payments = paymentRepo.findAll(spec, pageable);

        return payments;
    }

    @Override
    public BigDecimal getTotalSum(Instant start, Instant end, Long userId) {
        return paymentRepo.sumPayments(start, end, userId);
    }

    private Specification<Payment> prepareSpecification(Long orderId,
                                                        Long userId,
                                                        PaymentStatus paymentStatus) {
        Specification<Payment> spec = Specification.unrestricted();

        if (orderId != null) {
            spec = spec.and(PaymentSpecification.hasOrderId(orderId));
        }

        if (userId != null) {
            spec = spec.and(PaymentSpecification.hasUserId(userId));
        }

        if (paymentStatus != null) {
            spec = spec.and(PaymentSpecification.hasStatus(paymentStatus));
        }

        return spec;
    }
}
