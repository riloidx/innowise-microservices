package com.riloidx.orderservice.mapper;

import com.riloidx.orderservice.dto.request.ItemCreateDto;
import com.riloidx.orderservice.dto.request.ItemUpdateDto;
import com.riloidx.orderservice.dto.response.ItemResponseDto;
import com.riloidx.orderservice.entity.Item;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ItemMapper {
    Item toEntity(ItemCreateDto itemCreateDto);

    ItemResponseDto toDto(Item item);

    List<ItemResponseDto> toDto(List<Item> items);

    void updateEntityFromDto(ItemUpdateDto itemUpdateDto,
                             @MappingTarget Item item);
}
