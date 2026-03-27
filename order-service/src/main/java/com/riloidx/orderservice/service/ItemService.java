package com.riloidx.orderservice.service;

import com.riloidx.orderservice.dto.request.ItemCreateDto;
import com.riloidx.orderservice.dto.request.ItemUpdateDto;
import com.riloidx.orderservice.dto.response.ItemResponseDto;
import com.riloidx.orderservice.entity.Item;

import java.math.BigDecimal;
import java.util.List;

public interface ItemService {

    ItemResponseDto create(ItemCreateDto itemCreateDto);

    List<ItemResponseDto> findAll();

    Item findById(long id);

    ItemResponseDto findDtoById(long id);

    ItemResponseDto findByName(String name);

    ItemResponseDto update(long id, ItemUpdateDto itemUpdateDto);

    void delete(long id);

    BigDecimal calculateTotalPriceOfAllItems();
}
