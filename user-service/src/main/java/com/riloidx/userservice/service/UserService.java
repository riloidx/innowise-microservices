package com.riloidx.userservice.service;

import com.riloidx.userservice.dto.request.UserCreateDto;
import com.riloidx.userservice.dto.request.UserUpdateDto;
import com.riloidx.userservice.dto.response.UserResponseDto;
import com.riloidx.userservice.entity.User;
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
