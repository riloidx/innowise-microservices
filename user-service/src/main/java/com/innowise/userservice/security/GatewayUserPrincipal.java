package com.innowise.userservice.security;

import java.security.Principal;

public record GatewayUserPrincipal(Long id, String role) implements Principal {
    @Override
    public String getName() {
        return String.valueOf(id);
    }
}