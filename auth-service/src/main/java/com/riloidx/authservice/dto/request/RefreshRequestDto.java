package com.riloidx.authservice.dto.request;

import lombok.Data;

@Data
public class RefreshRequestDto {
    private String refreshToken;
}
