package com.mhsa.backend.auth.utils;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mhsa.backend.auth.model.Role;

import javax.crypto.SecretKey;
import java.security.KeyFactory;

@Component
public class JwtUtils {

    private static final Logger log = LoggerFactory.getLogger(JwtUtils.class);
    private static final Set<String> SUPPORTED_ALGORITHMS = Set.of("RS256", "HS256");

    private enum SigningMode {
        RS256,
        HS256
    }

    @Value("${mhsa.app.jwtSecret:}")
    private String jwtSecret;

    @Value("${mhsa.app.jwtPrivateKey:}")
    private String jwtPrivateKey;

    @Value("${mhsa.app.jwtPublicKey:}")
    private String jwtPublicKey;

    @Value("${mhsa.app.jwtExpirationMs}")
    private int jwtExpirationMs;

    @Value("${mhsa.app.jwtIssuer:mhsa-auth}")
    private String jwtIssuer;

    @Value("${mhsa.app.jwtAudience:mhsa-api}")
    private String jwtAudience;

    @Value("${mhsa.app.jwtSigningKid:mhsa-key-1}")
    private String jwtSigningKid;

    @Value("${mhsa.app.jwtAllowHs256Fallback:true}")
    private boolean jwtAllowHs256Fallback;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private SigningMode signingMode;
    private SecretKey hmacSigningKey;
    private PrivateKey privateSigningKey;
    private PublicKey publicVerificationKey;

    @PostConstruct
    void initializeKeys() {
        boolean hasPrivate = hasText(jwtPrivateKey);
        boolean hasPublic = hasText(jwtPublicKey);

        if (hasPrivate && hasPublic) {
            privateSigningKey = parsePrivateKey(jwtPrivateKey);
            publicVerificationKey = parsePublicKey(jwtPublicKey);
            signingMode = SigningMode.RS256;
            log.info("JWT configured for RS256 signing with key id {}", jwtSigningKid);
            return;
        }

        if (hasPrivate || hasPublic) {
            log.warn("Incomplete RSA key configuration detected (private={}, public={}), falling back to HS256 if available",
                    hasPrivate,
                    hasPublic);
        }

        if (!hasText(jwtSecret)) {
            throw new IllegalStateException(
                    "No JWT signing material configured. Provide RSA keys (jwtPrivateKey+jwtPublicKey) or jwtSecret.");
        }

        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        hmacSigningKey = Keys.hmacShaKeyFor(keyBytes);
        signingMode = SigningMode.HS256;
        log.info("JWT configured for HS256 signing");
    }

    public String generateToken(UUID userId, UUID profileId, String email, Role role) {
        var builder = Jwts.builder()
                .setSubject(userId.toString())
                .claim("email", email)
                .claim("profileId", profileId == null ? null : profileId.toString())
                .claim("role", role == null ? null : role.name())
                .setIssuer(jwtIssuer)
                .setAudience(jwtAudience)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs));

        if (signingMode == SigningMode.RS256) {
            return builder
                    .setHeaderParam("kid", jwtSigningKid)
                    .signWith(privateSigningKey, SignatureAlgorithm.RS256)
                    .compact();
        }

        return builder
                .signWith(hmacSigningKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public String getUserIdFromJwtToken(String token) {
        return parseClaims(token).getSubject();
    }

    public String getEmailFromJwtToken(String token) {
        Object email = parseClaims(token).get("email");
        return email == null ? null : email.toString();
    }

    public UUID getProfileIdFromJwtToken(String token) {
        Object profileId = parseClaims(token).get("profileId");
        return profileId == null ? null : UUID.fromString(profileId.toString());
    }

    public Role getRoleFromJwtToken(String token) {
        Object role = parseClaims(token).get("role");
        return role == null ? null : Role.valueOf(role.toString());
    }

    public boolean validateJwtToken(String authToken) {
        try {
            parseClaims(authToken);
            return true;
        } catch (JwtException e) {
            log.warn("Invalid JWT token: {}", e.getMessage());
        } catch (Exception e) {
            log.warn("Unexpected JWT validation error: {}", e.getMessage());
        }
        return false;
    }

    public Date getExpirationDateFromToken(String token) {
        return parseClaims(token).getExpiration();
    }

    private Claims parseClaims(String token) {
        String algorithm = readAlgorithm(token);
        Claims claims;

        if ("RS256".equals(algorithm)) {
            if (publicVerificationKey == null) {
                throw new JwtException("RS256 token received but RSA public key is not configured");
            }
            claims = Jwts.parserBuilder()
                    .setSigningKey(publicVerificationKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            validateKid(token);
        } else if ("HS256".equals(algorithm)) {
            if (!jwtAllowHs256Fallback) {
                throw new JwtException("HS256 tokens are disabled");
            }
            if (hmacSigningKey == null) {
                throw new JwtException("HS256 token received but HMAC secret is not configured");
            }
            claims = Jwts.parserBuilder()
                    .setSigningKey(hmacSigningKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } else {
            throw new JwtException("Unsupported JWT algorithm: " + algorithm);
        }

        validateStandardClaims(claims);
        return claims;
    }

    private String readAlgorithm(String token) {
        try {
            Map<String, Object> headerMap = readHeader(token);
            Object alg = headerMap.get("alg");

            if (alg == null) {
                throw new JwtException("JWT header is missing alg");
            }

            String algorithm = alg.toString();
            if (!SUPPORTED_ALGORITHMS.contains(algorithm)) {
                throw new JwtException("Unsupported JWT algorithm: " + algorithm);
            }

            return algorithm;
        } catch (JwtException e) {
            throw e;
        } catch (Exception e) {
            throw new JwtException("Unable to parse JWT header", e);
        }
    }

    private void validateKid(String token) {
        if (!hasText(jwtSigningKid)) {
            return;
        }

        Object kidHeader = readHeader(token).get("kid");
        String kid = kidHeader == null ? null : kidHeader.toString();

        if (!jwtSigningKid.equals(kid)) {
            throw new JwtException("Unknown JWT key id");
        }
    }

    private Map<String, Object> readHeader(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                throw new JwtException("JWT format is invalid");
            }

            byte[] headerBytes = Decoders.BASE64URL.decode(parts[0]);
            @SuppressWarnings("unchecked")
            Map<String, Object> headerMap = objectMapper.readValue(headerBytes, Map.class);
            return headerMap;
        } catch (JwtException e) {
            throw e;
        } catch (Exception e) {
            throw new JwtException("Unable to parse JWT header", e);
        }
    }

    private void validateStandardClaims(Claims claims) {
        if (!jwtIssuer.equals(claims.getIssuer())) {
            throw new JwtException("Invalid JWT issuer");
        }

        if (!jwtAudience.equals(claims.getAudience())) {
            throw new JwtException("Invalid JWT audience");
        }
    }

    private PrivateKey parsePrivateKey(String keyValue) {
        try {
            byte[] keyBytes = decodeKeyMaterial(keyValue);
            var keySpec = new PKCS8EncodedKeySpec(keyBytes);
            return KeyFactory.getInstance("RSA").generatePrivate(keySpec);
        } catch (Exception e) {
            throw new IllegalStateException("Invalid JWT private key configuration", e);
        }
    }

    private PublicKey parsePublicKey(String keyValue) {
        try {
            byte[] keyBytes = decodeKeyMaterial(keyValue);
            var keySpec = new X509EncodedKeySpec(keyBytes);
            return KeyFactory.getInstance("RSA").generatePublic(keySpec);
        } catch (Exception e) {
            throw new IllegalStateException("Invalid JWT public key configuration", e);
        }
    }

    private byte[] decodeKeyMaterial(String keyValue) {
        String normalized = keyValue
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s", "");

        return Decoders.BASE64.decode(normalized);
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
