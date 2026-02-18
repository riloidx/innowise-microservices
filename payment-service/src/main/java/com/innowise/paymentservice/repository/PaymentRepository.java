package com.innowise.paymentservice.repository;

import com.innowise.paymentservice.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.Instant;

public interface PaymentRepository extends JpaRepository<Payment, Long>, JpaSpecificationExecutor<Payment> {
    @Query("SELECT SUM(p.paymentAmount) FROM Payment p " +
            "WHERE p.timestamp BETWEEN :start AND :end " +
            "AND (:userId IS NULL OR p.userId = :userId)")
    BigDecimal sumPayments(@Param("start") Instant start,
                           @Param("end") Instant end,
                           @Param("userId") Long userId);
}
