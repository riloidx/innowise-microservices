package com.innowise.authservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthResponseDto {
    String login;
    String accessToken;
    String refreshToken;
}
