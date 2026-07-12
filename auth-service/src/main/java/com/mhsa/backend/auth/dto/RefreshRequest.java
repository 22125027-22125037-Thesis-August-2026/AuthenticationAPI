package com.mhsa.backend.auth.dto;

import lombok.Data;

/**
 * Body for {@code POST /api/v1/auth/refresh} and the optional refresh-token revocation on logout.
 */
@Data
public class RefreshRequest {

    private String refreshToken;

    /** Optional client/device label persisted with the rotated token for a "logged-in devices" UX. */
    private String deviceLabel;
}
