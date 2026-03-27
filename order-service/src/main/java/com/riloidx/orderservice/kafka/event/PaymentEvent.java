package com.riloidx.orderservice.kafka.event;

public record PaymentEvent(
        Long orderId,
        String status
) {}
