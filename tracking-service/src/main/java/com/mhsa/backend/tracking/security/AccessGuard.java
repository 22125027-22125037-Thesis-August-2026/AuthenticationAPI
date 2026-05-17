package com.mhsa.backend.tracking.security;

import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import com.mhsa.backend.auth.jwt.AuthenticatedUserPrincipal;
import com.mhsa.backend.tracking.service.DataAccessGrantService;

import lombok.RequiredArgsConstructor;

@Component("accessGuard")
@RequiredArgsConstructor
public class AccessGuard {

    private final DataAccessGrantService dataAccessGrantService;

    public boolean canReadTrackingData(Authentication auth, UUID profileId) {
        if (auth == null || auth.getPrincipal() == null) {
            return false;
        }

        Object principal = auth.getPrincipal();
        if (principal instanceof AuthenticatedUserPrincipal authenticatedPrincipal) {
            UUID currentProfileId = authenticatedPrincipal.profileId();
            if (currentProfileId != null) {
                if (currentProfileId.equals(profileId)) {
                    return true;
                }
                return dataAccessGrantService.hasDelegatedAccess(profileId, currentProfileId);
            }
        }

        return false;
    }

    public boolean canManageGrants(Authentication auth, UUID profileId) {
        if (auth == null || auth.getPrincipal() == null) {
            return false;
        }

        Object principal = auth.getPrincipal();
        if (principal instanceof AuthenticatedUserPrincipal authenticatedPrincipal) {
            return authenticatedPrincipal.profileId() != null && authenticatedPrincipal.profileId().equals(profileId);
        }

        return false;
    }
}
