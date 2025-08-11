package com.technican.restservice.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;
import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Set;

@Component
public class JwtUtil {
    private final String jwtSecret = "supersecretkeysupersecretkeysupersecretkey";
    private final long jwtExpirationMs = 86400000; // 1 day
    private final SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes());

    public String generateToken(Long userId, String username, Set<String> roles) {
        return Jwts.builder()
                .setSubject(username)
                .claim("userId", userId)
                .claim("roles", String.join(",", roles))
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(key)
                .compact();
    }

    public Jws<Claims> validateToken(String token) {
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
    }

    public String getUsernameFromToken(String token) {
        Jws<Claims> jwt = validateToken(token);
        return jwt.getPayload().getSubject();
    }

    public Long getUserIdFromToken(String token) {
        Jws<Claims> jwt = validateToken(token);
        return jwt.getPayload().get("userId", Long.class);
    }

    public Set<String> getRolesFromToken(String token) {
        Jws<Claims> jwt = validateToken(token);
        String roles = jwt.getPayload().get("roles", String.class);
        Set<String> roleSet = Set.of(roles.split(","));
        return roleSet;
    }
}
