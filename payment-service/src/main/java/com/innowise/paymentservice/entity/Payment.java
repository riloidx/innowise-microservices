package com.innowise.paymentservice.entity;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@Document(collection = "payments")
public class Payment {

    @Id
    private String id;

    @Field("order_id")
    @Indexed
    private Long orderId;

    @Field("user_id")
    @Indexed
    private Long userId;

    @Field("status")
    private PaymentStatus status;

    @CreatedDate
    @Field("timestamp")
    private Instant timestamp;

    @Field("payment_amount")
    private BigDecimal paymentAmount;
}