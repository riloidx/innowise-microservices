package com.riloidx.authservice.service.api;

import com.riloidx.authservice.dto.request.RegistrationDto;
import com.riloidx.authservice.dto.response.AuthResponseDto;

public interface CredentialService {
    AuthResponseDto createCredentials(RegistrationDto dto, Long userId);
}
