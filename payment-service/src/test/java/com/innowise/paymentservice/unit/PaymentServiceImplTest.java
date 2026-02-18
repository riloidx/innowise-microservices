package com.innowise.paymentservice.unit;

import com.innowise.paymentservice.dto.request.PaymentCreateDto;
import com.innowise.paymentservice.entity.Payment;
import com.innowise.paymentservice.entity.PaymentStatus;
import com.innowise.paymentservice.exception.ExternalServiceException;
import com.innowise.paymentservice.integration.RandomNumberClient;
import com.innowise.paymentservice.integration.RandomNumberResponseDto;
import com.innowise.paymentservice.kafka.event.PaymentEvent;
import com.innowise.paymentservice.mapper.PaymentMapper;
import com.innowise.paymentservice.repository.PaymentRepository;
import com.innowise.paymentservice.service.PaymentServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

    @Mock
    private PaymentRepository paymentRepo;
    @Mock
    private PaymentMapper mapper;
    @Mock
    private RandomNumberClient randomNumberClient;
    @Mock
    private KafkaTemplate<String, PaymentEvent> kafkaTemplate;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    record RandomResponse(int random) {
    }

    @Test
    void createShouldSetSuccessStatusWhenNumberIsEven() {
        PaymentCreateDto dto = new PaymentCreateDto(1L, 100L, BigDecimal.TEN);
        Payment payment = new Payment();
        payment.setOrderId(100L);

        RandomNumberResponseDto mockDto = new RandomNumberResponseDto("success", 1, 10, 2);
        when(randomNumberClient.getRandomNumber()).thenReturn(List.of(mockDto));

        when(mapper.toEntity(dto)).thenReturn(payment);
        when(paymentRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        paymentService.create(dto);

        assertEquals(PaymentStatus.SUCCESS, payment.getStatus());
        verify(kafkaTemplate).send(eq("payment-events"), any(PaymentEvent.class));
    }

    @Test
    void createShouldSetFailedStatusWhenNumberIsOdd() {
        PaymentCreateDto dto = new PaymentCreateDto(1L, 100L, BigDecimal.TEN);
        Payment payment = new Payment();

        RandomNumberResponseDto mockDto = new RandomNumberResponseDto("success", 1, 10, 3);
        when(randomNumberClient.getRandomNumber()).thenReturn(List.of(mockDto));

        when(mapper.toEntity(dto)).thenReturn(payment);
        when(paymentRepo.save(any())).thenReturn(payment);

        paymentService.create(dto);

        assertEquals(PaymentStatus.FAILED, payment.getStatus());
    }

    @Test
    void createShouldThrowExternalServiceExceptionWhenClientReturnsNull() {
        when(randomNumberClient.getRandomNumber()).thenReturn(null);

        assertThrows(ExternalServiceException.class,
                () -> paymentService.create(new PaymentCreateDto(1L, 1L, BigDecimal.ONE)));
    }
}
