package com.mhsa.backend.tracking.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.mhsa.backend.tracking.entity.DataAccessGrant;
import com.mhsa.backend.tracking.entity.GrantStatus;

@Repository
public interface DataAccessGrantRepository extends JpaRepository<DataAccessGrant, UUID> {

    @Query("SELECT g FROM DataAccessGrant g WHERE g.granterProfileId = :granterProfileId "
            + "AND g.granteeProfileId = :granteeProfileId AND g.status = 'ACTIVE' "
            + "AND (g.expiresAt IS NULL OR g.expiresAt > :now)")
    Optional<DataAccessGrant> findActiveGrant(@Param("granterProfileId") UUID granterProfileId,
            @Param("granteeProfileId") UUID granteeProfileId, @Param("now") Instant now);

    List<DataAccessGrant> findByGranterProfileIdAndStatus(UUID granterProfileId, GrantStatus status);

    List<DataAccessGrant> findByGranteeProfileIdAndStatus(UUID granteeProfileId, GrantStatus status);

    Optional<DataAccessGrant> findByGranterProfileIdAndGranteeProfileId(UUID granterProfileId,
            UUID granteeProfileId);
}
