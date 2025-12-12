package com.innowise.userservice.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.innowise.userservice.dto.request.PaymentCardCreateDto;
import com.innowise.userservice.dto.request.PaymentCardUpdateDto;
import com.innowise.userservice.entity.PaymentCard;
import com.innowise.userservice.entity.User;
import com.innowise.userservice.repository.PaymentCardRepository;
import com.innowise.userservice.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;

import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class PaymentCardIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private PaymentCardRepository paymentCardRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private User testUser;
    private PaymentCard activeCard;
    private PaymentCard inactiveCard;

    @BeforeEach
    void setUp() {
        testUser = userRepository.save(User.builder()
                .name("Test")
                .surname("test")
                .email("john@example.com")
                .birthDate(LocalDate.of(2000, 1, 1))
                .active(true)
                .build());

        activeCard = paymentCardRepository.save(PaymentCard.builder()
                .number("1111222233334444")
                .holder("ACTIVE TEST")
                .expirationDate(LocalDate.now().plusYears(1))
                .active(true)
                .user(testUser)
                .build());

        inactiveCard = paymentCardRepository.save(PaymentCard.builder()
                .number("5555666677778888")
                .holder("INACTIVE TEST")
                .expirationDate(LocalDate.now().plusMonths(6))
                .active(false)
                .user(testUser)
                .build());
    }

    @AfterEach
    void tearDown() {
        paymentCardRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void shouldGetCardById() throws Exception {
        mockMvc.perform(get("/cards/{id}", activeCard.getId())
                        .headers(createAuthHeaders())
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(activeCard.getId().intValue())))
                .andExpect(jsonPath("$.number", is(activeCard.getNumber())))
                .andExpect(jsonPath("$.holder", is(activeCard.getHolder())))
                .andExpect(jsonPath("$.expirationDate", is(activeCard.getExpirationDate().toString())))
                .andExpect(jsonPath("$.active", is(true)));
    }

    @Test
    void shouldFindAllCardsForUser() throws Exception {
        mockMvc.perform(get("/cards/user/{userId}", testUser.getId())
                        .headers(createAuthHeaders())
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].holder", is(activeCard.getHolder())))
                .andExpect(jsonPath("$[1].holder", is(inactiveCard.getHolder())));
    }

    @Test
    void shouldFindAllCardsWithNoFilter() throws Exception {
        mockMvc.perform(get("/cards")
                        .headers(createAuthHeaders())
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)));
    }

    @Test
    void shouldFindAllActiveCardsFilter() throws Exception {
        mockMvc.perform(get("/cards")
                        .headers(createAuthHeaders())
                        .param("active", "true")
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].holder", is(activeCard.getHolder())));
    }

    @Test
    void shouldFindAllCardsExpiresAfterFilter() throws Exception {
        LocalDate futureDate = LocalDate.now().minusDays(1);

        mockMvc.perform(get("/cards")
                        .headers(createAuthHeaders())
                        .param("expires_after", futureDate.toString())
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)));
    }

    @Test
    void shouldCreateCard() throws Exception {
        PaymentCardCreateDto createDto = PaymentCardCreateDto.builder()
                .number("1234123412341234")
                .expirationDate(LocalDate.of(2030, 1, 1))
                .userId(testUser.getId())
                .build();

        String jsonRequest = objectMapper.writeValueAsString(createDto);

        mockMvc.perform(post("/cards")
                        .headers(createAuthHeaders())
                        .contentType(APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(notNullValue())))
                .andExpect(jsonPath("$.number", is("1234123412341234")))
                .andExpect(jsonPath("$.holder", is(testUser.getName() + " " + testUser.getSurname())))
                .andExpect(jsonPath("$.active", is(true)));
    }

    @Test
    void shouldUpdateCard() throws Exception {
        PaymentCardUpdateDto updateDto = PaymentCardUpdateDto.builder()
                .id(3L)
                .expirationDate(LocalDate.of(2035, 1, 1))
                .build();

        String jsonRequest = objectMapper.writeValueAsString(updateDto);

        mockMvc.perform(put("/cards/{id}", activeCard.getId())
                        .headers(createAuthHeaders())
                        .contentType(APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.expirationDate", is("2035-01-01")));
    }

    @Test
    void shouldActivateCard() throws Exception {
        mockMvc.perform(patch("/cards/{id}/activate", inactiveCard.getId())
                        .headers(createAuthHeaders())
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(inactiveCard.getId().intValue())))
                .andExpect(jsonPath("$.active", is(true)));
    }

    @Test
    void shouldDeactivateCard() throws Exception {
        mockMvc.perform(patch("/cards/{id}/deactivate", activeCard.getId())
                        .headers(createAuthHeaders())
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(activeCard.getId().intValue())))
                .andExpect(jsonPath("$.active", is(false)));
    }
}