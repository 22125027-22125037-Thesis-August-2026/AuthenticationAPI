package com.mhsa.backend.auth.model;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * A single issued refresh token. Only {@link #tokenHash} (SHA-256 of the raw token) is
 * persisted — the raw token is returned to the client once and never stored.
 *
 * <p>Tokens rotate on every use: the presented row is marked {@link #revokedAt} and points to
 * its successor via {@link #replacedBy}. A token is usable iff it is neither revoked nor past
 * {@link #expiresAt} / {@link #absoluteExpiresAt}.
 */
@Entity
@Table(name = "refresh_tokens")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** SHA-256 hex digest of the raw token; the only representation we keep. */
    @Column(name = "token_hash", nullable = false, unique = true, length = 64)
    private String tokenHash;

    /** Optional human-readable device/client label for a "logged-in devices" UX. */
    @Column(name = "device_label")
    private String deviceLabel;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    /** Rolling expiry, extended on each rotation. */
    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    /** Hard cap on the session, carried across rotations; null means no absolute cap. */
    @Column(name = "absolute_expires_at")
    private Instant absoluteExpiresAt;

    /** Set when the token is rotated, revoked on logout, or invalidated by reuse detection. */
    @Column(name = "revoked_at")
    private Instant revokedAt;

    /** Id of the successor token issued when this one was rotated. */
    @Column(name = "replaced_by")
    private UUID replacedBy;
}
