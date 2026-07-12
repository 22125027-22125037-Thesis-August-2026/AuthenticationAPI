package com.mhsa.backend.auth.security;

import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import com.mhsa.backend.auth.jwt.AuthenticatedUserPrincipal;
import com.mhsa.backend.auth.jwt.Role;
import com.mhsa.backend.auth.model.AssignmentStatus;
import com.mhsa.backend.auth.repository.TherapistPatientAssignmentRepository;
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
    private final TherapistPatientAssignmentRepository assignmentRepository;

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

    /**
     * Returns true if the authenticated caller may view the patient profile identified by
     * {@code targetProfileId}.
     *
     * <p>Grants access when {@link #canReadTrackingData} already allows it (ADMIN, the owner, or an
     * ACTIVE delegated grant), OR when the caller is a therapist with an ACTIVE assignment to that
     * patient in the local read-model. This widens <em>profile</em> visibility for assigned
     * therapists without touching {@link #canReadTrackingData}, which still gates tracking/diaries.
     *
     * <p><b>Fail-closed:</b> an unauthenticated caller, a caller with no {@code profileId}, or the
     * absence of an ACTIVE assignment row all result in denial.
     */
    public boolean canViewPatientProfile(Authentication authentication, UUID targetProfileId) {
        if (canReadTrackingData(authentication, targetProfileId)) {
            return true;
        }

        if (authentication == null
                || !(authentication.getPrincipal() instanceof AuthenticatedUserPrincipal principal)
                || principal.profileId() == null) {
            return false;
        }

        return assignmentRepository.existsByIdTherapistProfileIdAndIdPatientProfileIdAndStatus(
                principal.profileId(), targetProfileId, AssignmentStatus.ACTIVE);
    }

    /**
     * Returns true if the authenticated caller is an ADMIN. Reads the role straight
     * off the JWT principal so it does not depend on UserDetails being resolvable.
     */
    public boolean isAdmin(Authentication authentication) {
        return authentication != null
                && authentication.getPrincipal() instanceof AuthenticatedUserPrincipal principal
                && principal.role() == Role.ADMIN;
    }

    public boolean canManageGrants(Authentication authentication, UUID profileId) {
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedUserPrincipal principal)) {
            return false;
        }

        if (principal.role() == Role.ADMIN) {
            return true;
        }

        return principal.profileId() != null && principal.profileId().equals(profileId);
    }
}

