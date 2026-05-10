package com.mhsa.backend.auth.jwt;

import java.io.Serializable;
import java.security.Principal;
import java.util.UUID;

public record AuthenticatedUserPrincipal(
        UUID userId,
        UUID profileId,
        String email,
        Role role) implements Principal, Serializable {

    @Override
    public String getName() {
        return profileId == null ? null : profileId.toString();
    }
}
