package com.innowise.userservice.security;

import com.innowise.userservice.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Collection;
import java.util.Collections;
import java.util.function.Function;

@Component
@RequiredArgsConstructor
public class JwtUtil {
    private final JwtProperties jwtProperties;

    public String extractUsername(String token, boolean isRefresh) {
        return extractClaim(token, Claims::getSubject, isRefresh);
    }

    public Long extractUserId(String token, boolean isRefresh) {
        return extractClaim(token, claims -> claims.get("userId", Long.class), isRefresh);
    }

    public String extractRole(String token, boolean isRefresh) {
        return extractClaim(token, claims -> claims.get("role", String.class), isRefresh);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver, boolean isRefresh) {
        final Claims claims = extractAllClaims(token, isRefresh);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token, boolean isRefresh) {
        Key key = getKey(isRefresh);
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException | MalformedJwtException e) {
            throw e;
        }
    }

    private Key getKey(boolean isRefresh) {
        String secret = isRefresh ? jwtProperties.getRefresh().getKey() : jwtProperties.getAccess().getKey();
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    public boolean isTokenValid(String token, boolean isRefresh) {
        try {
            extractAllClaims(token, isRefresh);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public Collection<? extends GrantedAuthority> getAuthoritiesFromToken(String token, boolean isRefresh) {
        String role = extractRole(token, isRefresh);
        if (role != null) {
            String authority = role.startsWith("ROLE_") ? role : "ROLE_" + role;
            return Collections.singletonList(new SimpleGrantedAuthority(authority));
        }
        return Collections.emptyList();
    }
}