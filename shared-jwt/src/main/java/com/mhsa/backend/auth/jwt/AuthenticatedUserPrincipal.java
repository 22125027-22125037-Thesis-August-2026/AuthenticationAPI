package com.mhsa.backend.auth.jwt;

import java.io.Serializable;
import java.security.Principal;
import java.util.UUID;

/**
 * The authenticated identity extracted from a JWT. Since the users/profiles merge the
 * profile id is the single account identifier across every service; the token's
 * {@code sub} claim carries it (older tokens also carry a redundant {@code profileId} claim).
 */
public record AuthenticatedUserPrincipal(
        UUID profileId,
        String email,
        Role role) implements Principal, Serializable {

    @Override
    public String getName() {
        return profileId == null ? null : profileId.toString();
    }
}
