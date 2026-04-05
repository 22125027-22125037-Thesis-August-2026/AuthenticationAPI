package com.mhsa.backend.auth.utils;

import java.security.Key;
import java.util.Date;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import com.mhsa.backend.auth.model.Role;

@Component
public class JwtUtils {

    @Value("${mhsa.app.jwtSecret}")
    private String jwtSecret;

    @Value("${mhsa.app.jwtExpirationMs}")
    private int jwtExpirationMs;

    // Lấy Key từ chuỗi Secret trong file properties
    private Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // 1. Generate token with subject = user UUID; keep email, profileId and role as claims.
    public String generateToken(UUID userId, UUID profileId, String email, Role role) {
        return Jwts.builder()
                .setSubject(userId.toString())
                .claim("email", email)
                .claim("profileId", profileId == null ? null : profileId.toString())
                .claim("role", role == null ? null : role.name())
                .setIssuedAt(new Date())
                .setExpiration(new Date((new Date()).getTime() + jwtExpirationMs))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // 2. Extract user UUID string from subject.
    public String getUserIdFromJwtToken(String token) {
        return Jwts.parserBuilder().setSigningKey(getSigningKey()).build()
                .parseClaimsJws(token).getBody().getSubject();
    }

    // 3. Extract email from custom claim.
    public String getEmailFromJwtToken(String token) {
        Object email = Jwts.parserBuilder().setSigningKey(getSigningKey()).build()
                .parseClaimsJws(token).getBody().get("email");
        return email == null ? null : email.toString();
    }

    public UUID getProfileIdFromJwtToken(String token) {
        Object profileId = Jwts.parserBuilder().setSigningKey(getSigningKey()).build()
                .parseClaimsJws(token).getBody().get("profileId");
        return profileId == null ? null : UUID.fromString(profileId.toString());
    }

    public Role getRoleFromJwtToken(String token) {
        Object role = Jwts.parserBuilder().setSigningKey(getSigningKey()).build()
                .parseClaimsJws(token).getBody().get("role");
        return role == null ? null : Role.valueOf(role.toString());
    }

    // 4. Validate token signature and expiration.
    public boolean validateJwtToken(String authToken) {
        try {
            Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(authToken);
            return true;
        } catch (JwtException e) {
            System.err.println("Invalid JWT Token: " + e.getMessage());
        }
        return false;
    }

    public Date getExpirationDateFromToken(String token) {
        return Jwts.parserBuilder().setSigningKey(getSigningKey()).build()
                .parseClaimsJws(token).getBody().getExpiration();
    }
}
