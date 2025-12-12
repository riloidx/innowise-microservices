package com.innowise.userservice.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.innowise.userservice.dto.request.UserCreateDto;
import com.innowise.userservice.dto.request.UserUpdateDto;
import com.innowise.userservice.entity.User;
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
class UserIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private User activeUser;
    private User inactiveUser;

    @BeforeEach
    void setUp() {
        activeUser = userRepository.save(User.builder()
                .name("John")
                .surname("Doe")
                .email("john.doe@test.com")
                .birthDate(LocalDate.of(1990, 5, 15))
                .active(true)
                .build());

        inactiveUser = userRepository.save(User.builder()
                .name("Jane")
                .surname("Smith")
                .email("jane.smith@test.com")
                .birthDate(LocalDate.of(1995, 10, 25))
                .active(false)
                .build());
    }

    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
    }

    @Test
    void shouldGetUserById() throws Exception {
        mockMvc.perform(get("/users/{id}", activeUser.getId())
                        .headers(createAuthHeaders())
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(activeUser.getId().intValue())))
                .andExpect(jsonPath("$.name", is(activeUser.getName())))
                .andExpect(jsonPath("$.email", is(activeUser.getEmail())))
                .andExpect(jsonPath("$.active", is(true)));
    }

    @Test
    void shouldGetUserByEmail() throws Exception {
        mockMvc.perform(get("/users/email/{email}", inactiveUser.getEmail())
                        .headers(createAuthHeaders())
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(inactiveUser.getId().intValue())))
                .andExpect(jsonPath("$.name", is(inactiveUser.getName())))
                .andExpect(jsonPath("$.active", is(false)));
    }

    @Test
    void shouldFindAllUsersWithNoFilter() throws Exception {
        mockMvc.perform(get("/users")
                        .headers(createAuthHeaders())
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.page.totalElements", is(2)));
    }

    @Test
    void shouldFindAllActiveUsersFilter() throws Exception {
        mockMvc.perform(get("/users")
                        .headers(createAuthHeaders())
                        .param("active", "true")
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name", is(activeUser.getName())));
    }

    @Test
    void shouldFindUsersByNameAndSurnameFilter() throws Exception {
        mockMvc.perform(get("/users")
                        .headers(createAuthHeaders())
                        .param("name", activeUser.getName())
                        .param("surname", activeUser.getSurname())
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].email", is(activeUser.getEmail())));
    }

    @Test
    void shouldCreateUser() throws Exception {
        UserCreateDto createDto = UserCreateDto.builder()
                .name("Test")
                .surname("User")
                .email("test.user@create.com")
                .birthDate(LocalDate.of(2005, 1, 1))
                .build();

        String jsonRequest = objectMapper.writeValueAsString(createDto);

        mockMvc.perform(post("/users")
                        .headers(createAuthHeaders())
                        .contentType(APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(notNullValue())))
                .andExpect(jsonPath("$.name", is("Test")))
                .andExpect(jsonPath("$.email", is("test.user@create.com")))
                .andExpect(jsonPath("$.active", is(true)));
    }

    @Test
    void shouldUpdateUser() throws Exception {
        UserUpdateDto updateDto = UserUpdateDto.builder()
                .id(activeUser.getId())
                .name("newName")
                .birthDate(LocalDate.of(1991, 1, 1))
                .build();

        String jsonRequest = objectMapper.writeValueAsString(updateDto);

        mockMvc.perform(put("/users/{id}", activeUser.getId())
                        .headers(createAuthHeaders())
                        .contentType(APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("newName")))
                .andExpect(jsonPath("$.birthDate", is("1991-01-01")));
    }

    @Test
    void shouldDeleteUser() throws Exception {
        mockMvc.perform(delete("/users/{id}", activeUser.getId())
                        .headers(createAuthHeaders())
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/users/{id}", activeUser.getId())
                        .headers(createAuthHeaders())
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldActivateUser() throws Exception {
        mockMvc.perform(patch("/users/{id}/activate", inactiveUser.getId())
                        .headers(createAuthHeaders())
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(inactiveUser.getId().intValue())))
                .andExpect(jsonPath("$.active", is(true)));
    }

    @Test
    void shouldDeactivateUser() throws Exception {
        mockMvc.perform(patch("/users/{id}/deactivate", activeUser.getId())
                        .headers(createAuthHeaders())
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(activeUser.getId().intValue())))
                .andExpect(jsonPath("$.active", is(false)));
    }
}