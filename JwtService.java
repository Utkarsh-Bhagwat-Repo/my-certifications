package com.healthtrack360.security;

import com.healthtrack360.domain.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Map;

@Service
public class JwtService {

    private final Key key;
    private final long expirationMinutes;
    private final long refreshExpirationDays;

    public JwtService(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration-minutes}") long expirationMinutes,
            @Value("${jwt.refresh-expiration-days}") long refreshExpirationDays) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
        this.expirationMinutes = expirationMinutes;
        this.refreshExpirationDays = refreshExpirationDays;
    }

    public String generateAccessToken(User user) {
        Instant now = Instant.now();
        Instant expiry = now.plus(expirationMinutes, ChronoUnit.MINUTES);
        return Jwts.builder()
                .setSubject(user.getEmail())
                .addClaims(Map.of("uid", user.getId(), "type", "access"))
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(expiry))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateRefreshToken(User user) {
        Instant now = Instant.now();
        Instant expiry = now.plus(refreshExpirationDays, ChronoUnit.DAYS);
        return Jwts.builder()
                .setSubject(user.getEmail())
                .addClaims(Map.of("uid", user.getId(), "type", "refresh"))
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(expiry))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String extractUsername(String token) {
        return parseClaims(token).getBody().getSubject();
    }

    public boolean isRefreshToken(String token) {
        Claims claims = parseClaims(token).getBody();
        Object type = claims.get("type");
        return "refresh".equals(type);
    }

    public boolean isExpired(String token) {
        Date expiration = parseClaims(token).getBody().getExpiration();
        return expiration.before(new Date());
    }

    private Jws<Claims> parseClaims(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
    }
}
