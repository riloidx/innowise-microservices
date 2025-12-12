package com.innowise.userservice.security;

import lombok.Getter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

@Getter
public class CustomAuthenticationToken extends UsernamePasswordAuthenticationToken {

    private final Long userId;

    public CustomAuthenticationToken(String username, Long userId, Collection<? extends GrantedAuthority> authorities) {
        super(username, null, authorities);
        this.userId = userId;
    }
}