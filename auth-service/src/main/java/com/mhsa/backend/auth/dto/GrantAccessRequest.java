package com.mhsa.backend.auth.dto;

import java.time.Instant;
import java.util.UUID;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class GrantAccessRequest {

    @NotNull
    private UUID granteeProfileId;

    /**
     * A comma-separated set of tracking-category tokens the grantee may read — e.g.
     * {@code "READ_SLEEP,READ_FOOD"} or the {@code "READ_ALL"} shorthand (see
     * {@link com.mhsa.backend.contract.AccessScopes}). Token validity is checked in the service.
     */
    @NotNull
    private String accessScope;

    @Future
    private Instant expiresAt;
}
