package com.riloidx.paymentservice.repository;

import com.riloidx.paymentservice.dto.response.TotalSum;
import com.riloidx.paymentservice.entity.Payment;
import com.riloidx.paymentservice.entity.PaymentStatus;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.Instant;
import java.util.List;

public interface PaymentRepository extends MongoRepository<Payment, String> {
    List<Payment> findByUserId(Long userId);

    List<Payment> findByOrderId(Long orderId);

    List<Payment> findByStatus(PaymentStatus status);

    @Aggregation(pipeline = {
            "{ $match: { 'user_id': ?0, 'timestamp': { $gte: ?1, $lte: ?2 }, 'status': 'SUCCESS' } }",
            "{ $group: { _id: null, total: { $sum: { $toDecimal: '$payment_amount' } } } }"
    })
    TotalSum sumAmountByUserIdAndDateRange(Long userId, Instant start, Instant end);

    @Aggregation(pipeline = {
            "{ $match: { 'timestamp': { $gte: ?0, $lte: ?1 }, 'status': 'SUCCESS' } }",
            "{ $group: { _id: null, total: { $sum: { $toDecimal: '$payment_amount' } } } }"
    })
    TotalSum sumAmountForDateRange(Instant start, Instant end);

}
