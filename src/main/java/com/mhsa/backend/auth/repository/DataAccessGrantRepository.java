package com.mhsa.backend.auth.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.mhsa.backend.auth.model.DataAccessGrant;
import com.mhsa.backend.auth.model.GrantStatus;

public interface DataAccessGrantRepository extends JpaRepository<DataAccessGrant, UUID> {

    // Used by hasDelegatedAccess — finds a live ACTIVE grant between two profiles.
    @Query("""
            SELECT g FROM DataAccessGrant g
            WHERE g.granterProfileId = :granterProfileId
              AND g.granteeProfileId = :granteeProfileId
              AND g.status = 'ACTIVE'
              AND (g.expiresAt IS NULL OR g.expiresAt > :now)
            """)
    Optional<DataAccessGrant> findActiveGrant(
            @Param("granterProfileId") UUID granterProfileId,
            @Param("granteeProfileId") UUID granteeProfileId,
            @Param("now") Instant now);

    // Used by the controller to list all grants issued by a profile.
    List<DataAccessGrant> findByGranterProfileIdAndStatus(UUID granterProfileId, GrantStatus status);

    // Used by revokeAccess to locate the grant to mark as REVOKED.
    Optional<DataAccessGrant> findByGranterProfileIdAndGranteeProfileId(
            UUID granterProfileId, UUID granteeProfileId);
}
