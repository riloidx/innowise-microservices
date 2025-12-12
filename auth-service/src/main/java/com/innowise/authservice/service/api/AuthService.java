package com.innowise.authservice.service.api;


import com.innowise.authservice.dto.request.LoginDto;
import com.innowise.authservice.dto.request.RefreshRequestDto;
import com.innowise.authservice.dto.request.RegistrationDto;
import com.innowise.authservice.dto.response.AuthResponseDto;
import com.innowise.authservice.dto.response.RefreshResponseDto;
import com.innowise.authservice.dto.response.ValidateResponseDto;

public interface AuthService {
    AuthResponseDto login(LoginDto loginDto);

    AuthResponseDto register(RegistrationDto registrationDto);
    
    RefreshResponseDto refresh(RefreshRequestDto refreshRequestDto);
    
    ValidateResponseDto validateToken(String token);
}
