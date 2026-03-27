package com.ct5121.shareit.security;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class JwtService {
    private final JwtTokenProvider jwtTokenProvider;

    public String generateToken(Long userId, String email) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("email", email);
        return jwtTokenProvider.generateToken(claims, email);
    }

    public String extractEmail(String token) {
        return extractAllClaims(token).getSubject();
    }

    public Long extractUserId(String token) {
        Number value = extractAllClaims(token).get("userId", Number.class);
        return value == null ? null : value.longValue();
    }

    public Instant extractExpiration(String token) {
        Date expiration = extractAllClaims(token).getExpiration();
        return expiration == null ? null : expiration.toInstant();
    }

    public boolean isTokenValid(String token, ShareItUserDetails userDetails) {
        String email = extractEmail(token);
        Long userId = extractUserId(token);
        Instant expiration = extractExpiration(token);

        return userDetails.getUsername().equals(email)
                && userDetails.getId().equals(userId)
                && expiration != null
                && expiration.isAfter(Instant.now());
    }

    private Claims extractAllClaims(String token) {
        return jwtTokenProvider.extractAllClaims(token);
    }
}
