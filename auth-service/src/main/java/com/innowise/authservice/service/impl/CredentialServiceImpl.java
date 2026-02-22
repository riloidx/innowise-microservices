package com.innowise.authservice.service.impl;

import com.innowise.authservice.dto.request.RegistrationDto;
import com.innowise.authservice.dto.response.AuthResponseDto;
import com.innowise.authservice.entity.Credential;
import com.innowise.authservice.enums.Role;
import com.innowise.authservice.jwt.JwtUtil;
import com.innowise.authservice.repository.CredentialRepository;
import com.innowise.authservice.service.api.CredentialService;
import com.innowise.authservice.user.CustomUserDetails;
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