package com.testetecnico.portoseguros.security.service;

import com.testetecnico.portoseguros.entity.Student;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.SignatureException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.Jws;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

@Component
public class JwtTokenProvider {

    private final SecretKey secretKey;
    private final long expirationMinutes;

    public JwtTokenProvider(
            @Value("${security.jwt.secret:0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF}") String secret,
            @Value("${security.jwt.expiration-minutes:60}") long expirationMinutes) {
        Assert.hasText(secret, "JWT secret must not be empty");
        Assert.isTrue(secret.getBytes(StandardCharsets.UTF_8).length >= 32,
                "JWT secret must be at least 256 bits (32 ASCII characters)");
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMinutes = expirationMinutes;
    }

    public String generateToken(Student student) {
        Instant now = Instant.now();
        Instant expiry = now.plus(expirationMinutes, ChronoUnit.MINUTES);

        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(student.getId().toString())
                .claim("email", student.getEmail())
                .claim("role", student.getRole().name())
                .claim("name", student.getFirstName() + " " + student.getLastName())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean isTokenValid(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (IllegalArgumentException | io.jsonwebtoken.JwtException ex) {
            // Any parse/format/signature issue means the token is not valid
            return false;
        }
    }

    public UUID extractStudentId(String token) {
        Claims claims = parseClaims(token).getPayload();
        return UUID.fromString(claims.getSubject());
    }

    public String extractEmail(String token) {
        Claims claims = parseClaims(token).getPayload();
        return claims.get("email", String.class);
    }

    private io.jsonwebtoken.Jws<Claims> parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token);
    }
}

