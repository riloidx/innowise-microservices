package com.riloidx.orderservice.mapper;

import com.riloidx.orderservice.dto.request.OrderCreateDto;
import com.riloidx.orderservice.dto.request.OrderUpdateDto;
import com.riloidx.orderservice.dto.response.OrderFullResponseDto;
import com.riloidx.orderservice.dto.response.OrderResponseDto;
import com.riloidx.orderservice.dto.response.UserResponseDto;
import com.riloidx.orderservice.entity.Order;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", uses = {OrderItemMapper.class})
public interface  OrderMapper {
    @Mapping(target = "orderItems", ignore = true)
    Order toEntity(OrderCreateDto orderCreateDto);

    OrderResponseDto toDto(Order order);

    List<OrderResponseDto> toDto(List<Order> orders);

    @Mapping(target = "id", source = "order.id")
    @Mapping(target = "status", source = "order.status")
    @Mapping(target = "deleted", source = "order.deleted")
    @Mapping(target = "totalPrice", source = "order.totalPrice")
    @Mapping(target = "orderItems", source = "order.orderItems")
    @Mapping(target = "user", source = "userDto")
    OrderFullResponseDto toFullDto(Order order, UserResponseDto userDto);


    @Mapping(target = "orderItems", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDto(OrderUpdateDto orderUpdateDto,
                             @MappingTarget Order order);
}
