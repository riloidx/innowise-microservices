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
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderFullResponseDto> create(@RequestBody @Valid OrderCreateDto orderCreateDto) {
        OrderFullResponseDto response = orderService.create(orderCreateDto);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<Page<OrderFullResponseDto>> findAll(
            Pageable pageable,
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) Boolean deleted,
            @RequestParam(required = false) LocalDate createdAfter,
            @RequestParam(required = false) LocalDate createdBefore
    ) {
        Page<OrderFullResponseDto> response = orderService.findAll(pageable,
                status,
                deleted,
                createdAfter,
                createdBefore);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderFullResponseDto> findById(@PathVariable Long id) {
        OrderFullResponseDto response = orderService.findDtoById(id);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<OrderFullResponseDto>> findByUserId(@PathVariable Long userId) {
        List<OrderFullResponseDto> response = orderService.findByUserId(userId);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<OrderFullResponseDto> update(
            @PathVariable Long id,
            @RequestBody @Valid OrderUpdateDto orderUpdateDto
    ) {
        OrderFullResponseDto response = orderService.update(id, orderUpdateDto);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<OrderResponseDto> delete(@PathVariable Long id) {
        OrderResponseDto response = orderService.delete(id);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}