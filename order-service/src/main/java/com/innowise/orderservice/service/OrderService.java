package com.innowise.orderservice.service;

import com.innowise.orderservice.dto.request.OrderCreateDto;
import com.innowise.orderservice.dto.request.OrderUpdateDto;
import com.innowise.orderservice.dto.response.OrderFullResponseDto;
import com.innowise.orderservice.dto.response.OrderResponseDto;
import com.innowise.orderservice.entity.Order;
import com.innowise.orderservice.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.List;

public interface OrderService {

    Mono<OrderFullResponseDto> create(OrderCreateDto orderCreateDto);

    Mono<Page<OrderFullResponseDto>> findAll(Pageable pageable,
                                             OrderStatus orderStatus,
                                             Boolean deleted,
                                             LocalDate createdAfter,
                                             LocalDate createdBefore);

    Order findById(long id);

    Mono<OrderFullResponseDto> findDtoById(long id);

    Mono<List<OrderFullResponseDto>> findByUserId(long userId);

    Mono<OrderFullResponseDto> update(long id, OrderUpdateDto order);

    Mono<OrderResponseDto> delete(long id);
}
