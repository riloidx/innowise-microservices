package com.innowise.paymentservice.service;

import com.innowise.paymentservice.dto.request.PaymentCreateDto;
import com.innowise.paymentservice.dto.response.PaymentResponseDto;
import com.innowise.paymentservice.dto.response.TotalSum;
import com.innowise.paymentservice.entity.Payment;
import com.innowise.paymentservice.entity.PaymentStatus;
import com.innowise.paymentservice.exception.ExternalServiceException;
import com.innowise.paymentservice.integration.RandomNumberClient;
import com.innowise.paymentservice.kafka.event.PaymentEvent;
import com.innowise.paymentservice.mapper.PaymentMapper;
import com.innowise.paymentservice.repository.PaymentRepository;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;


@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepo;
    private final PaymentMapper mapper;
    private final RandomNumberClient randomNumberClient;
    private final KafkaTemplate<String, PaymentEvent> kafkaTemplate;

    @Override
    public PaymentResponseDto create(PaymentCreateDto paymentCreateDto) {
        Payment payment = preparePayment(paymentCreateDto);
        Payment savedPayment = paymentRepo.save(payment);

        PaymentEvent event = new PaymentEvent(
                savedPayment.getOrderId(),
                savedPayment.getStatus().name()
        );
        kafkaTemplate.send("payment-events", event);

        return mapper.toDto(savedPayment);
    }

    @Override
    public List<PaymentResponseDto> findByUserId(Long userId) {
        return mapper.toDto(paymentRepo.findByUserId(userId));
    }

    @Override
    public List<PaymentResponseDto> findByOrderId(Long orderId) {
        return mapper.toDto(paymentRepo.findByOrderId(orderId));
    }

    @Override
    public List<PaymentResponseDto> findByStatus(PaymentStatus status) {
        return mapper.toDto(paymentRepo.findByStatus(status));
    }

    @Override
    public TotalSum getTotalSum(Instant start, Instant end, Long userId) {
        return paymentRepo.sumAmountByUserIdAndDateRange(userId, start, end);
    }

    @Override
    public TotalSum getTotalSumAdmin(Instant start, Instant end) {
        return paymentRepo.sumAmountForDateRange(start, end);
    }

    private Payment preparePayment(PaymentCreateDto paymentCreateDto) {
        Payment payment = mapper.toEntity(paymentCreateDto);

        payment.setStatus(getPaymentStatus());

        return payment;
    }

    private PaymentStatus getPaymentStatus() {
        try {
            var response = randomNumberClient.getRandomNumber();

            if (response == null || response.isEmpty()) {
                throw new ExternalServiceException("External service return null");
            }

            int randomNumber = response.getFirst().random();
            return randomNumber % 2 == 0 ? PaymentStatus.SUCCESS : PaymentStatus.FAILED;

        } catch (FeignException e) {
            throw new ExternalServiceException("External service communication failed: " + e.getMessage());
        }
    }
}
