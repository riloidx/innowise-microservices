package com.riloidx.orderservice.mapper;

import com.riloidx.orderservice.dto.request.OrderItemDto;
import com.riloidx.orderservice.dto.response.OrderItemResponseDto;
import com.riloidx.orderservice.entity.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrderItemMapper {

    OrderItem toEntity(OrderItemDto dto);

    @Mapping(source = "item.id", target = "itemId")
    @Mapping(source = "item.name", target = "name")
    @Mapping(source = "item.price", target = "price")
    @Mapping(source = "id", target = "id")
    OrderItemResponseDto toDto(OrderItem orderItem);
}
