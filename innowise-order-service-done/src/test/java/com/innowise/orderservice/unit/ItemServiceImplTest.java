package com.innowise.orderservice.unit;

import com.innowise.orderservice.dto.request.ItemCreateDto;
import com.innowise.orderservice.dto.request.ItemUpdateDto;
import com.innowise.orderservice.dto.response.ItemResponseDto;
import com.innowise.orderservice.entity.Item;
import com.innowise.orderservice.exception.ItemAlreadyExistsException;
import com.innowise.orderservice.exception.ItemNotFoundException;
import com.innowise.orderservice.mapper.ItemMapper;
import com.innowise.orderservice.repository.ItemRepository;
import com.innowise.orderservice.service.ItemServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemServiceImplTest {

    @Mock
    private ItemRepository itemRepo;

    @Mock
    private ItemMapper itemMapper;

    @InjectMocks
    private ItemServiceImpl service;

    @Test
    void createShouldSaveAndReturnDtoWhenNameUnique() {
        ItemCreateDto createDto = new ItemCreateDto("Phone", BigDecimal.valueOf(100.00));
        Item item = new Item();
        item.setId(1L);
        item.setName("Phone");
        item.setPrice(BigDecimal.valueOf(100.00));

        ItemResponseDto responseDto = new ItemResponseDto(1L, "Phone", BigDecimal.valueOf(100.00));

        when(itemRepo.findByName("Phone")).thenReturn(Optional.empty());
        when(itemMapper.toEntity(createDto)).thenReturn(item);
        when(itemRepo.save(item)).thenReturn(item);
        when(itemMapper.toDto(item)).thenReturn(responseDto);

        ItemResponseDto result = service.create(createDto);

        assertEquals(responseDto, result);
        verify(itemRepo).findByName("Phone");
        verify(itemRepo).save(item);
        verify(itemMapper).toEntity(createDto);
        verify(itemMapper).toDto(item);
    }

    @Test
    void createShouldThrowWhenNameExists() {
        ItemCreateDto createDto = new ItemCreateDto("Phone", BigDecimal.valueOf(100.00));
        Item existingItem = new Item();
        existingItem.setName("Phone");

        when(itemRepo.findByName("Phone")).thenReturn(Optional.of(existingItem));

        assertThrows(ItemAlreadyExistsException.class, () -> service.create(createDto));
        verify(itemRepo).findByName("Phone");
        verifyNoMoreInteractions(itemRepo, itemMapper);
    }

    @Test
    void findAllShouldReturnListOfItems() {
        Item item = new Item();
        ItemResponseDto responseDto = new ItemResponseDto(1L, "Phone", BigDecimal.valueOf(100.00));

        when(itemRepo.findAll()).thenReturn(List.of(item));
        when(itemMapper.toDto(List.of(item))).thenReturn(List.of(responseDto));

        List<ItemResponseDto> result = service.findAll();

        assertEquals(1, result.size());
        assertEquals(responseDto, result.get(0));
        verify(itemRepo).findAll();
        verify(itemMapper).toDto(List.of(item));
    }

    @Test
    void findByIdShouldReturnItemWhenExists() {
        long id = 1L;
        Item item = new Item();
        item.setId(id);

        when(itemRepo.findById(id)).thenReturn(Optional.of(item));

        Item result = service.findById(id);

        assertEquals(item, result);
        verify(itemRepo).findById(id);
    }

    @Test
    void findByIdShouldThrowWhenNotFound() {
        long id = 1L;
        when(itemRepo.findById(id)).thenReturn(Optional.empty());

        assertThrows(ItemNotFoundException.class, () -> service.findById(id));
        verify(itemRepo).findById(id);
    }

    @Test
    void findDtoByIdShouldReturnDtoWhenExists() {
        long id = 1L;
        Item item = new Item();
        ItemResponseDto responseDto = new ItemResponseDto(id, "Phone", BigDecimal.valueOf(100.00));

        when(itemRepo.findById(id)).thenReturn(Optional.of(item));
        when(itemMapper.toDto(item)).thenReturn(responseDto);

        ItemResponseDto result = service.findDtoById(id);

        assertEquals(responseDto, result);
        verify(itemRepo).findById(id);
        verify(itemMapper).toDto(item);
    }

    @Test
    void findByNameShouldReturnDtoWhenExists() {
        String name = "Phone";
        Item item = new Item();
        ItemResponseDto responseDto = new ItemResponseDto(1L, name, BigDecimal.valueOf(100.00));

        when(itemRepo.findByName(name)).thenReturn(Optional.of(item));
        when(itemMapper.toDto(item)).thenReturn(responseDto);

        ItemResponseDto result = service.findByName(name);

        assertEquals(responseDto, result);
        verify(itemRepo).findByName(name);
        verify(itemMapper).toDto(item);
    }

    @Test
    void findByNameShouldThrowWhenNotFound() {
        String name = "Phone";
        when(itemRepo.findByName(name)).thenReturn(Optional.empty());

        assertThrows(ItemNotFoundException.class, () -> service.findByName(name));
        verify(itemRepo).findByName(name);
    }

    @Test
    void updateShouldSaveAndReturnDtoWhenValid() {
        long id = 1L;
        ItemUpdateDto updateDto = new ItemUpdateDto("New Phone", BigDecimal.valueOf(150.00));
        Item currentItem = new Item();
        currentItem.setId(id);
        currentItem.setName("Old Phone");

        Item updatedItem = new Item();
        updatedItem.setId(id);
        updatedItem.setName("New Phone");

        ItemResponseDto responseDto = new ItemResponseDto(id, "New Phone", BigDecimal.valueOf(150.00));

        when(itemRepo.findByName("New Phone")).thenReturn(Optional.empty());
        when(itemRepo.findById(id)).thenReturn(Optional.of(currentItem));
        doNothing().when(itemMapper).updateEntityFromDto(updateDto, currentItem);
        when(itemRepo.save(currentItem)).thenReturn(updatedItem);
        when(itemMapper.toDto(updatedItem)).thenReturn(responseDto);

        ItemResponseDto result = service.update(id, updateDto);

        assertEquals(responseDto, result);
        verify(itemRepo).findByName("New Phone");
        verify(itemRepo).findById(id);
        verify(itemRepo).save(currentItem);
        verify(itemMapper).updateEntityFromDto(updateDto, currentItem);
    }

    @Test
    void updateShouldThrowWhenNameTaken() {
        long id = 1L;
        ItemUpdateDto updateDto = new ItemUpdateDto("Existing Phone", BigDecimal.valueOf(150.00));
        Item existingItem = new Item();

        when(itemRepo.findByName("Existing Phone")).thenReturn(Optional.of(existingItem));

        assertThrows(ItemAlreadyExistsException.class, () -> service.update(id, updateDto));
        verify(itemRepo).findByName("Existing Phone");
        verifyNoMoreInteractions(itemRepo, itemMapper);
    }

    @Test
    void deleteShouldCallRepoWhenItemExists() {
        long id = 1L;
        Item item = new Item();
        when(itemRepo.findById(id)).thenReturn(Optional.of(item));

        service.delete(id);

        verify(itemRepo).findById(id);
        verify(itemRepo).deleteById(id);
    }

    @Test
    void calculateTotalPriceOfAllItemsShouldReturnCorrectSum() {
        Item item1 = new Item();
        item1.setPrice(BigDecimal.valueOf(100.00));
        Item item2 = new Item();
        item2.setPrice(BigDecimal.valueOf(50.50));

        when(itemRepo.findAll()).thenReturn(List.of(item1, item2));

        BigDecimal result = service.calculateTotalPriceOfAllItems();

        assertEquals(new BigDecimal("150.50"), result);
        verify(itemRepo).findAll();
    }
}