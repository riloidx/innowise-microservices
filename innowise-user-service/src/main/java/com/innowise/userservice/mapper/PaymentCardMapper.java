package com.innowise.userservice.mapper;

import com.innowise.userservice.dto.request.PaymentCardCreateDto;
import com.innowise.userservice.dto.request.PaymentCardUpdateDto;
import com.innowise.userservice.dto.response.PaymentCardResponseDto;
import com.innowise.userservice.entity.PaymentCard;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.springframework.data.domain.Page;

import java.util.List;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface PaymentCardMapper {

    PaymentCard toEntity(PaymentCardCreateDto paymentCardCreateDto);

    PaymentCardResponseDto toDto(PaymentCard paymentCard);

    List<PaymentCardResponseDto> toDto(List<PaymentCard> paymentCards);

    default Page<PaymentCardResponseDto> toDto(Page<PaymentCard> cards) {
        return cards.map(this::toDto);
    }

    void updateEntityFromDto(PaymentCardUpdateDto paymentCardUpdateDto,
                             @MappingTarget PaymentCard paymentCard);
}
