package com.mhsa.backend.tracking.dto;

import java.time.Instant;
import java.util.UUID;

import com.mhsa.backend.tracking.entity.AccessScope;

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

    @NotNull(message = "accessScope is required")
    private AccessScope accessScope;

    private Instant expiresAt;
}
