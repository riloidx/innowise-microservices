package com.riloidx.orderservice.dto.response;

public record UserResponseDto(
        Long id,
        String name,
        String surname,
        String email
) {}
