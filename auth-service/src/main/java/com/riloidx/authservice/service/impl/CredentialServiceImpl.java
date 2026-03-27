package com.riloidx.authservice.service.impl;

import com.riloidx.authservice.dto.request.RegistrationDto;
import com.riloidx.authservice.dto.response.AuthResponseDto;
import com.riloidx.authservice.entity.Credential;
import com.riloidx.authservice.enums.Role;
import com.riloidx.authservice.jwt.JwtUtil;
import com.riloidx.authservice.repository.CredentialRepository;
import com.riloidx.authservice.service.api.CredentialService;
import com.riloidx.authservice.user.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CredentialServiceImpl implements CredentialService {
    private final CredentialRepository credentialRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Transactional
    public AuthResponseDto createCredentials(RegistrationDto dto, Long userId) {
        Credential credential = Credential.builder()
                .userId(userId)
                .login(dto.getLogin())
                .passwordHash(passwordEncoder.encode(dto.getPassword()))
                .role(Role.USER)
                .build();

        credentialRepo.save(credential);
        CustomUserDetails userDetails = new CustomUserDetails(credential);

        return new AuthResponseDto(
                credential.getLogin(),
                jwtUtil.generateAccessToken(userDetails),
                jwtUtil.generateRefreshToken(userDetails)
        );
    }
}