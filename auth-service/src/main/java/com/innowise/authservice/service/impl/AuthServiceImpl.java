package com.innowise.authservice.service.impl;

import com.innowise.authservice.dto.request.LoginDto;
import com.innowise.authservice.dto.request.RefreshRequestDto;
import com.innowise.authservice.dto.request.RegistrationDto;
import com.innowise.authservice.dto.request.UserCreateDto;
import com.innowise.authservice.dto.response.AuthResponseDto;
import com.innowise.authservice.dto.response.RefreshResponseDto;
import com.innowise.authservice.dto.response.UserResponseDto;
import com.innowise.authservice.dto.response.ValidateResponseDto;
import com.innowise.authservice.entity.Credential;
import com.innowise.authservice.enums.Role;
import com.innowise.authservice.exception.CredentialAlreadyExistsException;
import com.innowise.authservice.exception.InvalidCredentialsException;
import com.innowise.authservice.jwt.JwtUtil;
import com.innowise.authservice.repository.CredentialRepository;
import com.innowise.authservice.service.api.AuthService;
import com.innowise.authservice.service.api.UserFeignClient;
import com.innowise.authservice.user.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final CredentialRepository credentialRepo;
    private final UserDetailsServiceImpl userDetailsService;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final UserFeignClient userFeignClient;

    @Override
    @Transactional
    public AuthResponseDto register(RegistrationDto registrationDto) {
        log.info("Starting user registration for login: {}", registrationDto.getLogin());
        
        if (credentialRepo.findCredentialByLogin(registrationDto.getLogin()).isPresent()) {
            log.warn("Registration failed: login already exists - {}", registrationDto.getLogin());
            throw new CredentialAlreadyExistsException("login", registrationDto.getLogin());
        }

        UserCreateDto userCreateDto = UserCreateDto.builder()
                .name(registrationDto.getName())
                .email(registrationDto.getEmail())
                .surname(registrationDto.getSurname())
                .birthDate(registrationDto.getBirthDate())
                .build();

        UserResponseDto savedUser = null;
        try {
            log.debug("Creating user in user-service for login: {}", registrationDto.getLogin());
            savedUser = userFeignClient.createUser(userCreateDto);
            log.debug("User created successfully with ID: {}", savedUser.getId());

            Credential credential = Credential.builder()
                    .userId(savedUser.getId())
                    .login(registrationDto.getLogin())
                    .passwordHash(passwordEncoder.encode(registrationDto.getPassword()))
                    .role(Role.USER)
                    .build();

            credentialRepo.save(credential);
            log.debug("Credentials saved for user ID: {}", savedUser.getId());

            CustomUserDetails userDetails = new CustomUserDetails(credential);
            String accessToken = jwtUtil.generateAccessToken(userDetails);
            String refreshToken = jwtUtil.generateRefreshToken(userDetails);

            log.info("User registration completed successfully for login: {}", registrationDto.getLogin());
            return new AuthResponseDto(credential.getLogin(), accessToken, refreshToken);
        } catch (Exception e) {
            log.error("Registration failed for login: {}. Error: {}", registrationDto.getLogin(), e.getMessage());

            if (savedUser != null) {
                try {
                    log.debug("Rolling back user creation for user ID: {}", savedUser.getId());
                    userFeignClient.deleteUser(savedUser.getId());
                } catch (Exception ignored) {
                    log.warn("Failed to rollback user creation for user ID: {}", savedUser.getId());
                }
            }
            throw new RuntimeException("Registration failed: " + e.getMessage(), e);
        }
    }

    @Override
    public AuthResponseDto login(LoginDto loginDto) {
        log.info("User login attempt for login: {}", loginDto.getLogin());
        
        Credential credential = ((CustomUserDetails) userDetailsService.
                loadUserByUsername(loginDto.getLogin())).credential();

        matchPasswordOrThrow(credential.getPasswordHash(), loginDto.getPassword());

        CustomUserDetails userDetails = new CustomUserDetails(credential);
        String accessToken = jwtUtil.generateAccessToken(userDetails);
        String refreshToken = jwtUtil.generateRefreshToken(userDetails);

        log.info("User logged in successfully: {}", loginDto.getLogin());
        return new AuthResponseDto(credential.getLogin(), accessToken, refreshToken);
    }


    @Override
    public RefreshResponseDto refresh(RefreshRequestDto refreshRequestDto) {
        log.info("Token refresh attempt");
        
        String refreshToken = refreshRequestDto.getRefreshToken();
        String username = jwtUtil.extractUsername(refreshToken, true);
        log.debug("Refreshing token for user: {}", username);

        CustomUserDetails userDetails = (CustomUserDetails) userDetailsService.loadUserByUsername(username);

        if (!jwtUtil.isTokenValid(refreshToken, userDetails, true)) {
            log.warn("Invalid refresh token for user: {}", username);
            throw new InvalidCredentialsException("Invalid refresh token");
        }

        String newAccessToken = jwtUtil.generateAccessToken(userDetails);
        String newRefreshToken = jwtUtil.generateRefreshToken(userDetails);

        RefreshResponseDto response = new RefreshResponseDto();
        response.setAccessToken(newAccessToken);
        response.setRefreshToken(newRefreshToken);

        log.info("Token refreshed successfully for user: {}", username);
        return response;
    }

    @Override
    public ValidateResponseDto validateToken(String token) {
        log.debug("Validating token");

        String username = jwtUtil.extractUsername(token, false);
        CustomUserDetails userDetails = (CustomUserDetails) userDetailsService.loadUserByUsername(username);

        if (!jwtUtil.isTokenValid(token, userDetails, false)) {
            log.warn("Token validation failed for user: {}", username);
            throw new InvalidCredentialsException("Invalid refresh token");
        }

        log.debug("Token validated successfully for user: {}", username);
        return new ValidateResponseDto(true);
    }

    private void matchPasswordOrThrow(String hashPassword, String password) {
        if (!passwordEncoder.matches(password, hashPassword)) {
            log.warn("Password mismatch during authentication");
            throw new InvalidCredentialsException("Invalid login or password");
        }
    }
}
