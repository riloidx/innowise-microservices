package com.innowise.orderservice.specification;

import com.innowise.orderservice.entity.Order;
import com.innowise.orderservice.enums.OrderStatus;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;

public class OrderSpecification {

    public static Specification<Order> hasStatus(OrderStatus status) {
        return (root, query, cb) ->
                cb.equal(root.get("status"), status);
    }

    public static Specification<Order> isDeleted(boolean deleted) {
        return (root, query, cb) ->
                cb.equal(root.get("deleted"), deleted);
    }

    public static Specification<Order> createdAfter(Instant fromDate) {
        return (root, query, cb) ->
                cb.greaterThanOrEqualTo(root.get("createdAt"), fromDate);
    }

    public static Specification<Order> createdBefore(Instant toDate) {
        return (root, query, cb) ->
                cb.lessThanOrEqualTo(root.get("createdAt"), toDate);
    }
}