package com.riloidx.orderservice.kafka.consumer;

import com.riloidx.orderservice.kafka.event.PaymentEvent;
import com.riloidx.orderservice.service.OrderService;
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
