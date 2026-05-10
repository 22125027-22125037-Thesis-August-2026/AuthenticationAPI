package com.mhsa.backend.auth.dto;

import java.time.Instant;
import java.util.UUID;

import com.mhsa.backend.auth.model.AccessScope;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class GrantAccessRequest {

    @NotNull
    private UUID granteeProfileId;

    @NotNull
    private AccessScope accessScope;

    @Future
    private Instant expiresAt;
}
