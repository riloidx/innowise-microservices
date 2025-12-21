package com.innowise.orderservice.service;

import com.innowise.orderservice.dto.request.OrderCreateDto;
import com.innowise.orderservice.dto.request.OrderUpdateDto;
import com.innowise.orderservice.dto.response.OrderFullResponseDto;
import com.innowise.orderservice.dto.response.OrderResponseDto;
import com.innowise.orderservice.entity.Order;
import com.innowise.orderservice.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface OrderService {

    OrderFullResponseDto create(OrderCreateDto orderCreateDto);

    Page<OrderFullResponseDto> findAll(Pageable pageable,
                                       OrderStatus orderStatus,
                                       Boolean deleted,
                                       LocalDate createdAfter,
                                       LocalDate createdBefore);

    Order findById(long id);

    OrderFullResponseDto findDtoById(long id);

    List<OrderFullResponseDto> findByUserId(long userId);

    OrderFullResponseDto update(long id, OrderUpdateDto order);

    void updateStatusFromPayment(long orderId, String paymentStatus);

    OrderResponseDto delete(long id);
}
