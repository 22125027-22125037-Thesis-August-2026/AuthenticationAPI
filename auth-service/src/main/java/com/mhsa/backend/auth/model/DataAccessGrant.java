package com.mhsa.backend.auth.model;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
// accessScope is now a comma-separated set of category tokens (see contract.AccessScopes),
// stored as text so one grant can carry a subset of tracking categories.
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "data_access_grants")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DataAccessGrant {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "grant_id")
    private UUID grantId;

    // Scalar UUID — no cross-domain @ManyToOne
    @Column(name = "granter_profile_id", nullable = false)
    private UUID granterProfileId;

    @Column(name = "grantee_profile_id", nullable = false)
    private UUID granteeProfileId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private GrantStatus status;

    @Column(name = "access_scope", nullable = false, length = 100)
    private String accessScope;

    @Column(name = "granted_at", nullable = false)
    private Instant grantedAt;

    @Column(name = "expires_at")
    private Instant expiresAt;
}
