package com.mhsa.backend.dashboard.security;

import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import com.mhsa.backend.auth.jwt.AuthenticatedUserPrincipal;

@Component("accessGuard")
public class AccessGuard {

    public boolean canReadDashboard(Authentication auth, UUID profileId) {
        if (auth == null || auth.getPrincipal() == null) {
            return false;
        }

        Object principal = auth.getPrincipal();
        if (principal instanceof AuthenticatedUserPrincipal authenticatedPrincipal) {
            UUID currentProfileId = authenticatedPrincipal.profileId();
            if (currentProfileId != null) {
                return currentProfileId.equals(profileId);
            }
        }

        return false;
    }
}
