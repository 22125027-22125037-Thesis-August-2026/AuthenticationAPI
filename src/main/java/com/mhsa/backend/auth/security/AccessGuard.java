package com.mhsa.backend.auth.security;

import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import com.mhsa.backend.auth.model.Role;
import com.mhsa.backend.auth.service.DataAccessGrantService;

import lombok.RequiredArgsConstructor;

/**
 * Centralized authorization component.
 *
 * Apply via: @PreAuthorize("@accessGuard.canReadTrackingData(authentication, #profileId)")
 *
 * Never duplicate this logic in service classes.
 */
@Component("accessGuard")
@RequiredArgsConstructor
public class AccessGuard {

    private final DataAccessGrantService dataAccessGrantService;

    /**
     * Returns true if the authenticated caller may read tracking data
     * belonging to {@code targetProfileId}.
     *
     * Three OR-clauses evaluated short-circuit:
     *   1. Caller is ADMIN.
     *   2. Caller is the data owner (same profileId).
     *   3. Caller holds an ACTIVE, unexpired delegated grant from the owner.
     */
    public boolean canReadTrackingData(Authentication authentication, UUID targetProfileId) {
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedUserPrincipal principal)) {
            return false;
        }

        if (principal.role() == Role.ADMIN) {
            return true;
        }

        if (principal.profileId() != null && principal.profileId().equals(targetProfileId)) {
            return true;
        }

        return dataAccessGrantService.hasDelegatedAccess(targetProfileId, principal.profileId());
    }
}
