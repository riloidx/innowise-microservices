package com.innowise.orderservice.mapper;

import com.innowise.orderservice.dto.request.ItemCreateDto;
import com.innowise.orderservice.dto.request.ItemUpdateDto;
import com.innowise.orderservice.dto.response.ItemResponseDto;
import com.innowise.orderservice.entity.Item;
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
