package com.innowise.paymentservice.mapper;

import com.innowise.paymentservice.dto.request.PaymentCreateDto;
import com.innowise.paymentservice.dto.response.PaymentResponseDto;
import com.innowise.paymentservice.entity.Payment;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PaymentMapper {
    Payment toEntity(PaymentCreateDto paymentCreateDto);

    PaymentResponseDto toDto(Payment payment);

    List<PaymentResponseDto> toDto(List<Payment> payments);
}
