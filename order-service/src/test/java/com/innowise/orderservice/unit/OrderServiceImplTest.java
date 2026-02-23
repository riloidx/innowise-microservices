package com.innowise.orderservice.unit;

import com.innowise.orderservice.dto.request.OrderCreateDto;
import com.innowise.orderservice.dto.request.OrderItemDto;
import com.innowise.orderservice.dto.request.OrderUpdateDto;
import com.innowise.orderservice.dto.response.OrderFullResponseDto;
import com.innowise.orderservice.dto.response.OrderResponseDto;
import com.innowise.orderservice.dto.response.UserResponseDto;
import com.innowise.orderservice.entity.Item;
import com.innowise.orderservice.entity.Order;
import com.innowise.orderservice.enums.OrderStatus;
import com.innowise.orderservice.exception.OrderNotFoundException;
import com.innowise.orderservice.mapper.OrderMapper;
import com.innowise.orderservice.repository.OrderRepository;
import com.innowise.orderservice.service.ItemService;
import com.innowise.orderservice.service.OrderServiceImpl;
import com.innowise.orderservice.service.UserServiceClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepo;
    @Mock
    private ItemService itemService;
    @Mock
    private UserServiceClient userService;
    @Mock
    private OrderMapper orderMapper;

    @InjectMocks
    private OrderServiceImpl service;

    private final UserResponseDto mockUser = new UserResponseDto(1L, "John", "Doe", "test@email.com");

    @Test
    void createShouldCalculatePriceAndReturnFullDto() {
        OrderCreateDto createDto = new OrderCreateDto(1L, List.of(new OrderItemDto(10L, 2)));

        Item item = new Item();
        item.setId(10L);
        item.setPrice(BigDecimal.valueOf(100.00));

        Order orderEntity = new Order();
        orderEntity.setUserId(1L);
        orderEntity.setOrderItems(new ArrayList<>());

        Order savedOrder = new Order();
        savedOrder.setId(1L);
        savedOrder.setUserId(1L);
        savedOrder.setTotalPrice(BigDecimal.valueOf(200.00));

        OrderFullResponseDto expectedDto = new OrderFullResponseDto(1L, OrderStatus.PENDING, false, BigDecimal.valueOf(200.00), mockUser, List.of());

        when(orderMapper.toEntity(createDto)).thenReturn(orderEntity);
        when(itemService.findById(10L)).thenReturn(item);
        when(orderRepo.save(orderEntity)).thenReturn(savedOrder);
        when(userService.getUserById(1L)).thenReturn(mockUser);
        when(orderMapper.toFullDto(savedOrder, mockUser)).thenReturn(expectedDto);

        OrderFullResponseDto result = service.create(createDto);

        assertNotNull(result);
        assertEquals(expectedDto, result);
        assertEquals(new BigDecimal("200.00"), orderEntity.getTotalPrice());
        verify(userService).getUserById(1L);
    }

    @Test
    void findAllShouldReturnPageOfFullDtos() {
        Pageable pageable = Pageable.unpaged();
        Order order = new Order();
        order.setUserId(1L);
        Page<Order> page = new PageImpl<>(List.of(order));

        OrderFullResponseDto fullDto = new OrderFullResponseDto(1L, OrderStatus.PENDING, false, BigDecimal.TEN, mockUser, List.of());

        when(orderRepo.findAll(any(Specification.class), eq(pageable))).thenReturn(page);
        when(userService.getUserById(1L)).thenReturn(mockUser);
        when(orderMapper.toFullDto(order, mockUser)).thenReturn(fullDto);

        Page<OrderFullResponseDto> resultPage = service.findAll(pageable, null, null, null, null);

        assertEquals(1, resultPage.getTotalElements());
        assertEquals(fullDto, resultPage.getContent().get(0));
        verify(userService).getUserById(1L);
    }

    @Test
    void findByUserIdShouldReturnListWithUserInfo() {
        Order order = new Order();
        order.setUserId(1L);
        OrderFullResponseDto fullDto = new OrderFullResponseDto(1L, OrderStatus.PENDING, false, BigDecimal.TEN, mockUser, List.of());

        when(orderRepo.findAllByUserId(1L)).thenReturn(List.of(order));
        when(userService.getUserById(1L)).thenReturn(mockUser);
        when(orderMapper.toFullDto(order, mockUser)).thenReturn(fullDto);

        List<OrderFullResponseDto> result = service.findByUserId(1L);

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        verify(userService).getUserById(1L);
    }

    @Test
    void updateShouldProcessItemsAndSaveWhenValid() {
        long orderId = 1L;
        OrderUpdateDto updateDto = new OrderUpdateDto(OrderStatus.CONFIRMED, List.of(new OrderItemDto(10L, 1)));

        Order existingOrder = new Order();
        existingOrder.setId(orderId);
        existingOrder.setUserId(1L);
        existingOrder.setDeleted(false);
        existingOrder.setOrderItems(new ArrayList<>());

        Item item = new Item();
        item.setId(10L);
        item.setPrice(BigDecimal.TEN);

        OrderFullResponseDto expectedDto = new OrderFullResponseDto(orderId, OrderStatus.CONFIRMED, false, BigDecimal.TEN, mockUser, List.of());

        when(orderRepo.findById(orderId)).thenReturn(Optional.of(existingOrder));
        when(itemService.findById(10L)).thenReturn(item);
        when(orderRepo.save(any(Order.class))).thenReturn(existingOrder);
        when(userService.getUserById(1L)).thenReturn(mockUser);
        when(orderMapper.toFullDto(existingOrder, mockUser)).thenReturn(expectedDto);

        OrderFullResponseDto result = service.update(orderId, updateDto);

        assertEquals(expectedDto, result);
        verify(orderMapper).updateEntityFromDto(updateDto, existingOrder);
        verify(orderRepo).save(any(Order.class));
    }

    @Test
    void deleteShouldSetDeletedFlagAndReturnDto() {
        long orderId = 1L;
        Order order = new Order();
        order.setId(orderId);
        order.setDeleted(false);

        OrderResponseDto expectedDto = new OrderResponseDto(orderId, OrderStatus.PENDING, true, BigDecimal.ZERO, List.of());

        when(orderRepo.findById(orderId)).thenReturn(Optional.of(order));
        when(orderRepo.save(order)).thenReturn(order);
        when(orderMapper.toDto(order)).thenReturn(expectedDto);

        OrderResponseDto result = service.delete(orderId);

        assertTrue(order.getDeleted());
        assertEquals(expectedDto, result);
        verify(orderRepo).save(order);
    }

    @Test
    void findByIdShouldThrowExceptionWhenNotFound() {
        when(orderRepo.findById(99L)).thenReturn(Optional.empty());
        assertThrows(OrderNotFoundException.class, () -> service.findById(99L));
    }
}