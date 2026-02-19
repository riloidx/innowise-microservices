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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepo;
    private final ItemMapper itemMapper;

    @Override
    @Transactional
    public ItemResponseDto create(ItemCreateDto itemCreateDto) {
        log.info("Creating new item with name: {}", itemCreateDto.name());
        
        validateNameIsUnique(itemCreateDto.name());

        Item item = itemMapper.toEntity(itemCreateDto);
        Item savedItem = itemRepo.save(item);
        
        log.info("Item created successfully with ID: {}", savedItem.getId());
        return itemMapper.toDto(savedItem);
    }

    @Override
    public List<ItemResponseDto> findAll() {
        log.debug("Finding all items");

        List<Item> items = itemRepo.findAll();
        log.debug("Found {} items", items.size());

        return itemMapper.toDto(items);
    }

    @Override
    public Item findById(long id) {
        log.debug("Finding item by ID: {}", id);
        return itemRepo.findById(id).
                orElseThrow(() -> {
                    log.warn("Item not found with ID: {}", id);
                    return new ItemNotFoundException("id", String.valueOf(id));
                });
    }

    @Override
    public ItemResponseDto findDtoById(long id) {
        return itemMapper.toDto(findById(id));
    }

    @Override
    public ItemResponseDto findByName(String name) {
        log.debug("Finding item by name: {}", name);
        
        Item item = itemRepo.findByName(name).
                orElseThrow(() -> {
                    log.warn("Item not found with name: {}", name);
                    return new ItemNotFoundException("name", name);
                });

        return itemMapper.toDto(item);
    }

    @Override
    @Transactional
    public ItemResponseDto update(long id, ItemUpdateDto itemUpdateDto) {
        log.info("Updating item with ID: {}", id);
        
        validateNameIsUnique(itemUpdateDto.name());

        Item curItem = findById(id);

        itemMapper.updateEntityFromDto(itemUpdateDto, curItem);

        Item savedItem = itemRepo.save(curItem);
        log.info("Item updated successfully with ID: {}", id);

        return itemMapper.toDto(savedItem);
    }

    @Override
    @Transactional
    public void delete(long id) {
        log.info("Deleting item with ID: {}", id);
        
        findById(id);

        itemRepo.deleteById(id);
        log.info("Item deleted successfully with ID: {}", id);
    }

    @Override
    public BigDecimal calculateTotalPriceOfAllItems() {
        log.debug("Calculating total price of all items");
        
        List<Item> allItems = itemRepo.findAll();

        BigDecimal total = allItems.stream()
                .map(Item::getPrice)
                .reduce(
                        BigDecimal.ZERO,
                        BigDecimal::add
                );

        BigDecimal result = total.setScale(2, RoundingMode.HALF_UP);
        log.debug("Total price of all items: {}", result);
        
        return result;
    }

    private void validateNameIsUnique(String name) {
        if (itemRepo.findByName(name).isPresent()) {
            log.warn("Item with name already exists: {}", name);
            throw new ItemAlreadyExistsException("name", name);
        }
    }
}
