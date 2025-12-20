package com.innowise.paymentservice.specification;

import com.innowise.paymentservice.entity.Payment;
import com.innowise.paymentservice.entity.PaymentStatus;
import org.springframework.data.jpa.domain.Specification;

public class PaymentSpecification {

    public static Specification<Payment> hasUserId(Long userId) {
        return (root, query, cb) ->
                cb.equal(root.get("userId"), userId);
    }

    public static Specification<Payment> hasOrderId(Long orderId) {
        return (root, query, cb) ->
                cb.equal(root.get("orderId"), orderId);
    }

    public static Specification<Payment> hasStatus(PaymentStatus status) {
        return (root, query, cb) ->
                cb.equal(root.get("status"), status);
    }
}
