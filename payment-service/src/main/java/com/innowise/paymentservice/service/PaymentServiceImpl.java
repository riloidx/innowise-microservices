package com.innowise.paymentservice.service;

import com.innowise.paymentservice.dto.request.PaymentCreateDto;
import com.innowise.paymentservice.dto.response.PaymentResponseDto;
import com.innowise.paymentservice.entity.Payment;
import com.innowise.paymentservice.entity.PaymentStatus;
import com.innowise.paymentservice.exception.ExternalServiceException;
import com.innowise.paymentservice.integration.RandomNumberClient;
import com.innowise.paymentservice.mapper.PaymentMapper;
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
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepo;
    private final PaymentMapper mapper;
    private final RandomNumberClient randomNumberClient;

    @Override
    public PaymentResponseDto create(PaymentCreateDto paymentCreateDto) {
        Payment payment = preparePayment(paymentCreateDto);

        return mapper.toDto(paymentRepo.save(payment));
    }

    @Override
    public Page<PaymentResponseDto> findAll(Long orderId, Long userId, PaymentStatus paymentStatus, Pageable pageable) {
        Specification<Payment> spec = prepareSpecification(orderId, userId, paymentStatus);

        Page<Payment> payments = paymentRepo.findAll(spec, pageable);

        return mapper.toDto(payments);
    }

    @Override
    public BigDecimal getTotalSum(Instant start, Instant end, Long userId) {
        return paymentRepo.sumPayments(start, end, userId);
    }

    private Payment preparePayment(PaymentCreateDto paymentCreateDto) {
        Payment payment = mapper.toEntity(paymentCreateDto);

        payment.setStatus(getPaymentStatus());

        return payment;
    }

    private PaymentStatus getPaymentStatus() {
        var response = randomNumberClient.getRandomNumber();

        if (response == null) {
            throw new ExternalServiceException("External service return null");
        }

        int randomNumber = response.getFirst().random();

        return randomNumber % 2 == 0 ? PaymentStatus.SUCCESS : PaymentStatus.FAILED;
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
