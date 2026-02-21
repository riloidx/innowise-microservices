package com.innowise.orderservice.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.innowise.orderservice.dto.request.ItemCreateDto;
import com.innowise.orderservice.dto.request.ItemUpdateDto;
import com.innowise.orderservice.entity.Item;
import com.innowise.orderservice.repository.ItemRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class ItemIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Item phoneItem;
    private Item laptopItem;

    @BeforeEach
    void setUp() {
        Item phone = new Item();
        phone.setName("iPhone 15");
        phone.setPrice(BigDecimal.valueOf(999.99));
        phoneItem = itemRepository.save(phone);

        Item laptop = new Item();
        laptop.setName("MacBook Pro");
        laptop.setPrice(BigDecimal.valueOf(2500.00));
        laptopItem = itemRepository.save(laptop);
    }

    @AfterEach
    void tearDown() {
        itemRepository.deleteAll();
    }

    @Test
    void shouldCreateItem() throws Exception {
        ItemCreateDto createDto = new ItemCreateDto("Mouse", BigDecimal.valueOf(50.00));

        String jsonRequest = objectMapper.writeValueAsString(createDto);

        mockMvc.perform(post("/orders/items")
                        .contentType(APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(notNullValue())))
                .andExpect(jsonPath("$.name", is("Mouse")))
                .andExpect(jsonPath("$.price", is(50.00)));
    }

    @Test
    void shouldFindAllItems() throws Exception {
        mockMvc.perform(get("/orders/items")
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name", is(notNullValue())));
    }

    @Test
    void shouldFindItemById() throws Exception {
        mockMvc.perform(get("/orders/items/{id}", phoneItem.getId())
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(phoneItem.getId().intValue())))
                .andExpect(jsonPath("$.name", is("iPhone 15")))
                .andExpect(jsonPath("$.price", is(999.99)));
    }

    @Test
    void shouldFindItemByName() throws Exception {
        mockMvc.perform(get("/orders/items/search")
                        .param("name", laptopItem.getName())
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(laptopItem.getId().intValue())))
                .andExpect(jsonPath("$.name", is("MacBook Pro")));
    }

    @Test
    void shouldUpdateItem() throws Exception {
        ItemUpdateDto updateDto = new ItemUpdateDto("iPhone 15 Pro", BigDecimal.valueOf(1100.00));

        String jsonRequest = objectMapper.writeValueAsString(updateDto);

        mockMvc.perform(put("/orders/items/{id}", phoneItem.getId())
                        .contentType(APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("iPhone 15 Pro")))
                .andExpect(jsonPath("$.price", is(1100.00)));
    }

    @Test
    void shouldDeleteItem() throws Exception {
        mockMvc.perform(delete("/orders/items/{id}", laptopItem.getId())
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/orders/items/{id}", laptopItem.getId())
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }
}