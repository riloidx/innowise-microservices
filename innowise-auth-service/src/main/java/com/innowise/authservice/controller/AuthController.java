package com.innowise.authservice.controller;


import com.innowise.authservice.dto.request.LoginDto;
import com.innowise.authservice.dto.request.RefreshRequestDto;
import com.innowise.authservice.dto.request.RegistrationDto;
import com.innowise.authservice.dto.response.AuthResponseDto;
import com.innowise.authservice.dto.response.RefreshResponseDto;
import com.innowise.authservice.dto.response.ValidateResponseDto;
import com.innowise.authservice.service.api.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/registration")
    public ResponseEntity<AuthResponseDto> registration(@Valid @RequestBody RegistrationDto registrationDto) {
        AuthResponseDto response = authService.register(registrationDto);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDto> login(@Valid @RequestBody LoginDto loginDto) {
        AuthResponseDto response = authService.login(loginDto);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
    
    @PostMapping("/refresh")
    public ResponseEntity<RefreshResponseDto> refresh(@Valid @RequestBody RefreshRequestDto refreshRequestDto) {
        RefreshResponseDto response = authService.refresh(refreshRequestDto);
        
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
    
    @PostMapping("/validate")
    public ResponseEntity<ValidateResponseDto> validateToken(@RequestParam String token) {
        ValidateResponseDto response = authService.validateToken(token);
        
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
