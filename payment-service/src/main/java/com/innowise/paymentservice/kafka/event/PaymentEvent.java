package com.innowise.paymentservice.kafka.event;

public record PaymentEvent(
        Long orderId,
        String status
) {}
