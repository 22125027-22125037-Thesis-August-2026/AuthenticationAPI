package com.mhsa.backend.auth.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    /**
     * Legacy alias for the access token, kept so existing clients keep working during rollout.
     * New clients should read {@link #accessToken}. Remove once all clients are migrated.
     */
    private String token;

    private String accessToken;
    private String refreshToken;

    @Builder.Default
    private String tokenType = "Bearer";

    /** Access-token lifetime in seconds; lets clients refresh proactively before expiry. */
    private long expiresIn;

    private UUID profileId;
    private String email;
    private String role;
}
