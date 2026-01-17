package com.innowise.orderservice.service;

import com.innowise.orderservice.dto.request.OrderCreateDto;
import com.innowise.orderservice.dto.request.OrderItemDto;
import com.innowise.orderservice.dto.request.OrderUpdateDto;
import com.innowise.orderservice.dto.response.OrderFullResponseDto;
import com.innowise.orderservice.dto.response.OrderResponseDto;
import com.innowise.orderservice.dto.response.UserResponseDto;
import com.innowise.orderservice.entity.Item;
import com.innowise.orderservice.entity.Order;
import com.innowise.orderservice.entity.OrderItem;
import com.innowise.orderservice.enums.OrderStatus;
import com.innowise.orderservice.exception.OrderNotFoundException;
import com.innowise.orderservice.mapper.OrderMapper;
import com.innowise.orderservice.repository.OrderRepository;
import com.innowise.orderservice.specification.OrderSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepo;
    private final ItemService itemService;
    private final UserServiceClient userService;
    private final OrderMapper orderMapper;

    @Override
    @Transactional
    public OrderFullResponseDto create(OrderCreateDto orderCreateDto) {
        log.info("Creating new order for user ID: {}", orderCreateDto.userId());
        
        Order order = orderMapper.toEntity(orderCreateDto);

        processOrderItems(order, orderCreateDto.items());

        Order savedOrder = orderRepo.save(order);
        log.info("Order created successfully with ID: {}", savedOrder.getId());

        return convertToFullDto(savedOrder);
    }

    @Override
    public Page<OrderFullResponseDto> findAll(Pageable pageable,
                                          OrderStatus orderStatus,
                                          Boolean deleted,
                                          LocalDate createdAfter,
                                          LocalDate createdBefore) {
        log.debug("Finding all orders with filters - status: {}, deleted: {}", orderStatus, deleted);
        
        Specification<Order> spec = prepareSpecification(orderStatus, deleted, createdAfter, createdBefore);

        Page<Order> orders = orderRepo.findAll(spec, pageable);
        log.debug("Found {} orders", orders.getTotalElements());

        return orders.map(this::convertToFullDto);
    }

    @Override
    public Order findById(long id) {
        log.debug("Finding order by ID: {}", id);
        return orderRepo.findById(id).
                orElseThrow(() -> {
                    log.warn("Order not found with ID: {}", id);
                    return new OrderNotFoundException("id", String.valueOf(id));
                });
    }

    @Override
    public OrderFullResponseDto findDtoById(long id) {
        return convertToFullDto(findById(id));
    }

    @Override
    public List<OrderFullResponseDto> findByUserId(long userId) {
        log.debug("Finding orders for user ID: {}", userId);
        
        List<Order> orders = orderRepo.findAllByUserId(userId);

        UserResponseDto userDto = userService.getUserById(userId);
        log.debug("Found {} orders for user ID: {}", orders.size(), userId);

        return orders.stream()
                .map(order -> orderMapper.toFullDto(order, userDto))
                .toList();
    }

    @Override
    @Transactional
    public OrderFullResponseDto update(long id, OrderUpdateDto orderUpdateDto) {
        log.info("Updating order with ID: {}", id);
        
        Order curOrder = findById(id);

        validateOrderNotDeleted(curOrder);

        orderMapper.updateEntityFromDto(orderUpdateDto, curOrder);

        processOrderItems(curOrder, orderUpdateDto.items());

        orderRepo.save(curOrder);
        log.info("Order updated successfully with ID: {}", id);

        return convertToFullDto(curOrder);
    }

    @Override
    @Transactional
    public void updateStatusFromPayment(long orderId, String paymentStatus) {
        log.info("Updating order status from payment - Order ID: {}, Payment status: {}", orderId, paymentStatus);
        
        Order order = findById(orderId);

        if (paymentStatus.equals("SUCCESS")) {
            order.setStatus(OrderStatus.CONFIRMED);
            log.info("Order {} status changed to CONFIRMED", orderId);
        } else {
            order.setStatus(OrderStatus.CANCELED);
            log.info("Order {} status changed to CANCELED", orderId);
        }

        orderRepo.save(order);
    }

    @Override
    @Transactional
    public OrderResponseDto delete(long id) {
        log.info("Deleting order with ID: {}", id);
        
        Order curOrder = findById(id);

        validateOrderNotDeleted(curOrder);

        curOrder.setDeleted(true);
        log.info("Order marked as deleted with ID: {}", id);

        return orderMapper.toDto(orderRepo.save(curOrder));
    }

    private void processOrderItems(Order order, List<OrderItemDto> itemsDto) {
        log.debug("Processing {} order items", itemsDto.size());
        
        order.getOrderItems().clear();

        BigDecimal totalPrice = BigDecimal.ZERO;

        for (OrderItemDto itemDto : itemsDto) {
            Item realItem = itemService.findById(itemDto.itemId());

            BigDecimal subTotal = realItem.getPrice().multiply(BigDecimal.valueOf(itemDto.quantity()));
            totalPrice = totalPrice.add(subTotal);

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setItem(realItem);
            orderItem.setQuantity(itemDto.quantity());

            order.getOrderItems().add(orderItem);
        }
        order.setTotalPrice(totalPrice.setScale(2, RoundingMode.HALF_UP));
        log.debug("Order total price calculated: {}", totalPrice);
    }

    private OrderFullResponseDto convertToFullDto(Order order) {
        UserResponseDto userDto = userService.getUserById(order.getUserId());

        return orderMapper.toFullDto(order, userDto);
    }

    private void validateOrderNotDeleted(Order order) {
        if (Boolean.TRUE.equals(order.getDeleted())) {
            log.warn("Attempted to modify deleted order with ID: {}", order.getId());
            throw new IllegalArgumentException("Order has been deleted");
        }
    }

    private Specification<Order> prepareSpecification(OrderStatus status,
                                                      Boolean deleted,
                                                      LocalDate createdAfter,
                                                      LocalDate createdBefore) {

        Specification<Order> spec = Specification.unrestricted();

        if (status != null) {
            spec = spec.and(OrderSpecification.hasStatus(status));
        }

        if (deleted != null) {
            spec = spec.and(OrderSpecification.isDeleted(deleted));
        }

        if (createdAfter != null) {
            Instant afterInstant = createdAfter.atStartOfDay(ZoneId.systemDefault()).toInstant();
            spec = spec.and(OrderSpecification.createdAfter(afterInstant));
        }

        if (createdBefore != null) {
            Instant beforeInstant = createdBefore.atTime(LocalTime.MAX).atZone(ZoneId.systemDefault()).toInstant();
            spec = spec.and(OrderSpecification.createdBefore(beforeInstant));
        }

        return spec;
    }
}