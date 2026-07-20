package com.mhsa.backend.tracking.dto;

import java.time.Instant;
import java.util.UUID;

import com.mhsa.backend.tracking.entity.GrantStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DataAccessGrantResponse {

    private UUID grantId;
    private UUID granterProfileId;
    private UUID granteeProfileId;
    private GrantStatus status;
    /** Comma-separated set of category tokens (see {@link com.mhsa.backend.contract.AccessScopes}). */
    private String accessScope;
    private Instant grantedAt;
    private Instant expiresAt;
    private Instant createdAt;
    private Instant updatedAt;
}
