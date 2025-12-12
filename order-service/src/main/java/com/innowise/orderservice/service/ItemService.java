package com.innowise.orderservice.service;

import com.innowise.orderservice.dto.request.ItemCreateDto;
import com.innowise.orderservice.dto.request.ItemUpdateDto;
import com.innowise.orderservice.dto.response.ItemResponseDto;
import com.innowise.orderservice.entity.Item;

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
