package com.mhsa.backend.tracking.dto;

import java.time.Instant;
import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DataAccessGrantRequest {

    @NotNull(message = "granteeProfileId is required")
    private UUID granteeProfileId;

    /** Comma-separated set of category tokens (see {@link com.mhsa.backend.contract.AccessScopes}). */
    @NotNull(message = "accessScope is required")
    private String accessScope;

    private Instant expiresAt;
}
