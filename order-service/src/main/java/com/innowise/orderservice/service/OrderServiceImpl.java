package com.innowise.orderservice.service;

import com.innowise.orderservice.dto.request.OrderCreateDto;
import com.innowise.orderservice.dto.request.OrderItemDto;
import com.innowise.orderservice.dto.request.OrderUpdateDto;
import com.innowise.orderservice.dto.response.OrderFullResponseDto;
import com.innowise.orderservice.dto.response.OrderResponseDto;
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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

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
        Order order = orderMapper.toEntity(orderCreateDto);
        processOrderItems(order, orderCreateDto.items());

        Order savedOrder = orderRepo.save(order);
        log.info("Order created successfully with ID: {}", savedOrder.getId());

        return convertToFullDto(savedOrder);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderFullResponseDto> findAll(Pageable pageable,
                                              OrderStatus orderStatus,
                                              Boolean deleted,
                                              LocalDate createdAfter,
                                              LocalDate createdBefore) {
        Specification<Order> spec = prepareSpecification(orderStatus, deleted, createdAfter, createdBefore);
        Page<Order> ordersPage = orderRepo.findAll(spec, pageable);

        return ordersPage.map(this::convertToFullDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Order findById(long id) {
        return orderRepo.findById(id)
                .orElseThrow(() -> new OrderNotFoundException("id", String.valueOf(id)));
    }

    @Override
    @Transactional(readOnly = true)
    public OrderFullResponseDto findDtoById(long id) {
        return convertToFullDto(findById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderFullResponseDto> findByUserId(long userId) {
        log.debug("Fetching all orders for user ID: {}", userId);

        List<Order> orders = orderRepo.findAllByUserId(userId);

        var userDto = userService.getUserById(userId);

        return orders.stream()
                .map(order -> orderMapper.toFullDto(order, userDto))
                .toList();
    }

    @Override
    @Transactional
    public OrderFullResponseDto update(long id, OrderUpdateDto orderUpdateDto) {
        Order curOrder = findById(id);
        validateOrderNotDeleted(curOrder);

        orderMapper.updateEntityFromDto(orderUpdateDto, curOrder);
        processOrderItems(curOrder, orderUpdateDto.items());

        return convertToFullDto(orderRepo.save(curOrder));
    }

    @Override
    @Transactional
    public void updateStatusFromPayment(long orderId, String paymentStatus) {
        Order order = findById(orderId);
        order.setStatus("SUCCESS".equals(paymentStatus) ? OrderStatus.CONFIRMED : OrderStatus.CANCELED);
        orderRepo.save(order);
    }

    @Override
    @Transactional
    public OrderResponseDto delete(long id) {
        Order curOrder = findById(id);
        validateOrderNotDeleted(curOrder);

        curOrder.setDeleted(true);
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
        var userDto = userService.getUserById(order.getUserId());
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