package com.mhsa.backend.auth.repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.mhsa.backend.auth.model.RefreshToken;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

    Optional<RefreshToken> findByTokenHash(String tokenHash);

    /**
     * Revoke every still-active token for a user. Used on reuse detection (suspected theft)
     * and for "log out of all devices".
     */
    @Modifying
    @Query("UPDATE RefreshToken t SET t.revokedAt = :now "
            + "WHERE t.user.id = :userId AND t.revokedAt IS NULL")
    int revokeAllForUser(@Param("userId") UUID userId, @Param("now") Instant now);

    /** Purge rows whose rolling expiry has passed; revoked-but-unexpired rows are kept for audit until they expire. */
    @Modifying
    @Query("DELETE FROM RefreshToken t WHERE t.expiresAt < :cutoff")
    int deleteExpired(@Param("cutoff") Instant cutoff);
}
