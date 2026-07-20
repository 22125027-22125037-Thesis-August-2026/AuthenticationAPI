package com.mhsa.backend.auth.jwt;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import com.mhsa.backend.contract.JwksKey;
import com.mhsa.backend.contract.JwksResponse;

import javax.crypto.SecretKey;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

@Component
public class JwtUtils {

    private static final Logger log = LoggerFactory.getLogger(JwtUtils.class);
    private static final Set<String> SUPPORTED_ALGORITHMS = Set.of("RS256", "HS256");
    private static final Pattern JWT_PATTERN
            = Pattern.compile("([A-Za-z0-9_-]+\\.[A-Za-z0-9_-]+\\.[A-Za-z0-9_-]+)");

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

    /**
     * The previous public key, kept published alongside the current one during a rotation
     * overlap. Optional: unset outside a rotation window.
     */
    @Value("${mhsa.app.jwtPreviousPublicKey:}")
    private String jwtPreviousPublicKey;

    /**
     * Where to fetch the signing key set from. When set, this service resolves RS256
     * verification keys by {@code kid} from Auth's published JWKS instead of from a
     * statically configured public key.
     */
    @Value("${mhsa.app.jwksEndpoint:}")
    private String jwksEndpoint;

    @Value("${mhsa.app.jwtExpirationMs}")
    private int jwtExpirationMs;

    @Value("${mhsa.app.jwtIssuer:mhsa-auth}")
    private String jwtIssuer;

    @Value("${mhsa.app.jwtAudience:mhsa-api}")
    private String jwtAudience;

    @Value("${mhsa.app.jwtSigningKid:mhsa-key-1}")
    private String jwtSigningKid;

    @Value("${mhsa.app.jwtPreviousSigningKid:}")
    private String jwtPreviousSigningKid;

    @Value("${mhsa.app.jwtAllowHs256Fallback:false}")
    private boolean jwtAllowHs256Fallback;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private SigningMode signingMode;
    private SecretKey hmacSigningKey;
    private PrivateKey privateSigningKey;
    private PublicKey publicVerificationKey;
    private PublicKey previousPublicVerificationKey;
    private JwksKeyProvider jwksKeyProvider;

    @PostConstruct
    void initializeKeys() {
        boolean hasPrivate = hasText(jwtPrivateKey);
        boolean hasPublic = hasText(jwtPublicKey);

        if (hasText(jwksEndpoint)) {
            // Verification keys come from Auth's published key set, selected by the token's
            // kid. Nothing is fetched here: the first RS256 token triggers the fetch, so this
            // service still starts when Auth is not up yet.
            jwksKeyProvider = new JwksKeyProvider(jwksEndpoint, objectMapper);
            log.info("JWT verification keys will be resolved from JWKS at {}", jwksEndpoint);
        }

        if (hasPrivate && hasPublic) {
            privateSigningKey = parsePrivateKey(jwtPrivateKey);
            publicVerificationKey = parsePublicKey(jwtPublicKey);
            if (hasText(jwtPreviousPublicKey)) {
                previousPublicVerificationKey = parsePublicKey(jwtPreviousPublicKey);
                log.info("JWT rotation overlap active: also publishing previous key id {}",
                        jwtPreviousSigningKid);
            }
            signingMode = SigningMode.RS256;
            log.info("JWT configured for RS256 signing with key id {}", jwtSigningKid);
            return;
        }

        if (jwksKeyProvider != null && !hasPrivate) {
            // A pure verifier (no signing material of its own). It cannot mint tokens, which
            // is correct for every service except Auth.
            if (hasPublic) {
                log.warn("Both jwksEndpoint and a static jwtPublicKey are configured; "
                        + "the static key is ignored in favour of the published key set");
            }
            signingMode = SigningMode.RS256;
            log.info("JWT configured for RS256 verification only, via JWKS");
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

    /**
     * Issues an access token for a profile. {@code sub} is the profile id — the platform's
     * single account identifier since the users/profiles merge. The redundant {@code profileId}
     * claim is kept so consumers that read it (therapist-api, social, notification) keep
     * working without a coordinated deploy.
     */
    public String generateToken(UUID profileId, String email, Role role) {
        var builder = Jwts.builder()
                .setSubject(profileId.toString())
                .claim("email", email)
                .claim("profileId", profileId.toString())
                .claim("role", role == null ? null : role.name())
                .setIssuer(jwtIssuer)
                .setAudience(jwtAudience)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs));

        if (signingMode == SigningMode.RS256) {
            if (privateSigningKey == null) {
                throw new IllegalStateException(
                        "This service verifies tokens but cannot issue them: no RSA private key is configured.");
            }
            return builder
                    .setHeaderParam("kid", jwtSigningKid)
                    .signWith(privateSigningKey, SignatureAlgorithm.RS256)
                    .compact();
        }

        return builder
                .signWith(hmacSigningKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public String getEmailFromJwtToken(String token) {
        Object email = parseClaims(token).get("email");
        return email == null ? null : email.toString();
    }

    /**
     * Reads the profile id, preferring the explicit {@code profileId} claim (present on all
     * tokens, old and new) and falling back to {@code sub} for tokens that lack it.
     */
    public UUID getProfileIdFromJwtToken(String token) {
        Claims claims = parseClaims(token);
        Object profileId = claims.get("profileId");
        if (profileId != null) {
            return UUID.fromString(profileId.toString());
        }
        return claims.getSubject() == null ? null : UUID.fromString(claims.getSubject());
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
            log.warn("Invalid JWT token: {} (preview={})", e.getMessage(), previewToken(authToken));
        } catch (Exception e) {
            log.warn("Unexpected JWT validation error: {} (preview={})", e.getMessage(), previewToken(authToken));
        }
        return false;
    }

    public Date getExpirationDateFromToken(String token) {
        return parseClaims(token).getExpiration();
    }

    public String resolveBearerToken(String authorizationHeader) {
        if (!hasText(authorizationHeader)) {
            return null;
        }

        String headerValue = authorizationHeader.trim();
        if (!headerValue.regionMatches(true, 0, "Bearer ", 0, 7)) {
            return null;
        }

        String token = headerValue.substring(7).trim();
        token = stripWrappingQuotes(token);

        Matcher matcher = JWT_PATTERN.matcher(token);
        if (matcher.find()) {
            String extracted = matcher.group(1);
            if (!extracted.equals(token)) {
                log.warn("Authorization header token was normalized before validation");
            }
            return extracted;
        }

        if (hasText(token)) {
            log.warn("Authorization Bearer token format is invalid (raw={})", previewToken(token));
        }

        return null;
    }

    /**
     * The public half of the signing key, in JWKS form.
     *
     * <p>During a rotation overlap the set carries two keys: the new one and the key it
     * replaced. Consumers select by {@code kid}, so tokens minted under the old key keep
     * validating until they expire naturally and nobody is forced to log in again. That
     * overlap is the whole reason this is a key <em>set</em> and not a single key.
     */
    public JwksResponse getJwksResponse() {
        if (signingMode != SigningMode.RS256 || publicVerificationKey == null) {
            throw new IllegalStateException("JWKS is only available when using RS256 signing mode");
        }

        List<JwksKey> keys = new ArrayList<>();
        keys.add(toJwksKey(publicVerificationKey, jwtSigningKid));

        if (previousPublicVerificationKey != null && hasText(jwtPreviousSigningKid)) {
            keys.add(toJwksKey(previousPublicVerificationKey, jwtPreviousSigningKid));
        }

        return JwksResponse.builder()
                .keys(keys)
                .build();
    }

    private JwksKey toJwksKey(PublicKey key, String kid) {
        RSAPublicKey rsaKey = (RSAPublicKey) key;
        return JwksKey.builder()
                .keyType("RSA")
                .use("sig")
                .kid(kid)
                .algorithm("RS256")
                .modulus(base64UrlUnsigned(rsaKey.getModulus()))
                .exponent(base64UrlUnsigned(rsaKey.getPublicExponent()))
                .build();
    }

    /**
     * RFC 7518 §6.3.1 wants {@code n} and {@code e} as unsigned big-endian bytes.
     * {@link BigInteger#toByteArray()} is two's-complement, so a positive value whose top
     * bit is set — which an RSA modulus always has — gains a leading {@code 0x00} sign
     * byte. Left in, it yields a 257-byte modulus for a 2048-bit key; Nimbus tolerates
     * that, jose4j and python-jose reject it.
     */
    private static String base64UrlUnsigned(BigInteger value) {
        byte[] bytes = value.toByteArray();
        if (bytes.length > 1 && bytes[0] == 0) {
            bytes = Arrays.copyOfRange(bytes, 1, bytes.length);
        }
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private Claims parseClaims(String token) {
        String algorithm = readAlgorithm(token);
        Claims claims;

        if ("RS256".equals(algorithm)) {
            claims = Jwts.parserBuilder()
                    .setSigningKey(resolveRs256Key(token))
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
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

    /**
     * Picks the key to verify an RS256 token with.
     *
     * <p>In JWKS mode the {@code kid} lookup <em>is</em> the key-id check — a token whose kid
     * is not in the published set never finds a key. In static mode the kid is compared
     * against the one key this service was configured with.
     */
    private PublicKey resolveRs256Key(String token) {
        Object kidHeader = readHeader(token).get("kid");
        String kid = kidHeader == null ? null : kidHeader.toString();

        if (jwksKeyProvider != null) {
            return jwksKeyProvider.resolve(kid);
        }

        if (publicVerificationKey == null) {
            throw new JwtException(
                    "RS256 token received but neither an RSA public key nor a JWKS endpoint is configured");
        }

        if (hasText(jwtSigningKid) && !jwtSigningKid.equals(kid)) {
            if (previousPublicVerificationKey != null && jwtPreviousSigningKid.equals(kid)) {
                return previousPublicVerificationKey;
            }
            throw new JwtException("Unknown JWT key id");
        }

        return publicVerificationKey;
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

    private String stripWrappingQuotes(String value) {
        if (!hasText(value)) {
            return value;
        }

        String normalized = value.trim();
        while (normalized.length() >= 2
                && normalized.startsWith("\"")
                && normalized.endsWith("\"")) {
            normalized = normalized.substring(1, normalized.length() - 1).trim();
        }

        return normalized;
    }

    private String previewToken(String value) {
        if (!hasText(value)) {
            return "<empty>";
        }

        String sanitized = value.replaceAll("\\s+", " ").trim();
        int prefixLength = Math.min(16, sanitized.length());
        return sanitized.substring(0, prefixLength) + "...";
    }
}
