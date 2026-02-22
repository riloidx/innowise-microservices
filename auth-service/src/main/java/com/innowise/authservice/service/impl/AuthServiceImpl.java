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
import com.innowise.authservice.exception.CredentialAlreadyExistsException;
import com.innowise.authservice.exception.InvalidCredentialsException;
import com.innowise.authservice.jwt.JwtUtil;
import com.innowise.authservice.repository.CredentialRepository;
import com.innowise.authservice.service.api.AuthService;
import com.innowise.authservice.service.api.CredentialService;
import com.innowise.authservice.service.api.UserFeignClient;
import com.innowise.authservice.user.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final CredentialRepository credentialRepo;
    private final UserDetailsServiceImpl userDetailsService;
    private final CredentialService credentialService;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final UserFeignClient userFeignClient;

    @Override
    public AuthResponseDto register(RegistrationDto registrationDto) {
        log.info("Starting user registration for login: {}", registrationDto.getLogin());

        if (credentialRepo.existsByLogin(registrationDto.getLogin())) {
            throw new CredentialAlreadyExistsException("login", registrationDto.getLogin());
        }

        UserResponseDto savedUser = createUserInRemoteService(registrationDto);

        try {
            return credentialService.createCredentials(registrationDto, savedUser.getId());
        } catch (Exception e) {
            log.error("Failed to save credentials for userId: {}. Initiating compensation...", savedUser.getId());
            compensateUserCreation(savedUser.getId());
            throw new RuntimeException("Registration failed: " + e.getMessage());
        }
    }

    private UserResponseDto createUserInRemoteService(RegistrationDto registrationDto) {
        UserCreateDto userCreateDto = UserCreateDto.builder()
                .name(registrationDto.getName())
                .email(registrationDto.getEmail())
                .surname(registrationDto.getSurname())
                .birthDate(registrationDto.getBirthDate())
                .build();
        return userFeignClient.createUser(userCreateDto);
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

    private void compensateUserCreation(Long userId) {
        try {
            userFeignClient.deleteUser(userId);
        } catch (Exception e) {

        }
    }
}
