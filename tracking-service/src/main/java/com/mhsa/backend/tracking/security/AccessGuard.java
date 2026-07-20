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

    /**
     * Whether the caller may read the owner's data for a specific tracking category.
     *
     * <p>Allowed when the caller <em>is</em> the owner, or holds an ACTIVE, unexpired grant from the
     * owner whose scope set covers {@code categoryToken} (e.g. {@code READ_SLEEP}; a {@code READ_ALL}
     * grant covers every category). Referenced from controllers as
     * {@code @accessGuard.canReadCategory(authentication, #profileId, 'READ_SLEEP')}.
     */
    public boolean canReadCategory(Authentication auth, UUID profileId, String categoryToken) {
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
                return dataAccessGrantService.hasScopedAccess(profileId, currentProfileId, categoryToken);
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
