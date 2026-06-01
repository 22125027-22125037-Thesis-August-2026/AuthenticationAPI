package com.mhsa.backend.tracking.entity;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

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

    // Assigned (not generated): the grant_id is authoritative in auth-service. The replica keeps
    // the same id so events/reconciliation can upsert by it idempotently. Locally-created grants
    // (legacy /api/v1/tracking/grants path) assign their own id via the mapper.
    @Id
    @Column(name = "grant_id")
    private UUID grantId;

    @Column(name = "granter_profile_id", nullable = false)
    private UUID granterProfileId;

    @Column(name = "grantee_profile_id", nullable = false)
    private UUID granteeProfileId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private GrantStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "access_scope", nullable = false, length = 20)
    private AccessScope accessScope;

    @Column(name = "granted_at", nullable = false)
    private Instant grantedAt;

    @Column(name = "expires_at")
    private Instant expiresAt;

    // Out-of-order watermark: the occurredAt of the last applied event. An incoming event is
    // applied only when its occurredAt >= this value.
    @Column(name = "last_event_at")
    private Instant lastEventAt;

    @CreationTimestamp
    @Column(name = "created_at")
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;
}
