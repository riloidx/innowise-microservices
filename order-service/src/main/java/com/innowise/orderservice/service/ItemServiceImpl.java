package com.innowise.orderservice.service;

import com.innowise.orderservice.dto.request.ItemCreateDto;
import com.innowise.orderservice.dto.request.ItemUpdateDto;
import com.innowise.orderservice.dto.response.ItemResponseDto;
import com.innowise.orderservice.entity.Item;
import com.innowise.orderservice.exception.ItemAlreadyExistsException;
import com.innowise.orderservice.exception.ItemNotFoundException;
import com.innowise.orderservice.mapper.ItemMapper;
import com.innowise.orderservice.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepo;
    private final ItemMapper itemMapper;

    @Override
    @Transactional
    public ItemResponseDto create(ItemCreateDto itemCreateDto) {
        validateNameIsUnique(itemCreateDto.name());

        Item item = itemMapper.toEntity(itemCreateDto);

        return itemMapper.toDto(itemRepo.save(item));
    }

    @Override
    public List<ItemResponseDto> findAll() {

        List<Item> items = itemRepo.findAll();

        return itemMapper.toDto(items);
    }

    @Override
    public Item findById(long id) {
        return itemRepo.findById(id).
                orElseThrow(() -> new ItemNotFoundException("id", String.valueOf(id)));
    }

    @Override
    public ItemResponseDto findDtoById(long id) {
        return itemMapper.toDto(findById(id));
    }

    @Override
    public ItemResponseDto findByName(String name) {
        Item item = itemRepo.findByName(name).
                orElseThrow(() -> new ItemNotFoundException("name", name));

        return itemMapper.toDto(item);
    }

    @Override
    @Transactional
    public ItemResponseDto update(long id, ItemUpdateDto itemUpdateDto) {
        validateNameIsUnique(itemUpdateDto.name());

        Item curItem = findById(id);

        itemMapper.updateEntityFromDto(itemUpdateDto, curItem);

        Item savedItem = itemRepo.save(curItem);

        return itemMapper.toDto(savedItem);
    }

    @Override
    @Transactional
    public void delete(long id) {
        findById(id);

        itemRepo.deleteById(id);
    }

    @Override
    public BigDecimal calculateTotalPriceOfAllItems() {
        List<Item> allItems = itemRepo.findAll();

        BigDecimal total = allItems.stream()
                .map(Item::getPrice)
                .reduce(
                        BigDecimal.ZERO,
                        BigDecimal::add
                );

        return total.setScale(2, RoundingMode.HALF_UP);
    }

    private void validateNameIsUnique(String name) {
        if (itemRepo.findByName(name).isPresent()) {
            throw new ItemAlreadyExistsException("name", name);
        }
    }
}
