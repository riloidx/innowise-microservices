package com.innowise.orderservice.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.innowise.orderservice.dto.request.OrderCreateDto;
import com.innowise.orderservice.dto.request.OrderItemDto;
import com.innowise.orderservice.dto.request.OrderUpdateDto;
import com.innowise.orderservice.entity.Item;
import com.innowise.orderservice.entity.Order;
import com.innowise.orderservice.enums.OrderStatus;
import com.innowise.orderservice.repository.ItemRepository;
import com.innowise.orderservice.repository.OrderRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.math.BigDecimal;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class OrderIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private ObjectMapper objectMapper;

    private Item savedItem;
    private Order savedOrder;

    @BeforeEach
    void setUp() {
        Item item = new Item();
        item.setName("Laptop");
        item.setPrice(BigDecimal.valueOf(1000.00));
        savedItem = itemRepository.save(item);

        Order order = new Order();
        order.setUserId(1L);
        order.setStatus(OrderStatus.PENDING);
        order.setDeleted(false);
        order.setTotalPrice(BigDecimal.valueOf(1000.00));
        savedOrder = orderRepository.save(order);

        stubFor(get(urlPathEqualTo("/api/users/1"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(200)
                        .withBody("""
                                {
                                    "id": 1,
                                    "name": "Alice",
                                    "surname": "Smith",
                                    "email": "alice@test.com"
                                }
                                """)));
    }

    @AfterEach
    void tearDown() {
        orderRepository.deleteAll();
        itemRepository.deleteAll();
    }

    @Test
    void createOrderShouldReturnFullDtoWithUserInfo() throws Exception {
        OrderCreateDto createDto = new OrderCreateDto(1L, List.of(new OrderItemDto(savedItem.getId(), 2)));
        String jsonRequest = objectMapper.writeValueAsString(createDto);

        mockMvc.perform(MockMvcRequestBuilders.post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.totalPrice", is(2000.00)))
                .andExpect(jsonPath("$.user.id", is(1)))
                .andExpect(jsonPath("$.user.email", is("alice@test.com")));
    }

    @Test
    void findByIdShouldReturnFullDtoWithUserInfo() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/orders/{id}", savedOrder.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(savedOrder.getId().intValue())))
                .andExpect(jsonPath("$.user.name", is("Alice")));
    }

    @Test
    void findByUserIdShouldReturnListOfOrders() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/orders/user/{userId}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].user.surname", is("Smith")));
    }

    @Test
    void updateOrderShouldModifyStatusAndRecalculatePrice() throws Exception {
        OrderUpdateDto updateDto = new OrderUpdateDto(OrderStatus.CONFIRMED, List.of(new OrderItemDto(savedItem.getId(), 5)));
        String jsonRequest = objectMapper.writeValueAsString(updateDto);

        mockMvc.perform(MockMvcRequestBuilders.put("/orders/{id}", savedOrder.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("CONFIRMED")))
                .andExpect(jsonPath("$.totalPrice", is(5000.00)))
                .andExpect(jsonPath("$.user.email", is("alice@test.com")));
    }

    @Test
    void deleteOrderShouldSoftDelete() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/orders/{id}", savedOrder.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.deleted", is(true)));

        mockMvc.perform(MockMvcRequestBuilders.delete("/orders/{id}", savedOrder.getId()))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void findAllWithFiltersShouldReturnCorrectPage() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/orders")
                        .param("status", "PENDING")
                        .param("deleted", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].user.name", is("Alice")));
    }

    @Test
    void createOrderShouldHandleUserServiceFailure() throws Exception {
        stubFor(get(urlPathEqualTo("/api/users/2"))
                .willReturn(aResponse().withStatus(500)));

        OrderCreateDto createDto = new OrderCreateDto(2L, List.of(new OrderItemDto(savedItem.getId(), 1)));
        String jsonRequest = objectMapper.writeValueAsString(createDto);

        mockMvc.perform(MockMvcRequestBuilders.post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.user.name", is("unknown")));
    }
}