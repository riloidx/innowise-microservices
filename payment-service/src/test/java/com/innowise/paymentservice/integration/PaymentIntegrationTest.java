package com.innowise.paymentservice.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.innowise.paymentservice.dto.request.PaymentCreateDto;
import com.innowise.paymentservice.repository.PaymentRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.math.BigDecimal;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PaymentIntegrationTest extends BaseIntegrationTest {

    @Autowired private PaymentRepository paymentRepository;
    @Autowired private ObjectMapper objectMapper;

    @AfterEach
    void cleanUp() {
        paymentRepository.deleteAll();
    }

    @Test
    void createPaymentShouldWorkCorrectly() throws Exception {
        stubFor(get(urlPathEqualTo("/csrng/csrng.php"))
                .withQueryParam("min", equalTo("1"))
                .withQueryParam("max", equalTo("100"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("[{\"random\": 4}]")));

        PaymentCreateDto request = new PaymentCreateDto(1L, 101L, new BigDecimal("150.00"));

        mockMvc.perform(MockMvcRequestBuilders.post("/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status", is("SUCCESS")))
                .andExpect(jsonPath("$.userId", is(101)))
                .andExpect(jsonPath("$.orderId", is(1)));
    }

    @Test
    void createPaymentShouldReturn502WhenExternalServiceFails() throws Exception {
        stubFor(get(urlPathEqualTo("/csrng/csrng.php"))
                .willReturn(aResponse().withStatus(500)));

        PaymentCreateDto request = new PaymentCreateDto(1L, 101L, BigDecimal.TEN);

        mockMvc.perform(MockMvcRequestBuilders.post("/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadGateway());
    }
}