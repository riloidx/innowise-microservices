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
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;


@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepo;
    private final PaymentMapper mapper;
    private final RandomNumberClient randomNumberClient;
    private final KafkaTemplate<String, PaymentEvent> kafkaTemplate;

    @Override
    public PaymentResponseDto create(PaymentCreateDto paymentCreateDto) {
        log.info("Creating payment for order ID: {}, user ID: {}", paymentCreateDto.getOrderId(), paymentCreateDto.getUserId());
        
        Payment payment = preparePayment(paymentCreateDto);
        Payment savedPayment = paymentRepo.save(payment);
        log.info("Payment created with ID: {}, status: {}", savedPayment.getId(), savedPayment.getStatus());

        PaymentEvent event = new PaymentEvent(
                savedPayment.getOrderId(),
                savedPayment.getStatus().name()
        );
        kafkaTemplate.send("payment-events", event);
        log.debug("Payment event sent to Kafka for order ID: {}", savedPayment.getOrderId());

        return mapper.toDto(savedPayment);
    }

    @Override
    public List<PaymentResponseDto> findByUserId(Long userId) {
        log.debug("Finding payments for user ID: {}", userId);
        List<PaymentResponseDto> payments = mapper.toDto(paymentRepo.findByUserId(userId));
        log.debug("Found {} payments for user ID: {}", payments.size(), userId);
        return payments;
    }

    @Override
    public List<PaymentResponseDto> findByOrderId(Long orderId) {
        log.debug("Finding payments for order ID: {}", orderId);
        List<PaymentResponseDto> payments = mapper.toDto(paymentRepo.findByOrderId(orderId));
        log.debug("Found {} payments for order ID: {}", payments.size(), orderId);
        return payments;
    }

    @Override
    public List<PaymentResponseDto> findByStatus(PaymentStatus status) {
        log.debug("Finding payments with status: {}", status);
        List<PaymentResponseDto> payments = mapper.toDto(paymentRepo.findByStatus(status));
        log.debug("Found {} payments with status: {}", payments.size(), status);
        return payments;
    }

    @Override
    public TotalSum getTotalSum(Instant start, Instant end, Long userId) {
        log.debug("Calculating total sum for user ID: {} between {} and {}", userId, start, end);
        TotalSum result = paymentRepo.sumAmountByUserIdAndDateRange(userId, start, end);
        log.debug("Total sum for user ID {}: {}", userId, result);
        return result;
    }

    @Override
    public TotalSum getTotalSumAdmin(Instant start, Instant end) {
        log.debug("Calculating total sum for all users between {} and {}", start, end);
        TotalSum result = paymentRepo.sumAmountForDateRange(start, end);
        log.debug("Total sum for all users: {}", result);
        return result;
    }

    private Payment preparePayment(PaymentCreateDto paymentCreateDto) {
        Payment payment = mapper.toEntity(paymentCreateDto);

        payment.setStatus(getPaymentStatus());

        return payment;
    }

    private PaymentStatus getPaymentStatus() {
        try {
            log.debug("Fetching random number from external service");
            var response = randomNumberClient.getRandomNumber();

            if (response == null || response.isEmpty()) {
                log.error("External service returned null or empty response");
                throw new ExternalServiceException("External service return null");
            }

            int randomNumber = response.getFirst().random();
            PaymentStatus status = randomNumber % 2 == 0 ? PaymentStatus.SUCCESS : PaymentStatus.FAILED;
            log.debug("Payment status determined: {} (random number: {})", status, randomNumber);
            return status;

        } catch (FeignException e) {
            log.error("External service communication failed: {}", e.getMessage());
            throw new ExternalServiceException("External service communication failed: " + e.getMessage());
        }
    }
}
