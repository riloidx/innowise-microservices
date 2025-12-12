package com.innowise.authservice.jwt;


import com.innowise.authservice.config.JwtProperties;
import com.innowise.authservice.user.CustomUserDetails;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
@RequiredArgsConstructor
public class JwtUtil {
    private final JwtProperties jwtProperties;

    public String extractUsername(String token, boolean isRefresh) {
        return extractClaim(token, Claims::getSubject, isRefresh);
    }

    public Date extractExpiration(String token, boolean isRefresh) {
        return extractClaim(token, Claims::getExpiration, isRefresh);
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
        String secret = isRefresh ? jwtProperties.getRefresh().getKey() : jwtProperties.getAccess().getKey();

        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    public String generateAccessToken(CustomUserDetails customUserDetails) {
        return generateToken(new HashMap<>(), customUserDetails, false);
    }

    public String generateRefreshToken(CustomUserDetails customUserDetails) {
        return generateToken(new HashMap<>(), customUserDetails, true);
    }

    private String generateToken(Map<String, Object> extraClaims, CustomUserDetails customUserDetails, boolean isRefresh) {
        long expiration = isRefresh ? jwtProperties.getRefresh().getExpirationMs()
                : jwtProperties.getAccess().getExpirationMs();
        Key key = getKey(isRefresh);

        extraClaims.put("userId", customUserDetails.credential().getUserId());
        if (!isRefresh) {
            extraClaims.put("role", customUserDetails.credential().getRole().name());
        }

        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(customUserDetails.getUsername())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(key)
                .compact();
    }

    public boolean isTokenValid(String token, UserDetails userDetails, boolean isRefresh) {
        String username = extractUsername(token, isRefresh);

        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token, isRefresh));
    }

    private boolean isTokenExpired(String token, boolean isRefresh) {
        return extractExpiration(token, isRefresh).before(new Date());
    }
}
