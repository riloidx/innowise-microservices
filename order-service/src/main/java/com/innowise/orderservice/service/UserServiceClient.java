package com.innowise.orderservice.service;

import com.innowise.orderservice.dto.response.UserResponseDto;

public interface UserServiceClient {
    UserResponseDto getUserById(long ig);
}
