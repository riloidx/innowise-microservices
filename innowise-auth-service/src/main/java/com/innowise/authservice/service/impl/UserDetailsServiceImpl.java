package com.innowise.authservice.service.impl;


import com.innowise.authservice.entity.Credential;
import com.innowise.authservice.exception.CredentialNotFoundException;
import com.innowise.authservice.repository.CredentialRepository;
import com.innowise.authservice.user.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {
    private final CredentialRepository credentialRepository;

    @Override
    public UserDetails loadUserByUsername(String login) {
        Credential credential = credentialRepository.findCredentialByLogin(login).
                orElseThrow(() -> new CredentialNotFoundException("Credential with login=" + login + " not found"));

        return new CustomUserDetails(credential);
    }
}
