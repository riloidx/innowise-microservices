package com.innowise.orderservice.kafka.event;

public record PaymentEvent(
        Long orderId,
        String status
) {}
