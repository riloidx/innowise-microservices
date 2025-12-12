package com.innowise.authservice.dto.response;

import lombok.Data;

@Data
public class RefreshResponseDto {
    private String accessToken;
    private String refreshToken;
}
