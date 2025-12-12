package com.innowise.userservice.specification;

import com.innowise.userservice.entity.PaymentCard;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

public class PaymentCardSpecification {

    public static Specification<PaymentCard> isActive(Boolean active) {
        return (root, query, cb) ->
                cb.equal(root.get("active"), active);
    }

    public static Specification<PaymentCard> expiresAfter(LocalDate date) {
        return (root, query, cb) ->
                cb.greaterThan(root.get("expirationDate"), date);
    }

    public static Specification<PaymentCard> expiresBefore(LocalDate date) {
        return (root, query, cb) ->
                cb.lessThan(root.get("expirationDate"), date);
    }
}
