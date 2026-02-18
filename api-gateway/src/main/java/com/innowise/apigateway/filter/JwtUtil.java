package com.innowise.apigateway.filter;


import com.innowise.apigateway.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.security.Key;
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
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key getKey(boolean isRefresh) {
        String secret = isRefresh ? jwtProperties.getRefreshKey() : jwtProperties.getAccessKey();
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
}