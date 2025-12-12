package com.innowise.orderservice.unit;

import com.innowise.orderservice.dto.request.OrderCreateDto;
import com.innowise.orderservice.dto.request.OrderItemDto;
import com.innowise.orderservice.dto.request.OrderUpdateDto;
import com.innowise.orderservice.dto.response.OrderFullResponseDto;
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

        Page<OrderFullResponseDto> result = service.findAll(pageable, null, null, null, null);

        assertEquals(1, result.getTotalElements());
        verify(userService).getUserById(1L);
    }

    @Test
    void findByIdShouldThrowExceptionWhenNotFound() {
        when(orderRepo.findById(99L)).thenReturn(Optional.empty());

        assertThrows(OrderNotFoundException.class, () -> service.findById(99L));
    }

    @Test
    void findByUserIdShouldReturnListWithUserInfo() {
        Order order = new Order();
        order.setUserId(1L);

        when(orderRepo.findAllByUserId(1L)).thenReturn(List.of(order));
        when(userService.getUserById(1L)).thenReturn(mockUser);

        when(orderMapper.toFullDto(order, mockUser)).thenReturn(null);

        service.findByUserId(1L);

        verify(userService).getUserById(1L);
        verify(orderMapper).toFullDto(order, mockUser);
    }

    @Test
    void updateShouldThrowWhenOrderIsDeleted() {
        long orderId = 1L;
        Order deletedOrder = new Order();
        deletedOrder.setId(orderId);
        deletedOrder.setDeleted(true);

        when(orderRepo.findById(orderId)).thenReturn(Optional.of(deletedOrder));

        assertThrows(IllegalArgumentException.class,
                () -> service.update(orderId, new OrderUpdateDto(OrderStatus.CONFIRMED, List.of())));

        verify(orderRepo, never()).save(any());
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

        when(orderRepo.findById(orderId)).thenReturn(Optional.of(existingOrder));
        when(itemService.findById(10L)).thenReturn(item);

        doAnswer(invocation -> {
            Order targetOrder = invocation.getArgument(1);
            targetOrder.setStatus(OrderStatus.CONFIRMED);
            return null;
        }).when(orderMapper).updateEntityFromDto(updateDto, existingOrder);

        when(orderRepo.save(any(Order.class))).thenReturn(existingOrder);

        when(userService.getUserById(1L)).thenReturn(mockUser);

        service.update(orderId, updateDto);

        verify(orderMapper).updateEntityFromDto(updateDto, existingOrder);
        verify(orderRepo).save(any(Order.class));

        assertEquals(0, new BigDecimal("10.00").compareTo(existingOrder.getTotalPrice()));
        assertEquals(1, existingOrder.getOrderItems().size());
    }

    @Test
    void deleteShouldThrowWhenAlreadyDeleted() {
        long orderId = 1L;
        Order deletedOrder = new Order();
        deletedOrder.setDeleted(true);

        when(orderRepo.findById(orderId)).thenReturn(Optional.of(deletedOrder));

        assertThrows(IllegalArgumentException.class, () -> service.delete(orderId));
    }

    @Test
    void deleteShouldSetDeletedFlagAndReturnDto() {
        long orderId = 1L;
        Order order = new Order();
        order.setId(orderId);
        order.setDeleted(false);

        when(orderRepo.findById(orderId)).thenReturn(Optional.of(order));
        when(orderRepo.save(order)).thenReturn(order);

        service.delete(orderId);

        assertTrue(order.getDeleted());
        verify(orderRepo).save(order);
        verify(orderMapper).toDto(order);
    }
}