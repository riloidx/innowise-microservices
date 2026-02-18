package com.innowise.orderservice.service;

import com.innowise.orderservice.dto.response.UserResponseDto;
import reactor.core.publisher.Mono;

public interface UserServiceClient {
    Mono<UserResponseDto> getUserById(long ig);
}
