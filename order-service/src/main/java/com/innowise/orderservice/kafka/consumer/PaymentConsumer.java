package com.innowise.orderservice.kafka.consumer;

import com.innowise.orderservice.kafka.event.PaymentEvent;
import com.innowise.orderservice.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentConsumer {

    private final OrderService orderService;

    @KafkaListener(topics = "payment-events", groupId = "order-group")
    public void handlePaymentEvent(PaymentEvent paymentEvent) {
        orderService.updateStatusFromPayment(paymentEvent.orderId(), paymentEvent.status());
    }
}
