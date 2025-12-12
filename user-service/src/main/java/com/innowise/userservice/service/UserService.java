package com.innowise.userservice.service;

import com.innowise.userservice.dto.request.UserCreateDto;
import com.innowise.userservice.dto.request.UserUpdateDto;
import com.innowise.userservice.dto.response.UserResponseDto;
import com.innowise.userservice.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

public interface UserService {

    // Commands

    UserResponseDto create(UserCreateDto userCreateDto);

    UserResponseDto update(long id, UserUpdateDto userUpdateDto);

    void delete(long id);

    UserResponseDto changeStatus(long id, boolean status);

    // Entity queries

    User findById(long id);

    // DTO queries

    UserResponseDto findDtoById(long id);

    UserResponseDto findDtoByEmail(String email);

    Page<UserResponseDto> findAll(String name,
                                  String surname,
                                  LocalDate birthDate,
                                  Boolean active,
                                  Pageable pageable);
}
