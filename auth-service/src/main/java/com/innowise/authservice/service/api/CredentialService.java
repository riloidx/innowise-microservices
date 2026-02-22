package com.innowise.authservice.service.api;

import com.innowise.authservice.dto.request.RegistrationDto;
import com.innowise.authservice.dto.response.AuthResponseDto;

public interface CredentialService {
    AuthResponseDto createCredentials(RegistrationDto dto, Long userId);
}
