package com.fooddelivery.userservice.config;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }


    public String generateToken(Long userId, String email, String role) {
        return Jwts.builder()
                .setSubject(email)
                .claim("userId", userId)
                .claim("role", role)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256) // header
                .compact(); // ← THIS builds + signs the entire token
    }

    public String extractEmail(String token) {
        return parseClaims(token).getSubject();
    }

    public String extractRole(String token) {
        return parseClaims(token).get("role", String.class);
    }

    public Long extractUserId(String token) {
        return parseClaims(token).get("userId", Long.class);
    }


    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;// ← expired token throws ExpiredJwtException
        }
    }

    private Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}






























/*

| Method                     | What it does                                  | Used where              |
| -------------------------- | --------------------------------------------- | ----------------------- |
| getSigningKey()            | Converts secret string to secure Key object   | Internally              |
| generateToken(email, role) | Creates JWT token with email + role inside    | After login/register    |
| extractEmail(token)        | Reads email from token                        | Security filter         |
| extractRole(token)         | Reads role from token                         | Authorization checks    |
| validateToken(token)       | Checks if token is valid and not expired      | Every protected request |
| parseClaims(token)         | Decodes the token and returns all data inside | Internally              |
 */