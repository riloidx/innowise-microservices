package com.innowise.userservice.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
public class GatewayHeaderFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String userIdStr = request.getHeader("X-User-Id");
        String userRole = request.getHeader("X-User-Role");

        if (userIdStr != null && userRole != null) {
            try {
                UsernamePasswordAuthenticationToken auth = getUsernamePasswordAuthenticationToken(userIdStr, userRole);
                SecurityContextHolder.getContext().setAuthentication(auth);
            } catch (Exception e) {
            }
        }
        filterChain.doFilter(request, response);
    }

    private static UsernamePasswordAuthenticationToken getUsernamePasswordAuthenticationToken(String userIdStr, String userRole) {
        Long userId = Long.parseLong(userIdStr);

        String finalRole = userRole.startsWith("ROLE_") ? userRole : "ROLE_" + userRole;

        GatewayUserPrincipal principal = new GatewayUserPrincipal(userId, finalRole);

        SimpleGrantedAuthority authority = new SimpleGrantedAuthority(finalRole);

        return new UsernamePasswordAuthenticationToken(
                principal,
                null,
                Collections.singletonList(authority)
        );
    }
}