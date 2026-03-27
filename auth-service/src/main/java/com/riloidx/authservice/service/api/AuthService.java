package com.riloidx.authservice.service.api;


import com.riloidx.authservice.dto.request.LoginDto;
import com.riloidx.authservice.dto.request.RefreshRequestDto;
import com.riloidx.authservice.dto.request.RegistrationDto;
import com.riloidx.authservice.dto.response.AuthResponseDto;
import com.riloidx.authservice.dto.response.RefreshResponseDto;
import com.riloidx.authservice.dto.response.ValidateResponseDto;

public interface AuthService {
    AuthResponseDto login(LoginDto loginDto);

    AuthResponseDto register(RegistrationDto registrationDto);
    
    RefreshResponseDto refresh(RefreshRequestDto refreshRequestDto);
    
    ValidateResponseDto validateToken(String token);
}
