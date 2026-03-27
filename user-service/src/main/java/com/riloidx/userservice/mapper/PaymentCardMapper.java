package com.riloidx.userservice.mapper;

import com.riloidx.userservice.dto.request.PaymentCardCreateDto;
import com.riloidx.userservice.dto.request.PaymentCardUpdateDto;
import com.riloidx.userservice.dto.response.PaymentCardResponseDto;
import com.riloidx.userservice.entity.PaymentCard;
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
