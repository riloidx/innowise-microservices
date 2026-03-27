package com.riloidx.paymentservice.controller;

import com.riloidx.paymentservice.dto.request.PaymentCreateDto;
import com.riloidx.paymentservice.dto.response.PaymentResponseDto;
import com.riloidx.paymentservice.dto.response.TotalSum;
import com.riloidx.paymentservice.entity.PaymentStatus;
import com.riloidx.paymentservice.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    public ResponseEntity<PaymentResponseDto> create(@RequestBody @Valid PaymentCreateDto createDto) {
        PaymentResponseDto res = paymentService.create(createDto);

        return ResponseEntity.status(HttpStatus.CREATED).body(res);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<PaymentResponseDto>> getByUserId(@PathVariable Long userId) {
        List<PaymentResponseDto> res = paymentService.findByUserId(userId);

        return ResponseEntity.status(HttpStatus.OK).body(res);
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<List<PaymentResponseDto>> getByOrderId(@PathVariable Long orderId) {
        List<PaymentResponseDto> res = paymentService.findByOrderId(orderId);

        return ResponseEntity.status(HttpStatus.OK).body(res);
    }

    @GetMapping("/status")
    public ResponseEntity<List<PaymentResponseDto>> getByStatus(@RequestParam PaymentStatus status) {
        List<PaymentResponseDto> res = paymentService.findByStatus(status);

        return ResponseEntity.status(HttpStatus.OK).body(res);
    }

    @GetMapping("/total-sum")
    public ResponseEntity<TotalSum> getTotalSum(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant end,
            @RequestParam Long userId) {
        TotalSum res = paymentService.getTotalSum(start, end, userId);

        return ResponseEntity.status(HttpStatus.OK).body(res);
    }

    @GetMapping("/admin/total-sum")
    public ResponseEntity<TotalSum> getTotalSumAdmin(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant end) {
        TotalSum res = paymentService.getTotalSumAdmin(start, end);

        return ResponseEntity.status(HttpStatus.OK).body(res);
    }
}
