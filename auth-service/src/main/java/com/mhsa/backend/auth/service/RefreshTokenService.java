package com.mhsa.backend.auth.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mhsa.backend.auth.model.Profile;
import com.mhsa.backend.auth.model.RefreshToken;
import com.mhsa.backend.auth.repository.RefreshTokenRepository;

import lombok.RequiredArgsConstructor;

/**
 * Issues, rotates, and revokes opaque refresh tokens. The raw token is a 256-bit random value
 * returned to the caller once; only its SHA-256 hash is persisted. Rotation invalidates the
 * presented token and issues a successor, and presenting an already-used token revokes the
 * profile's whole token set (reuse / theft detection).
 */
@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private static final Logger log = LoggerFactory.getLogger(RefreshTokenService.class);
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int RAW_TOKEN_BYTES = 32; // 256 bits of entropy

    private final RefreshTokenRepository repository;

    /** Rolling lifetime of a refresh token, extended on each rotation. Default 30 days. */
    @Value("${mhsa.app.jwtRefreshExpirationMs:2592000000}")
    private long refreshExpirationMs;

    /** Hard cap on a session regardless of activity. Default 180 days; set &lt;= 0 to disable. */
    @Value("${mhsa.app.jwtRefreshAbsoluteMs:15552000000}")
    private long refreshAbsoluteMs;

    /** The raw token (shown to the client once) paired with its persisted row. */
    public record IssuedToken(String rawToken, RefreshToken entity) {}

    /** Outcome of a rotation: the owning profile plus the freshly issued token. */
    public record RotationResult(Profile profile, String rawToken, RefreshToken entity) {}

    /**
     * Issue a brand-new refresh token for {@code profile} (called at login). Starts a fresh
     * rolling window and a fresh absolute cap.
     */
    @Transactional
    public IssuedToken issue(Profile profile, String deviceLabel) {
        Instant now = Instant.now();
        String raw = generateRawToken();

        RefreshToken token = RefreshToken.builder()
                .profile(profile)
                .tokenHash(hash(raw))
                .deviceLabel(deviceLabel)
                .createdAt(now)
                .expiresAt(now.plusMillis(refreshExpirationMs))
                .absoluteExpiresAt(refreshAbsoluteMs > 0 ? now.plusMillis(refreshAbsoluteMs) : null)
                .build();

        repository.save(token);
        return new IssuedToken(raw, token);
    }

    /**
     * Validate and rotate the presented refresh token. The old token is revoked and a successor
     * is issued, preserving the original absolute cap. Throws {@link InvalidRefreshTokenException}
     * if the token is unknown, expired, or already used.
     */
    @Transactional
    public RotationResult rotate(String rawToken, String deviceLabel) {
        RefreshToken current = repository.findByTokenHash(hash(rawToken))
                .orElseThrow(() -> new InvalidRefreshTokenException("Refresh token not recognized"));

        Instant now = Instant.now();

        // Reuse detection: a revoked token being presented again means either a normal replay
        // or a stolen token. Either way, invalidate the whole family so the attacker is locked out.
        if (current.getRevokedAt() != null) {
            int revoked = repository.revokeAllForProfile(current.getProfile().getId(), now);
            log.warn("Refresh token reuse detected for profile {}; revoked {} active token(s)",
                    current.getProfile().getId(), revoked);
            throw new InvalidRefreshTokenException("Refresh token already used");
        }

        if (current.getExpiresAt().isBefore(now)
                || (current.getAbsoluteExpiresAt() != null && current.getAbsoluteExpiresAt().isBefore(now))) {
            current.setRevokedAt(now);
            repository.save(current);
            throw new InvalidRefreshTokenException("Refresh token expired");
        }

        String raw = generateRawToken();
        Instant rollingExpiry = now.plusMillis(refreshExpirationMs);
        Instant absolute = current.getAbsoluteExpiresAt();
        if (absolute != null && rollingExpiry.isAfter(absolute)) {
            rollingExpiry = absolute; // never roll past the hard cap
        }

        RefreshToken replacement = RefreshToken.builder()
                .profile(current.getProfile())
                .tokenHash(hash(raw))
                .deviceLabel(deviceLabel != null ? deviceLabel : current.getDeviceLabel())
                .createdAt(now)
                .expiresAt(rollingExpiry)
                .absoluteExpiresAt(absolute)
                .build();
        repository.save(replacement);

        current.setRevokedAt(now);
        current.setReplacedBy(replacement.getId());
        repository.save(current);

        return new RotationResult(current.getProfile(), raw, replacement);
    }

    /** Revoke a single token (logout on one device). No-op if unknown or already revoked. */
    @Transactional
    public void revoke(String rawToken) {
        repository.findByTokenHash(hash(rawToken)).ifPresent(token -> {
            if (token.getRevokedAt() == null) {
                token.setRevokedAt(Instant.now());
                repository.save(token);
            }
        });
    }

    /** Revoke every active token for a profile (logout of all devices / forced sign-out). */
    @Transactional
    public int revokeAllForProfile(java.util.UUID profileId) {
        return repository.revokeAllForProfile(profileId, Instant.now());
    }

    private String generateRawToken() {
        byte[] bytes = new byte[RAW_TOKEN_BYTES];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hash(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashed);
        } catch (NoSuchAlgorithmException e) {
            // SHA-256 is mandated by the JLS; this can't happen on a conformant JVM.
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
