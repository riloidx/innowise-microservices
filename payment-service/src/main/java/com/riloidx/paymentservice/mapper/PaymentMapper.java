package com.riloidx.paymentservice.mapper;

import com.riloidx.paymentservice.dto.request.PaymentCreateDto;
import com.riloidx.paymentservice.dto.response.PaymentResponseDto;
import com.riloidx.paymentservice.entity.Payment;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PaymentMapper {
    Payment toEntity(PaymentCreateDto paymentCreateDto);

    PaymentResponseDto toDto(Payment payment);

    List<PaymentResponseDto> toDto(List<Payment> payments);
}
