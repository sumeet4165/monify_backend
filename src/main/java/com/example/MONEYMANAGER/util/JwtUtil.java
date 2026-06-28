package com.example.MONEYMANAGER.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtil {

    @Value("${jwt.secretKey}")
    private String jwtSecretKey;

    private SecretKey getSecretKey() {
        return Keys.hmacShaKeyFor(jwtSecretKey.getBytes(StandardCharsets.UTF_8));
    }

    // Generate token
    public String generateAccessToken(String email) {
        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 100 * 60 * 1000)) // 100 min
                .signWith(getSecretKey())
                .compact();
    }

    // Extract email from token
    public String getUsernameFromToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSecretKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return claims.getSubject();
        } catch (Exception e) {
            System.out.println("Invalid JWT: " + e.getMessage());
            return null;
        }
    }

    // Validate token
    public boolean isTokenValid(String token, String email) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSecretKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            String username = claims.getSubject();
            Date expiration = claims.getExpiration();

            System.out.println("Token username: " + username);
            System.out.println("Token expiration: " + expiration);
            System.out.println("Current time: " + new Date());

            return username.trim().equalsIgnoreCase(email.trim()) && !expiration.before(new Date());
        } catch (ExpiredJwtException e) {
            System.out.println("JWT expired: " + e.getMessage());
            return false;
        } catch (Exception e) {
            System.out.println("Invalid JWT: " + e.getMessage());
            return false;
        }
    }
}
