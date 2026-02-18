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

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepo;
    private final ItemService itemService;
    private final UserServiceClient userService;
    private final OrderMapper orderMapper;

    @Override
    @Transactional
    public Mono<OrderFullResponseDto> create(OrderCreateDto orderCreateDto) {
        Order order = orderMapper.toEntity(orderCreateDto);
        processOrderItems(order, orderCreateDto.items());
        Order savedOrder = orderRepo.save(order);
        return convertToFullDto(savedOrder);
    }

    @Override
    @Transactional(readOnly = true)
    public Mono<Page<OrderFullResponseDto>> findAll(Pageable pageable,
                                                    OrderStatus orderStatus,
                                                    Boolean deleted,
                                                    LocalDate createdAfter,
                                                    LocalDate createdBefore) {
        Specification<Order> spec = prepareSpecification(orderStatus, deleted, createdAfter, createdBefore);
        Page<Order> ordersPage = orderRepo.findAll(spec, pageable);

        return Flux.fromIterable(ordersPage.getContent())
                .flatMap(this::convertToFullDto)
                .collectList()
                .map(list -> new PageImpl<>(list, pageable, ordersPage.getTotalElements()));
    }

    @Override
    @Transactional(readOnly = true)
    public Order findById(long id) {
        return orderRepo.findById(id)
                .orElseThrow(() -> new OrderNotFoundException("id", String.valueOf(id)));
    }

    @Override
    @Transactional(readOnly = true)
    public Mono<OrderFullResponseDto> findDtoById(long id) {
        return Mono.fromCallable(() -> findById(id))
                .flatMap(this::convertToFullDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Mono<List<OrderFullResponseDto>> findByUserId(long userId) {
        List<Order> orders = orderRepo.findAllByUserId(userId);

        return userService.getUserById(userId)
                .map(userDto -> orders.stream()
                        .map(order -> orderMapper.toFullDto(order, userDto))
                        .toList()
                );
    }

    @Override
    @Transactional
    public Mono<OrderFullResponseDto> update(long id, OrderUpdateDto orderUpdateDto) {
        Order curOrder = findById(id);
        validateOrderNotDeleted(curOrder);
        orderMapper.updateEntityFromDto(orderUpdateDto, curOrder);
        processOrderItems(curOrder, orderUpdateDto.items());
        Order savedOrder = orderRepo.save(curOrder);

        return convertToFullDto(savedOrder);
    }

    @Override
    @Transactional
    public Mono<OrderResponseDto> delete(long id) {
        Order curOrder = findById(id);
        validateOrderNotDeleted(curOrder);
        curOrder.setDeleted(true);

        return Mono.fromCallable(() -> orderMapper.toDto(orderRepo.save(curOrder)));
    }

    private void processOrderItems(Order order, List<OrderItemDto> itemsDto) {
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
    }

    private Mono<OrderFullResponseDto> convertToFullDto(Order order) {
        return userService.getUserById(order.getUserId())
                .map(userDto -> orderMapper.toFullDto(order, userDto));
    }

    private void validateOrderNotDeleted(Order order) {
        if (Boolean.TRUE.equals(order.getDeleted())) {
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