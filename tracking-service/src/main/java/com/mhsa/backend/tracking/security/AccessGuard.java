package com.mhsa.backend.tracking.security;

import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import com.mhsa.backend.auth.jwt.AuthenticatedUserPrincipal;

@Component("accessGuard")
public class AccessGuard {

    public boolean canReadTrackingData(Authentication auth, UUID profileId) {
        if (auth == null || auth.getPrincipal() == null) {
            return false;
        }

        Object principal = auth.getPrincipal();
        if (principal instanceof AuthenticatedUserPrincipal authenticatedPrincipal) {
            if (authenticatedPrincipal.profileId() != null) {
                return authenticatedPrincipal.profileId().equals(profileId);
            }
        }

        return false;
    }
}
