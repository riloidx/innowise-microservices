package com.innowise.orderservice.controller;

import com.innowise.orderservice.dto.request.OrderCreateDto;
import com.innowise.orderservice.dto.request.OrderUpdateDto;
import com.innowise.orderservice.dto.response.OrderFullResponseDto;
import com.innowise.orderservice.dto.response.OrderResponseDto;
import com.innowise.orderservice.enums.OrderStatus;
import com.innowise.orderservice.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<OrderFullResponseDto> create(@RequestBody @Valid OrderCreateDto orderCreateDto) {
        return orderService.create(orderCreateDto);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public Mono<Page<OrderFullResponseDto>> findAll(
            Pageable pageable,
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) Boolean deleted,
            @RequestParam(required = false) LocalDate createdAfter,
            @RequestParam(required = false) LocalDate createdBefore
    ) {
        return orderService.findAll(
                pageable,
                status,
                deleted,
                createdAfter,
                createdBefore
        );
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public Mono<OrderFullResponseDto> findById(@PathVariable Long id) {
        return orderService.findDtoById(id);
    }

    @GetMapping("/user/{userId}")
    @ResponseStatus(HttpStatus.OK)
    public Mono<List<OrderFullResponseDto>> findByUserId(@PathVariable Long userId) {
        return orderService.findByUserId(userId);
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public Mono<OrderFullResponseDto> update(
            @PathVariable Long id,
            @RequestBody @Valid OrderUpdateDto orderUpdateDto
    ) {
        return orderService.update(id, orderUpdateDto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public Mono<OrderResponseDto> delete(@PathVariable Long id) {
        return orderService.delete(id);
    }
}