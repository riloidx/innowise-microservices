package com.riloidx.paymentservice.kafka.event;

public record PaymentEvent(
        Long orderId,
        String status
) {}
