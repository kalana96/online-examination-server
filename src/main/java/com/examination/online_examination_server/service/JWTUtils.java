package com.examination.online_examination_server.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.JwtException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.function.Function;
import java.security.Key;

@Component
public class JWTUtils {
    private  static  final long EXPIRATION_TIME = 86400000;  //24 hours
    private static final long REFRESH_EXPIRATION_TIME = 604800000; // 7 days
    private final SecretKey key;
//    private SecretKey key;
    public JWTUtils(@Value("${jwt.secret:}") String jwtSecret) {
        if (jwtSecret == null || jwtSecret.trim().isEmpty()) {
            // Generate a secure random key if none provided
            this.key = generateSecureKey();
        } else {
            // Use provided secret (should be Base64 encoded)
            byte[] keyBytes = Base64.getDecoder().decode(jwtSecret);
            this.key = new SecretKeySpec(keyBytes, "HmacSHA256");
        }
    }

    private SecretKey generateSecureKey() {
        // Generate a secure 256-bit key for HMAC-SHA256
        byte[] key = new byte[32];
        new SecureRandom().nextBytes(key);
        return new SecretKeySpec(key, "HmacSHA256");
    }

    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    public String generateToken(HashMap<String, Object> extraClaims, UserDetails userDetails) {
        return Jwts.builder()
                .claims(extraClaims)
                .subject(userDetails.getUsername())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(key)
                .compact();
    }

    public String generateRefreshToken(HashMap<String, Object> claims, UserDetails userDetails) {
        return Jwts.builder()
                .claims(claims)
                .subject(userDetails.getUsername())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + REFRESH_EXPIRATION_TIME))
                .signWith(key)
                .compact();
    }

    public String extractUsername(String token) {
        return extractClaims(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaims(token, Claims::getExpiration);
    }

    private <T> T extractClaims(String token, Function<Claims, T> claimsTFunction) {
        try {
            final Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return claimsTFunction.apply(claims);
        } catch (JwtException e) {
            throw new RuntimeException("Invalid JWT token", e);
        }
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            final String username = extractUsername(token);
            return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }




}
