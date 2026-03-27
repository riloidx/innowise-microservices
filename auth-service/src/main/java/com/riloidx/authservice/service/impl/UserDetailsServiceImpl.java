package com.riloidx.authservice.service.impl;


import com.riloidx.authservice.entity.Credential;
import com.riloidx.authservice.exception.CredentialNotFoundException;
import com.riloidx.authservice.repository.CredentialRepository;
import com.riloidx.authservice.user.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {
    private final CredentialRepository credentialRepository;

    @Override
    public UserDetails loadUserByUsername(String login) {
        log.debug("Loading user details for login: {}", login);
        
        Credential credential = credentialRepository.findCredentialByLogin(login).
                orElseThrow(() -> {
                    log.warn("Credential not found for login: {}", login);
                    return new CredentialNotFoundException("Credential with login=" + login + " not found");
                });

        log.debug("User details loaded successfully for login: {}", login);
        return new CustomUserDetails(credential);
    }
}
