package com.riloidx.userservice.service;

import com.riloidx.userservice.dto.request.PaymentCardCreateDto;
import com.riloidx.userservice.dto.request.PaymentCardUpdateDto;
import com.riloidx.userservice.dto.response.PaymentCardResponseDto;
import com.riloidx.userservice.entity.PaymentCard;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface PaymentCardService {

    // Commands

    PaymentCardResponseDto create(PaymentCardCreateDto paymentCardCreateDto);

    PaymentCardResponseDto update(long id, PaymentCardUpdateDto paymentCardUpdateDto);

    void delete(long id);

    PaymentCardResponseDto changeStatus(long id, boolean status);

    // Entity queries

    PaymentCard findById(long id);

    // DTO queries

    PaymentCardResponseDto findDtoById(long id);

    Page<PaymentCardResponseDto> findAll(Boolean active,
                                         LocalDate expiresAfter,
                                         LocalDate expiresBefore,
                                         Pageable pageable);

    List<PaymentCardResponseDto> findAllByUserId(long userId);

    Long getUserIdByCardId(long cardId);

}
