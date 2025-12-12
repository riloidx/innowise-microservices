package com.innowise.authservice.service.impl;

import com.innowise.authservice.dto.request.LoginDto;
import com.innowise.authservice.dto.request.RefreshRequestDto;
import com.innowise.authservice.dto.request.RegistrationDto;
import com.innowise.authservice.dto.response.AuthResponseDto;
import com.innowise.authservice.dto.response.RefreshResponseDto;
import com.innowise.authservice.dto.response.ValidateResponseDto;
import com.innowise.authservice.entity.Credential;
import com.innowise.authservice.enums.Role;
import com.innowise.authservice.exception.CredentialAlreadyExistsException;
import com.innowise.authservice.exception.InvalidCredentialsException;
import com.innowise.authservice.jwt.JwtUtil;
import com.innowise.authservice.repository.CredentialRepository;
import com.innowise.authservice.service.api.AuthService;
import com.innowise.authservice.user.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final CredentialRepository credentialRepo;
    private final UserDetailsServiceImpl userDetailsService;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    @Override
    public AuthResponseDto register(RegistrationDto registrationDto) {
        if (credentialRepo.findCredentialByLogin(registrationDto.getLogin()).isPresent()) {
            throw new CredentialAlreadyExistsException("login", registrationDto.getLogin());
        }

        if (credentialRepo.findByUserId(registrationDto.getUserId()).isPresent()) {
            throw new CredentialAlreadyExistsException("userId", String.valueOf(registrationDto.getUserId()));
        }

        Credential credential = Credential.builder().
                userId(registrationDto.getUserId()).
                login(registrationDto.getLogin()).
                passwordHash(passwordEncoder.encode(registrationDto.getPassword())).
                role(Role.USER).
                build();

        credentialRepo.save(credential);

        CustomUserDetails userDetails = new CustomUserDetails(credential);
        String accessToken = jwtUtil.generateAccessToken(userDetails);
        String refreshToken = jwtUtil.generateRefreshToken(userDetails);

        return new AuthResponseDto(credential.getLogin(), accessToken, refreshToken);
    }

    @Override
    public AuthResponseDto login(LoginDto loginDto) {
        Credential credential = ((CustomUserDetails) userDetailsService.
                loadUserByUsername(loginDto.getLogin())).credential();

        matchPasswordOrThrow(credential.getPasswordHash(), loginDto.getPassword());

        CustomUserDetails userDetails = new CustomUserDetails(credential);
        String accessToken = jwtUtil.generateAccessToken(userDetails);
        String refreshToken = jwtUtil.generateRefreshToken(userDetails);

        return new AuthResponseDto(credential.getLogin(), accessToken, refreshToken);
    }


    @Override
    public RefreshResponseDto refresh(RefreshRequestDto refreshRequestDto) {
        String refreshToken = refreshRequestDto.getRefreshToken();
        String username = jwtUtil.extractUsername(refreshToken, true);

        CustomUserDetails userDetails = (CustomUserDetails) userDetailsService.loadUserByUsername(username);

        if (!jwtUtil.isTokenValid(refreshToken, userDetails, true)) {
            throw new InvalidCredentialsException("Invalid refresh token");
        }

        String newAccessToken = jwtUtil.generateAccessToken(userDetails);
        String newRefreshToken = jwtUtil.generateRefreshToken(userDetails);

        RefreshResponseDto response = new RefreshResponseDto();
        response.setAccessToken(newAccessToken);
        response.setRefreshToken(newRefreshToken);

        return response;
    }

    @Override
    public ValidateResponseDto validateToken(String token) {

        String username = jwtUtil.extractUsername(token, false);
        CustomUserDetails userDetails = (CustomUserDetails) userDetailsService.loadUserByUsername(username);

        if (!jwtUtil.isTokenValid(token, userDetails, false)) {
            throw new InvalidCredentialsException("Invalid refresh token");
        }

        return new ValidateResponseDto(true);
    }

    private void matchPasswordOrThrow(String hashPassword, String password) {
        if (!passwordEncoder.matches(password, hashPassword)) {
            throw new InvalidCredentialsException("Invalid login or password");
        }
    }
}
